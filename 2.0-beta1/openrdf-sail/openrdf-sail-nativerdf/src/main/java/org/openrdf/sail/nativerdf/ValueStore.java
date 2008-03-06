/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.MultiReadSingleWriteLockManager;
import info.aduna.io.ByteArrayUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.sail.nativerdf.datastore.DataStore;
import org.openrdf.sail.nativerdf.model.NativeBNode;
import org.openrdf.sail.nativerdf.model.NativeLiteral;
import org.openrdf.sail.nativerdf.model.NativeResource;
import org.openrdf.sail.nativerdf.model.NativeURI;
import org.openrdf.sail.nativerdf.model.NativeValue;


/**
 * File-based indexed storage and retrieval of RDF values. ValueStore maps RDF
 * values to integer IDs and vice-versa.
 */
public class ValueStore extends ValueFactoryBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String FILENAME_PREFIX = "values";

	private static final int VALUE_CACHE_SIZE = 512;

	private static final int VALUE_ID_CACHE_SIZE = 128;

	private static final int NAMESPACE_CACHE_SIZE = 64;

	private static final int NAMESPACE_ID_CACHE_SIZE = 32;

	private static final byte VALUE_TYPE_MASK = 0x3; // 0000 0011

	private static final byte URI_VALUE = 0x1; // 0000 0001

	private static final byte BNODE_VALUE = 0x2; // 0000 0010

	private static final byte LITERAL_VALUE = 0x3; // 0000 0011

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Used to do the actual storage of values, once they're translated to byte
	 * arrays.
	 */
	private DataStore _dataStore;

	/**
	 * Lock manager used to prevent the removal of values over multiple method
	 * calls. Note that values can still be added when read locks are active.
	 */
	private MultiReadSingleWriteLockManager _lockManager = new MultiReadSingleWriteLockManager();

	/**
	 * An object that indicates the revision of the value store, which is used to
	 * check if cached value IDs are still valid. In order to be valid, the
	 * ValueStoreRevision object of a NativeValue needs to be equal to this
	 * object.
	 */
	private ValueStoreRevision _revision;

	/**
	 * A simple cache containing the [VALUE_CACHE_SIZE] most-recently used values
	 * stored by their ID.
	 */
	private LRUCache<Integer, NativeValue> _valueCache;

	/**
	 * A simple cache containing the [ID_CACHE_SIZE] most-recently used value-IDs
	 * stored by their value.
	 */
	private LRUCache<Value, Integer> _valueIDCache;

	/**
	 * A simple cache containing the [NAMESPACE_CACHE_SIZE] most-recently used
	 * namespaces stored by their ID.
	 */
	private LRUCache<Integer, String> _namespaceCache;

	/**
	 * A simple cache containing the [NAMESPACE_ID_CACHE_SIZE] most-recently used
	 * namespace-IDs stored by their namespace.
	 */
	private LRUCache<String, Integer> _namespaceIDCache;

	/**
	 * The prefix for any new bnode IDs.
	 */
	private String _bnodePrefix;

	/**
	 * The ID for the next bnode that is created.
	 */
	private int _nextBNodeID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueStore(File dataDir)
		throws IOException
	{
		_dataStore = new DataStore(dataDir, FILENAME_PREFIX);

		_valueCache = new LRUCache<Integer, NativeValue>(VALUE_CACHE_SIZE);
		_valueIDCache = new LRUCache<Value, Integer>(VALUE_ID_CACHE_SIZE);
		_namespaceCache = new LRUCache<Integer, String>(NAMESPACE_CACHE_SIZE);
		_namespaceIDCache = new LRUCache<String, Integer>(NAMESPACE_ID_CACHE_SIZE);

		_updateBNodePrefix();

		_setNewRevision();
	}

	/**
	 * Generates a new bnode prefix based on <tt>currentTimeMillis()</tt> and
	 * resets <tt>_nextBNodeID</tt> to <tt>1</tt>.
	 */
	private void _updateBNodePrefix() {
		// BNode prefix is based on currentTimeMillis(). Combined with a
		// sequential number per session, this gives a unique identifier.
		_bnodePrefix = "node" + Long.toString(System.currentTimeMillis(), 32) + "x";
		_nextBNodeID = 1;
	}

	/**
	 * Creates a new revision object for this value store, invalidating any IDs
	 * cached in NativeValue objects that were created by this value store.
	 */
	private void _setNewRevision() {
		_revision = new ValueStoreRevision(this);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueStoreRevision getRevision() {
		return _revision;
	}

	/**
	 * Gets a read lock on this value store that can be used to prevent values
	 * from being removed while the lock is active.
	 */
	public Lock getReadLock()
		throws InterruptedException
	{
		return _lockManager.getReadLock();
	}

	/**
	 * Gets the value for the specified ID.
	 * 
	 * @param id
	 *        A value ID.
	 * @return The value for the ID, or <tt>null</tt> no such value could be
	 *         found.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public NativeValue getValue(int id)
		throws IOException
	{
		NativeValue resultValue = null;

		// Check value cache
		Integer cacheID = new Integer(id);
		synchronized (_valueCache) {
			resultValue = _valueCache.get(cacheID);
		}

		if (resultValue == null) {
			// Value not in cache, fetch it from file
			byte[] data = _dataStore.getData(id);

			if (data != null) {
				resultValue = _data2value(id, data);

				// Store value in cache
				synchronized (_valueCache) {
					_valueCache.put(cacheID, resultValue);
				}
			}
		}

		return resultValue;
	}

	/**
	 * Gets the ID for the specified value.
	 * 
	 * @param value
	 *        A value.
	 * @return The ID for the specified value, or {@link NativeValue#UNKNOWN_ID}
	 *         if no such ID could be found.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public int getID(Value value)
		throws IOException
	{
		// Try to get the internal ID from the value itself
		boolean isOwnValue = _isOwnValue(value);

		if (isOwnValue) {
			NativeValue nativeValue = (NativeValue)value;

			if (_revisionIsCurrent(nativeValue)) {
				int id = nativeValue.getInternalID();

				if (id != NativeValue.UNKNOWN_ID) {
					return id;
				}
			}
		}

		// Check cache
		Integer cachedID = null;
		synchronized (_valueIDCache) {
			cachedID = _valueIDCache.get(value);
		}

		if (cachedID != null) {
			int id = cachedID.intValue();

			if (isOwnValue) {
				// Store id in value for fast access in any consecutive calls
				((NativeValue)value).setInternalID(id, _revision);
			}

			return id;
		}

		// ID not cached, search in file
		byte[] data = _value2data(value, false);

		if (data != null) {
			int id = _dataStore.getID(data);

			if (id != NativeValue.UNKNOWN_ID) {
				if (isOwnValue) {
					// Store id in value for fast access in any consecutive calls
					((NativeValue)value).setInternalID(id, _revision);
				}
				else {
					// Store id in cache
					synchronized (_valueIDCache) {
						_valueIDCache.put(value, new Integer(id));
					}
				}
			}

			return id;
		}

		return NativeValue.UNKNOWN_ID;
	}

	/**
	 * Stores the supplied value and returns the ID that has been assigned to it.
	 * In case the value was already present, the value will not be stored again
	 * and the ID of the existing value is returned.
	 * 
	 * @param value
	 *        The Value to store.
	 * @return The ID that has been assigned to the value.
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public int storeValue(Value value)
		throws IOException
	{
		// Try to get the internal ID from the value itself
		boolean isOwnValue = _isOwnValue(value);

		if (isOwnValue) {
			NativeValue nativeValue = (NativeValue)value;

			if (_revisionIsCurrent(nativeValue)) {
				// Value's ID is still current
				int id = nativeValue.getInternalID();

				if (id != NativeValue.UNKNOWN_ID) {
					return id;
				}
			}
		}

		// ID not stored in value itself, try the ID cache
		Integer cachedID = null;
		synchronized (_valueIDCache) {
			cachedID = _valueIDCache.get(value);
		}

		if (cachedID != null) {
			int id = cachedID.intValue();

			if (isOwnValue) {
				// Store id in value for fast access in any consecutive calls
				((NativeValue)value).setInternalID(id, _revision);
			}

			return id;
		}

		// Unable to get internal ID in a cheap way, just store it in the data
		// store which will handle duplicates
		byte[] valueData = _value2data(value, true);

		int id = _dataStore.storeData(valueData);

		if (isOwnValue) {
			// Store id in value for fast access in any consecutive calls
			((NativeValue)value).setInternalID(id, _revision);
		}
		else {
			// Update cache
			synchronized (_valueIDCache) {
				_valueIDCache.put(value, new Integer(id));
			}
		}

		return id;
	}

	/**
	 * Removes all values from the ValueStore.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		try {
			Lock writeLock = _lockManager.getWriteLock();
			try {
				_dataStore.clear();

				synchronized (_valueCache) {
					_valueCache.clear();
				}

				synchronized (_valueIDCache) {
					_valueIDCache.clear();
				}

				synchronized (_namespaceCache) {
					_namespaceCache.clear();
				}

				synchronized (_namespaceIDCache) {
					_namespaceIDCache.clear();
				}

				_nextBNodeID = 1;

				_setNewRevision();
			}
			finally {
				writeLock.release();
			}
		}
		catch (InterruptedException e) {
			IOException ioe = new IOException("Failed to acquire write lock");
			ioe.initCause(e);
			throw ioe;
		}
	}

	/**
	 * Synchronizes any changes that are cached in memory to disk.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void sync()
		throws IOException
	{
		_dataStore.sync();
	}

	/**
	 * Closes the ValueStore, releasing any file references, etc. Once closed,
	 * the ValueStore can no longer be used.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void close()
		throws IOException
	{
		_dataStore.close();
		_valueCache = null;
		_valueIDCache = null;
		_namespaceCache = null;
		_namespaceIDCache = null;
	}

	/**
	 * Checks if the supplied Value object is a NativeValue object that has been
	 * created by this ValueStore.
	 */
	private boolean _isOwnValue(Value value) {
		return value instanceof NativeValue
				&& ((NativeValue)value).getValueStoreRevision().getValueStore() == this;
	}

	/**
	 * Checks if the revision of the supplied value object is still current.
	 */
	private boolean _revisionIsCurrent(NativeValue value) {
		return _revision.equals(value.getValueStoreRevision());
	}

	private byte[] _value2data(Value value, boolean create)
		throws IOException
	{
		if (value instanceof URI) {
			return _uri2data((URI)value, create);
		}
		else if (value instanceof BNode) {
			return _bnode2data((BNode)value, create);
		}
		else if (value instanceof Literal) {
			return _literal2data((Literal)value, create);
		}
		else {
			throw new IllegalArgumentException("value parameter should be a URI, BNode or Literal");
		}
	}

	private byte[] _uri2data(URI uri, boolean create)
		throws IOException
	{
		int nsID = _getNamespaceID(uri.getNamespace(), create);

		if (nsID == -1) {
			// Unknown namespace means unknown URI
			return null;
		}

		// Get local name in UTF-8
		byte[] localNameData = uri.getLocalName().getBytes("UTF-8");

		// Combine parts in a single byte array
		byte[] uriData = new byte[5 + localNameData.length];
		uriData[0] = URI_VALUE;
		ByteArrayUtil.putInt(nsID, uriData, 1);
		ByteArrayUtil.put(localNameData, uriData, 5);

		return uriData;
	}

	private byte[] _bnode2data(BNode bNode, boolean create)
		throws IOException
	{
		byte[] idData = bNode.getID().getBytes("UTF-8");

		byte[] bNodeData = new byte[1 + idData.length];
		bNodeData[0] = BNODE_VALUE;
		ByteArrayUtil.put(idData, bNodeData, 1);

		return bNodeData;
	}

	private byte[] _literal2data(Literal literal, boolean create)
		throws IOException
	{
		// Get datatype ID
		int datatypeID = NativeValue.UNKNOWN_ID;

		if (literal.getDatatype() != null) {
			if (create) {
				datatypeID = storeValue(literal.getDatatype());
			}
			else {
				datatypeID = getID(literal.getDatatype());

				if (datatypeID == NativeValue.UNKNOWN_ID) {
					// Unknown datatype means unknown literal
					return null;
				}
			}
		}

		// Get language tag in UTF-8
		byte[] langData = null;
		int langDataLength = 0;
		if (literal.getLanguage() != null) {
			langData = literal.getLanguage().getBytes("UTF-8");
			langDataLength = langData.length;
		}

		// Get label in UTF-8
		byte[] labelData = literal.getLabel().getBytes("UTF-8");

		// Combine parts in a single byte array
		byte[] literalData = new byte[6 + langDataLength + labelData.length];
		literalData[0] = LITERAL_VALUE;
		ByteArrayUtil.putInt(datatypeID, literalData, 1);
		literalData[5] = (byte)langDataLength;
		if (langData != null) {
			ByteArrayUtil.put(langData, literalData, 6);
		}
		ByteArrayUtil.put(labelData, literalData, 6 + langDataLength);

		return literalData;
	}

	private NativeValue _data2value(int id, byte[] data)
		throws IOException
	{
		switch ((data[0] & VALUE_TYPE_MASK)) {
			case URI_VALUE:
				return _data2uri(id, data);
			case BNODE_VALUE:
				return _data2bnode(id, data);
			case LITERAL_VALUE:
				return _data2literal(id, data);
			default:
				throw new IllegalArgumentException("data does not specify a known value type");
		}
	}

	private NativeURI _data2uri(int id, byte[] data)
		throws IOException
	{
		int nsID = ByteArrayUtil.getInt(data, 1);
		String namespace = _getNamespace(nsID);

		String localName = new String(data, 5, data.length - 5, "UTF-8");

		return new NativeURI(_revision, namespace, localName, id);
	}

	private NativeBNode _data2bnode(int id, byte[] data)
		throws IOException
	{
		String nodeID = new String(data, 1, data.length - 1, "UTF-8");
		return new NativeBNode(_revision, nodeID, id);
	}

	private NativeLiteral _data2literal(int id, byte[] data)
		throws IOException
	{
		// Get datatype
		int datatypeID = ByteArrayUtil.getInt(data, 1);
		URI datatype = null;
		if (datatypeID != NativeValue.UNKNOWN_ID) {
			datatype = (URI)getValue(datatypeID);
		}

		// Get language tag
		String lang = null;
		int langLength = data[5];
		if (langLength > 0) {
			lang = new String(data, 6, langLength, "UTF-8");
		}

		// Get label
		String label = new String(data, 6 + langLength, data.length - 6 - langLength, "UTF-8");

		if (datatype != null) {
			return new NativeLiteral(_revision, label, datatype, id);
		}
		else if (lang != null) {
			return new NativeLiteral(_revision, label, lang, id);
		}
		else {
			return new NativeLiteral(_revision, label, id);
		}
	}

	private int _getNamespaceID(String namespace, boolean create)
		throws IOException
	{
		int id;

		Integer cacheID = null;
		synchronized (_namespaceIDCache) {
			cacheID = _namespaceIDCache.get(namespace);
		}

		if (cacheID != null) {
			id = cacheID.intValue();
		}
		else {
			byte[] namespaceData = namespace.getBytes("UTF-8");

			if (create) {
				id = _dataStore.storeData(namespaceData);
			}
			else {
				id = _dataStore.getID(namespaceData);
			}

			if (id != -1) {
				_namespaceIDCache.put(namespace, new Integer(id));
			}
		}

		return id;
	}

	private String _getNamespace(int id)
		throws IOException
	{
		Integer cacheID = new Integer(id);
		String namespace = null;

		synchronized (_namespaceCache) {
			namespace = _namespaceCache.get(cacheID);
		}

		if (namespace == null) {
			byte[] namespaceData = _dataStore.getData(id);
			namespace = new String(namespaceData, "UTF-8");

			synchronized (_namespaceCache) {
				_namespaceCache.put(cacheID, namespace);
			}
		}

		return namespace;
	}

	/*-------------------------------------*
	 * Methods from interface ValueFactory *
	 *-------------------------------------*/

	// Implements ValueFactory.createURI(String)
	public NativeURI createURI(String uri) {
		return new NativeURI(_revision, uri);
	}

	public NativeURI createURI(String namespace, String localName) {
		return new NativeURI(_revision, namespace, localName);
	}

	// Implements ValueFactory.createBNode()
	public NativeBNode createBNode() {
		if (_nextBNodeID == Integer.MAX_VALUE) {
			// Start with a new bnode prefix
			_updateBNodePrefix();
		}

		return createBNode(_bnodePrefix + _nextBNodeID++);
	}

	// Implements ValueFactory.createBNode(String)
	public NativeBNode createBNode(String nodeID) {
		return new NativeBNode(_revision, nodeID);
	}

	// Implements ValueFactory.createLiteral(String)
	public NativeLiteral createLiteral(String value) {
		return new NativeLiteral(_revision, value);
	}

	// Implements ValueFactory.createLiteral(String, String)
	public NativeLiteral createLiteral(String value, String language) {
		return new NativeLiteral(_revision, value, language);
	}

	// Implements ValueFactory.createLiteral(String, URI)
	public NativeLiteral createLiteral(String value, URI datatype) {
		return new NativeLiteral(_revision, value, datatype);
	}

	// Implements ValueFactory.createStatement(Resource, URI, Value)
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	// Implements ValueFactory.createStatement(Resource, URI, Value, Resource )
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return new ContextStatementImpl(subject, predicate, object, context);
	}

	/*----------------------------------------------------------------------*
	 * Methods for converting model objects to NativeStore-specific objects * 
	 *----------------------------------------------------------------------*/

	public NativeValue getNativeValue(Value value) {
		if (value instanceof Resource) {
			return getNativeResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return getNativeLiteral((Literal)value);
		}
		else {
			throw new IllegalArgumentException("Unknown value type: " + value.getClass());
		}
	}

	public NativeResource getNativeResource(Resource resource) {
		if (resource instanceof URI) {
			return getNativeURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getNativeBNode((BNode)resource);
		}
		else {
			throw new IllegalArgumentException("Unknown resource type: " + resource.getClass());
		}
	}

	/**
	 * Creates a NativeURI that is equal to the supplied URI. This method returns
	 * the supplied URI itself if it is already a NativeURI that has been created
	 * by this ValueStore, which prevents unnecessary object creations.
	 * 
	 * @return A NativeURI for the specified URI.
	 */
	public NativeURI getNativeURI(URI uri) {
		if (_isOwnValue(uri)) {
			return (NativeURI)uri;
		}

		return new NativeURI(_revision, uri.toString());
	}

	/**
	 * Creates a NativeBNode that is equal to the supplied bnode. This method
	 * returns the supplied bnode itself if it is already a NativeBNode that has
	 * been created by this ValueStore, which prevents unnecessary object
	 * creations.
	 * 
	 * @return A NativeBNode for the specified bnode.
	 */
	public NativeBNode getNativeBNode(BNode bnode) {
		if (_isOwnValue(bnode)) {
			return (NativeBNode)bnode;
		}

		return new NativeBNode(_revision, bnode.getID());
	}

	/**
	 * Creates an NativeLiteral that is equal to the supplied literal. This
	 * method returns the supplied literal itself if it is already a
	 * NativeLiteral that has been created by this ValueStore, which prevents
	 * unnecessary object creations.
	 * 
	 * @return A NativeLiteral for the specified literal.
	 */
	public NativeLiteral getNativeLiteral(Literal l) {
		if (_isOwnValue(l)) {
			return (NativeLiteral)l;
		}

		if (l.getLanguage() != null) {
			return new NativeLiteral(_revision, l.getLabel(), l.getLanguage());
		}
		else if (l.getDatatype() != null) {
			NativeURI datatype = getNativeURI(l.getDatatype());
			return new NativeLiteral(_revision, l.getLabel(), datatype);
		}
		else {
			return new NativeLiteral(_revision, l.getLabel());
		}
	}

	/*--------------------*
	 * Test/debug methods *
	 *--------------------*/

	public static void main(String[] args)
		throws Exception
	{
		File dataDir = new File(args[0]);
		ValueStore valueStore = new ValueStore(dataDir);

		int maxID = valueStore._dataStore.getMaxID();
		for (int id = 1; id <= maxID; id++) {
			Value value = valueStore.getValue(id);
			System.out.println("[" + id + "] " + value.toString());
		}
	}
}
