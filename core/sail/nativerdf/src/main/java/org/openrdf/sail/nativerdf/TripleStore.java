/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.ByteArrayUtil;

import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.btree.BTree;
import org.openrdf.sail.nativerdf.btree.RecordComparator;
import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * File-based indexed storage and retrieval of RDF statements. TripleStore
 * stores statements in the form of four integer IDs. Each ID represent an RDF
 * value that is stored in a {@link ValueStore}. The four IDs refer to the
 * statement's subject, predicate, object and context. The ID <tt>0</tt> is
 * used to represent the "null" context and doesn't map to an actual RDF value.
 * 
 * @author Arjohn Kampman
 */
class TripleStore {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The file name for the properties file.
	 */
	private static final String PROPERTIES_FILE = "triples.prop";

	/**
	 * The key used to store the triple store version in the properties file.
	 */
	private static final String VERSION_KEY = "version";

	/**
	 * The key used to store the triple indexes specification that specifies
	 * which triple indexes exist.
	 */
	private static final String INDEXES_KEY = "triple-indexes";

	/**
	 * The version number for the current triple store.
	 * <ul>
	 * <li>version 0: The first version which used a single spo-index. This
	 * version did not have a properties file yet.
	 * <li>version 1: Introduces configurable triple indexes and the properties
	 * file.
	 * <li>version 10: Introduces a context field, essentially making this a
	 * quad store.
	 * <li>version 10a: Introduces transaction flags, this is backwards
	 * compatible with version 10.
	 * </ul>
	 */
	private static final int SCHEME_VERSION = 10;

	// 17 bytes are used to represent a triple:
	// byte 0-3 : subject
	// byte 4-7 : predicate
	// byte 8-11: object
	// byte 12-15: context
	// byte 16: additional flag(s)
	static final int RECORD_LENGTH = 17;

	static final int SUBJ_IDX = 0;

	static final int PRED_IDX = 4;

	static final int OBJ_IDX = 8;

	static final int CONTEXT_IDX = 12;

	static final int FLAG_IDX = 16;

	/**
	 * Bit field indicating that a statement has been explicitly added (instead
	 * of being inferred).
	 */
	static final byte EXPLICIT_FLAG = (byte)0x1; // 0000 0001

	/**
	 * Bit field indicating that a statement has been added in a (currently
	 * active) transaction.
	 */
	static final byte ADDED_FLAG = (byte)0x2; // 0000 0010

	/**
	 * Bit field indicating that a statement has been removed in a (currently
	 * active) transaction.
	 */
	static final byte REMOVED_FLAG = (byte)0x4; // 0000 0100

	/**
	 * Bit field indicating that the explicit flag has been toggled (from true to
	 * false, or vice versa) in a (currently active) transaction.
	 */
	static final byte TOGGLE_EXPLICIT_FLAG = (byte)0x8; // 0000 1000

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The directory that is used to store the index files.
	 */
	private File dir;

	/**
	 * Object containing meta-data for the triple store. This includes
	 */
	private Properties properties;

	/**
	 * The array of triple indexes that are used to store and retrieve triples.
	 */
	private TripleIndex[] indexes;

	private boolean forceSync;

	/**
	 * Flag indicating whether one or more triples have been flagged as "added"
	 * during the current transaction.
	 */
	private boolean txnAddedTriples = false;

	/**
	 * Flag indicating whether one or more triples have been flagged as "removed"
	 * during the current transaction.
	 */
	private boolean txnRemovedTriples = false;

	private RecordCache updatedTriplesCache;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TripleStore(File dir, String indexSpecStr)
		throws IOException, SailException
	{
		this(dir, indexSpecStr, false);
	}

	public TripleStore(File dir, String indexSpecStr, boolean forceSync)
		throws IOException, SailException
	{
		this.dir = dir;
		this.forceSync = forceSync;
		properties = new Properties();

		// Read triple properties file, restore indexes, re-index
		File propFile = new File(dir, PROPERTIES_FILE);

		if (propFile.exists()) {
			loadProperties(propFile);

			// Check version number
			String versionStr = properties.getProperty(VERSION_KEY);
			if (versionStr == null) {
				logger.warn("version missing in TripleStore's properties file");
			}
			else {
				try {
					int version = Integer.parseInt(versionStr);
					if (version < 10) {
						throw new SailException("Directory contains incompatible triple data");
					}
					else if (version > SCHEME_VERSION) {
						throw new SailException("Directory contains data that uses a newer data format");
					}
				}
				catch (NumberFormatException e) {
					logger.warn("Malformed version number in TripleStore's properties file");
				}
			}
		}

		Set<String> indexSpecs = parseIndexSpecList(indexSpecStr);

		if (indexSpecs.isEmpty()) {
			// Create default spoc and posc indexes
			logger.info("No indexes specified, defaulting to indexes: spoc, posc");
			indexSpecs.add("spoc");
			indexSpecs.add("posc");
			indexSpecStr = "spoc,posc";
		}

		// Initialize added indexes and delete removed ones:
		reindex(indexSpecs);

		if (!String.valueOf(SCHEME_VERSION).equals(properties.getProperty(VERSION_KEY))
				|| !indexSpecStr.equals(properties.getProperty(INDEXES_KEY)))
		{
			// Store up-to-date properties
			properties.setProperty(VERSION_KEY, String.valueOf(SCHEME_VERSION));
			properties.setProperty(INDEXES_KEY, indexSpecStr);
			storeProperties(propFile);
		}

		// Create specified indexes
		indexes = new TripleIndex[indexSpecs.size()];
		int i = 0;
		for (String fieldSeq : indexSpecs) {
			logger.debug("Activating index '" + fieldSeq + "'...");
			indexes[i++] = new TripleIndex(fieldSeq);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Parses a comma/whitespace-separated list of index specifications. Index
	 * specifications are required to consists of 4 characters: 's', 'p', 'o' and
	 * 'c'.
	 * 
	 * @param indexSpecStr
	 *        A string like "spoc, pocs, cosp".
	 * @return A Set containing the parsed index specifications.
	 */
	private Set<String> parseIndexSpecList(String indexSpecStr)
		throws SailException
	{
		Set<String> indexes = new HashSet<String>();

		if (indexSpecStr != null) {
			StringTokenizer tok = new StringTokenizer(indexSpecStr, ", \t");
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken().toLowerCase();

				// sanity checks
				if (index.length() != 4 || index.indexOf('s') == -1 || index.indexOf('p') == -1
						|| index.indexOf('o') == -1 || index.indexOf('c') == -1)
				{
					throw new SailException("invalid value '" + index + "' in index specification: "
							+ indexSpecStr);
				}

				indexes.add(index);
			}
		}

		return indexes;
	}

	private void reindex(Set<String> newIndexSpecs)
		throws IOException, SailException
	{
		// Check if the index specification has changed and update indexes if
		// necessary
		String currentIndexSpecStr = properties.getProperty(INDEXES_KEY);
		if (currentIndexSpecStr == null) {
			return;
		}

		Set<String> currentIndexSpecs = parseIndexSpecList(currentIndexSpecStr);

		if (currentIndexSpecs.isEmpty()) {
			throw new SailException("Invalid index specification found in index properties");
		}

		// Determine the set of newly added indexes
		Set<String> addedIndexSpecs = new HashSet<String>(newIndexSpecs);
		addedIndexSpecs.removeAll(currentIndexSpecs);

		if (!addedIndexSpecs.isEmpty()) {
			// Initialize new indexes using an existing index as source
			String sourceIndexSpec = currentIndexSpecs.iterator().next();
			TripleIndex sourceIndex = new TripleIndex(sourceIndexSpec);

			try {
				for (String fieldSeq : addedIndexSpecs) {
					logger.debug("Initializing new index '" + fieldSeq + "'...");

					TripleIndex addedIndex = new TripleIndex(fieldSeq);
					BTree addedBTree = addedIndex.getBTree();

					RecordIterator sourceIter = sourceIndex.getBTree().iterateAll();
					try {
						byte[] value = null;
						while ((value = sourceIter.next()) != null) {
							addedBTree.insert(value);
						}
					}
					finally {
						sourceIter.close();
					}

					addedBTree.close();
				}

				logger.debug("New index(es) initialized");
			}
			finally {
				sourceIndex.getBTree().close();
			}
		}

		// Determine the set of removed indexes
		Set<String> removedIndexSpecs = new HashSet<String>(currentIndexSpecs);
		removedIndexSpecs.removeAll(newIndexSpecs);

		// Delete files for removed indexes
		for (String fieldSeq : removedIndexSpecs) {
			boolean deleted = getIndexFile(fieldSeq).delete();

			if (deleted) {
				logger.debug("Deleted file for removed " + fieldSeq + " index");
			}
			else {
				logger.warn("Unable to delete file for removed " + fieldSeq + " index");
			}
		}
	}

	public void close()
		throws IOException
	{
		for (int i = 0; i < indexes.length; i++) {
			indexes[i].getBTree().close();
		}
		indexes = null;
	}

	public RecordIterator getTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		// Return all triples except those that were added but not yet committed
		return getTriples(subj, pred, obj, context, 0, ADDED_FLAG);
	}

	public RecordIterator getTriples(int subj, int pred, int obj, int context, boolean readTransaction)
		throws IOException
	{
		if (readTransaction) {
			// Don't read removed statements
			return getTriples(subj, pred, obj, context, 0, TripleStore.REMOVED_FLAG);
		}
		else {
			// Don't read added statements
			return getTriples(subj, pred, obj, context, 0, TripleStore.ADDED_FLAG);
		}
	}

	public RecordIterator getTriples(int subj, int pred, int obj, int context, boolean explicit,
			boolean readTransaction)
		throws IOException
	{
		int flags = 0;
		int flagsMask = 0;

		if (readTransaction) {
			flagsMask |= TripleStore.REMOVED_FLAG;
			// 'explicit' is handled through an ExplicitStatementFilter
		}
		else {
			flagsMask |= TripleStore.ADDED_FLAG;

			if (explicit) {
				flags |= TripleStore.EXPLICIT_FLAG;
				flagsMask |= TripleStore.EXPLICIT_FLAG;
			}
		}

		RecordIterator btreeIter = getTriples(subj, pred, obj, context, flags, flagsMask);

		if (readTransaction && explicit) {
			// Filter implicit statements from the result
			btreeIter = new ExplicitStatementFilter(btreeIter);
		}

		return btreeIter;
	}

	/*-------------------------------------*
	 * Inner class ExplicitStatementFilter *
	 *-------------------------------------*/

	private static class ExplicitStatementFilter implements RecordIterator {

		private final RecordIterator wrappedIter;

		public ExplicitStatementFilter(RecordIterator wrappedIter) {
			this.wrappedIter = wrappedIter;
		}

		public byte[] next()
			throws IOException
		{
			byte[] result;

			while ((result = wrappedIter.next()) != null) {
				byte flags = result[TripleStore.FLAG_IDX];
				boolean explicit = (flags & TripleStore.EXPLICIT_FLAG) != 0;
				boolean toggled = (flags & TripleStore.TOGGLE_EXPLICIT_FLAG) != 0;

				if (explicit != toggled) {
					// Statement is either explicit and hasn't been toggled, or vice
					// versa
					break;
				}
			}

			return result;
		}

		public void set(byte[] value)
			throws IOException
		{
			wrappedIter.set(value);
		}

		public void close()
			throws IOException
		{
			wrappedIter.close();
		}
	} // end inner class ExplicitStatementFilter

	private RecordIterator getTriples(int subj, int pred, int obj, int context, int flags, int flagsMask)
		throws IOException
	{
		return getTriples(subj, pred, obj, context, flags, flagsMask, indexes);
	}

	private RecordIterator getTriples(int subj, int pred, int obj, int context, int flags, int flagsMask,
			TripleIndex... indexes)
		throws IOException
	{
		// Get best matching index
		int bestScore = -1;
		TripleIndex bestIndex = null;
		for (int i = 0; i < indexes.length; i++) {
			int score = indexes[i].getPatternScore(subj, pred, obj, context);
			if (score > bestScore) {
				bestScore = score;
				bestIndex = indexes[i];
			}
		}

		byte[] searchKey = getSearchKey(subj, pred, obj, context, flags);
		byte[] searchMask = getSearchMask(subj, pred, obj, context, flagsMask);

		if (bestScore > 0) {
			// Use ranged search
			byte[] minValue = getMinValue(subj, pred, obj, context);
			byte[] maxValue = getMaxValue(subj, pred, obj, context);

			return bestIndex.getBTree().iterateRangedValues(searchKey, searchMask, minValue, maxValue);
		}
		else {
			// Use sequential scan
			return bestIndex.getBTree().iterateValues(searchKey, searchMask);
		}
	}

	public void clear()
		throws IOException
	{
		for (int i = 0; i < indexes.length; i++) {
			indexes[i].getBTree().clear();
		}
	}

	public boolean storeTriple(int subj, int pred, int obj, int context)
		throws IOException
	{
		return storeTriple(subj, pred, obj, context, true);
	}

	public boolean storeTriple(int subj, int pred, int obj, int context, boolean explicit)
		throws IOException
	{
		boolean result = false;

		byte[] data = getData(subj, pred, obj, context, 0);
		byte[] storedData = indexes[0].getBTree().get(data);

		if (storedData == null) {
			// Statement does not yet exist
			data[FLAG_IDX] |= ADDED_FLAG;
			if (explicit) {
				data[FLAG_IDX] |= EXPLICIT_FLAG;
			}

			result = true;
			txnAddedTriples = true;
		}
		else {
			// Statement already exists, only modify its flags, see txn-flags.txt
			// for a description of the flag transformations
			byte flags = storedData[FLAG_IDX];
			boolean isExplicit = (flags & EXPLICIT_FLAG) != 0;
			boolean added = (flags & ADDED_FLAG) != 0;
			boolean removed = (flags & REMOVED_FLAG) != 0;
			boolean toggled = (flags & TOGGLE_EXPLICIT_FLAG) != 0;

			if (added) {
				// Statement has been added in the current transaction and is
				// invisible to other connections
				data[FLAG_IDX] |= ADDED_FLAG;
				if (explicit || isExplicit) {
					data[FLAG_IDX] |= EXPLICIT_FLAG;
				}
			}
			else {
				// Committed statement, must keep explicit flag the same
				if (isExplicit) {
					data[FLAG_IDX] |= EXPLICIT_FLAG;
				}

				if (explicit) {
					if (!isExplicit) {
						// Make inferred statement explicit
						data[FLAG_IDX] |= TOGGLE_EXPLICIT_FLAG;
					}
				}
				else {
					if (removed) {
						if (isExplicit) {
							// Re-add removed explicit statement as inferred
							data[FLAG_IDX] |= TOGGLE_EXPLICIT_FLAG;
						}
					}
					else if (toggled) {
						data[FLAG_IDX] |= TOGGLE_EXPLICIT_FLAG;
					}
				}
			}

			// Statement is new when it used to be removed
			result = removed;
		}

		if (storedData == null || !Arrays.equals(data, storedData)) {
			for (TripleIndex index : indexes) {
				index.getBTree().insert(data);
			}

			updatedTriplesCache.storeRecord(data);
		}

		return result;
	}

	public int removeTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		RecordIterator iter = getTriples(subj, pred, obj, context, 0, 0);
		return removeTriples(iter);
	}

	/**
	 * @param subj
	 *        The subject for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param pred
	 *        The predicate for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param obj
	 *        The object for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param context
	 *        The context for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param explicit
	 *        Flag indicating whether explicit or inferred statements should be
	 *        removed; <tt>true</tt> removes explicit statements that match the
	 *        pattern, <tt>false</tt> removes inferred statements that match
	 *        the pattern.
	 * @return The number of triples that were removed.
	 * @throws IOException
	 */
	public int removeTriples(int subj, int pred, int obj, int context, boolean explicit)
		throws IOException
	{
		byte flags = explicit ? EXPLICIT_FLAG : 0;
		RecordIterator iter = getTriples(subj, pred, obj, context, flags, EXPLICIT_FLAG);
		return removeTriples(iter);
	}

	private int removeTriples(RecordIterator iter)
		throws IOException
	{
		byte[] data = iter.next();

		if (data == null) {
			// no discarded triples
			return 0;
		}

		int count = 0;

		// Store the values that need to be removed in a tmp file and then
		// iterate over this file to set the REMOVED flag
		RecordCache removedTriplesCache = new SequentialRecordCache(dir, RECORD_LENGTH);
		try {
			while (data != null) {
				if ((data[FLAG_IDX] & REMOVED_FLAG) == 0) {
					data[FLAG_IDX] |= REMOVED_FLAG;
					removedTriplesCache.storeRecord(data);
				}
				data = iter.next();
			}
			iter.close();

			count = (int)removedTriplesCache.getRecordCount();
			updatedTriplesCache.storeRecords(removedTriplesCache);

			for (TripleIndex index : indexes) {
				BTree btree = index.getBTree();

				RecordIterator recIter = removedTriplesCache.getRecords();
				try {
					while ((data = recIter.next()) != null) {
						btree.insert(data);
					}
				}
				finally {
					recIter.close();
				}
			}
		}
		finally {
			removedTriplesCache.discard();
		}

		if (count > 0) {
			txnRemovedTriples = true;
		}

		return count;
	}

	private void discardTriples(RecordIterator iter)
		throws IOException
	{
		byte[] data = iter.next();

		if (data == null) {
			// no discarded triples
			return;
		}

		// Store the values that need to be discarded in a tmp file and then
		// iterate over this file to discard the values
		RecordCache recordCache = new SequentialRecordCache(dir, RECORD_LENGTH);
		try {
			while (data != null) {
				recordCache.storeRecord(data);
				data = iter.next();
			}
			iter.close();

			for (TripleIndex index : indexes) {
				BTree btree = index.getBTree();

				RecordIterator recIter = recordCache.getRecords();
				try {
					while ((data = recIter.next()) != null) {
						btree.remove(data);
					}
				}
				finally {
					recIter.close();
				}
			}
		}
		finally {
			recordCache.discard();
		}
	}

	public void startTransaction()
		throws IOException
	{
		// Create a record cache for storing updated triples with a maximum of
		// some 10% of the number of triples
		long maxRecords = indexes[0].getBTree().getValueCountEstimate() / 10L;
		updatedTriplesCache = new SortedRecordCache(dir, RECORD_LENGTH, maxRecords,
				new TripleComparator("spoc"));
	}

	public void commit()
		throws IOException
	{
		if (txnRemovedTriples) {
			RecordIterator iter = getTriples(-1, -1, -1, -1, REMOVED_FLAG, REMOVED_FLAG);
			try {
				discardTriples(iter);
			}
			finally {
				txnRemovedTriples = false;
				iter.close();
			}
		}

		boolean validCache = updatedTriplesCache.isValid();

		for (TripleIndex index : indexes) {
			BTree btree = index.getBTree();

			RecordIterator iter;
			if (validCache) {
				// Use the cached set of updated triples
				iter = updatedTriplesCache.getRecords();
			}
			else {
				// Cache is invalid; too much updates(?). Iterate over all triples
				iter = btree.iterateAll();
			}

			try {
				byte[] data = null;
				while ((data = iter.next()) != null) {
					byte flags = data[FLAG_IDX];
					boolean added = (flags & ADDED_FLAG) != 0;
					boolean removed = (flags & REMOVED_FLAG) != 0;
					boolean toggled = (flags & TOGGLE_EXPLICIT_FLAG) != 0;

					if (removed) {
						// Record has been discarded earlier, do not put it back in!
						continue;
					}

					if (added || toggled) {
						if (toggled) {
							data[FLAG_IDX] ^= EXPLICIT_FLAG;
						}
						if (added) {
							data[FLAG_IDX] ^= ADDED_FLAG;
						}

						if (validCache) {
							// We're iterating the cache
							btree.insert(data);
						}
						else {
							// We're iterating the BTree itself
							iter.set(data);
						}
					}
				}
			}
			finally {
				iter.close();
			}
		}

		updatedTriplesCache.discard();
		updatedTriplesCache = null;

		sync();
	}

	public void rollback()
		throws IOException
	{
		if (txnAddedTriples) {
			RecordIterator iter = getTriples(-1, -1, -1, -1, ADDED_FLAG, ADDED_FLAG);
			try {
				discardTriples(iter);
			}
			finally {
				txnAddedTriples = false;
				iter.close();
			}
		}

		boolean validCache = updatedTriplesCache.isValid();

		byte txnFlagsMask = ~(ADDED_FLAG | REMOVED_FLAG | TOGGLE_EXPLICIT_FLAG);

		for (TripleIndex index : indexes) {
			BTree btree = index.getBTree();

			RecordIterator iter;
			if (validCache) {
				// Use the cached set of updated triples
				iter = updatedTriplesCache.getRecords();
			}
			else {
				// Cache is invalid; too much updates(?). Iterate over all triples
				iter = btree.iterateAll();
			}

			try {
				byte[] data = null;
				while ((data = iter.next()) != null) {
					byte flags = data[FLAG_IDX];
					boolean removed = (flags & REMOVED_FLAG) != 0;
					boolean toggled = (flags & TOGGLE_EXPLICIT_FLAG) != 0;

					if (removed || toggled) {
						data[FLAG_IDX] &= txnFlagsMask;

						if (validCache) {
							// We're iterating the cache
							btree.insert(data);
						}
						else {
							// We're iterating the BTree itself
							iter.set(data);
						}
					}
				}
			}
			finally {
				iter.close();
			}
		}

		updatedTriplesCache.discard();
		updatedTriplesCache = null;

		sync();
	}

	protected void sync()
		throws IOException
	{
		for (int i = 0; i < indexes.length; i++) {
			indexes[i].getBTree().sync();
		}
	}

	private byte[] getData(int subj, int pred, int obj, int context, int flags) {
		byte[] data = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt(subj, data, SUBJ_IDX);
		ByteArrayUtil.putInt(pred, data, PRED_IDX);
		ByteArrayUtil.putInt(obj, data, OBJ_IDX);
		ByteArrayUtil.putInt(context, data, CONTEXT_IDX);
		data[FLAG_IDX] = (byte)flags;

		return data;
	}

	private byte[] getSearchKey(int subj, int pred, int obj, int context, int flags) {
		return getData(subj, pred, obj, context, flags);
	}

	private byte[] getSearchMask(int subj, int pred, int obj, int context, int flags) {
		byte[] mask = new byte[RECORD_LENGTH];

		if (subj != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, SUBJ_IDX);
		}
		if (pred != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, PRED_IDX);
		}
		if (obj != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, OBJ_IDX);
		}
		if (context != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, CONTEXT_IDX);
		}
		mask[FLAG_IDX] = (byte)flags;

		return mask;
	}

	private byte[] getMinValue(int subj, int pred, int obj, int context) {
		byte[] minValue = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt((subj == -1 ? 0x00000000 : subj), minValue, SUBJ_IDX);
		ByteArrayUtil.putInt((pred == -1 ? 0x00000000 : pred), minValue, PRED_IDX);
		ByteArrayUtil.putInt((obj == -1 ? 0x00000000 : obj), minValue, OBJ_IDX);
		ByteArrayUtil.putInt((context == -1 ? 0x00000000 : context), minValue, CONTEXT_IDX);
		minValue[FLAG_IDX] = (byte)0;

		return minValue;
	}

	private byte[] getMaxValue(int subj, int pred, int obj, int context) {
		byte[] maxValue = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt((subj == -1 ? 0xffffffff : subj), maxValue, SUBJ_IDX);
		ByteArrayUtil.putInt((pred == -1 ? 0xffffffff : pred), maxValue, PRED_IDX);
		ByteArrayUtil.putInt((obj == -1 ? 0xffffffff : obj), maxValue, OBJ_IDX);
		ByteArrayUtil.putInt((context == -1 ? 0xffffffff : context), maxValue, CONTEXT_IDX);
		maxValue[FLAG_IDX] = (byte)0xff;

		return maxValue;
	}

	private File getIndexFile(String fieldSeq) {
		return new File(dir, "triples-" + fieldSeq + ".dat");
	}

	private void loadProperties(File propFile)
		throws IOException
	{
		InputStream in = new FileInputStream(propFile);
		try {
			properties.clear();
			properties.load(in);
		}
		finally {
			in.close();
		}
	}

	private void storeProperties(File propFile)
		throws IOException
	{
		OutputStream out = new FileOutputStream(propFile);
		try {
			properties.store(out, "triple indexes meta-data, DO NOT EDIT!");
		}
		finally {
			out.close();
		}
	}

	/*-------------------------*
	 * Inner class TripleIndex *
	 *-------------------------*/

	private class TripleIndex {

		private TripleComparator tripleComparator;

		private BTree btree;

		public TripleIndex(String fieldSeq)
			throws IOException
		{
			tripleComparator = new TripleComparator(fieldSeq);
			File btreeFile = getIndexFile(fieldSeq);
			btree = new BTree(btreeFile, 2048, RECORD_LENGTH, tripleComparator, forceSync);
		}

		public char[] getFieldSeq() {
			return tripleComparator.getFieldSeq();
		}

		public File getFile() {
			return btree.getFile();
		}

		public BTree getBTree() {
			return btree;
		}

		/**
		 * Determines the 'score' of this index on the supplied pattern of
		 * subject, predicate, object and context IDs. The higher the score, the
		 * better the index is suited for matching the pattern. Lowest score is 0,
		 * which means that the index will perform a sequential scan.
		 */
		public int getPatternScore(int subj, int pred, int obj, int context) {
			int score = 0;

			for (char field : tripleComparator.getFieldSeq()) {
				switch (field) {
					case 's':
						if (subj >= 0) {
							score++;
						}
						else {
							return score;
						}
						break;
					case 'p':
						if (pred >= 0) {
							score++;
						}
						else {
							return score;
						}
						break;
					case 'o':
						if (obj >= 0) {
							score++;
						}
						else {
							return score;
						}
						break;
					case 'c':
						if (context >= 0) {
							score++;
						}
						else {
							return score;
						}
						break;
					default:
						throw new RuntimeException("invalid character '" + field + "' in field sequence: "
								+ new String(tripleComparator.getFieldSeq()));
				}
			}

			return score;
		}
	}

	/*------------------------------*
	 * Inner class TripleComparator *
	 *------------------------------*/

	/**
	 * A RecordComparator that can be used to create indexes with a configurable
	 * order of the subject, predicate, object and context fields.
	 */
	private static class TripleComparator implements RecordComparator {

		private char[] fieldSeq;

		public TripleComparator(String fieldSeq) {
			this.fieldSeq = fieldSeq.toCharArray();
		}

		public char[] getFieldSeq() {
			return fieldSeq;
		}

		public final int compareBTreeValues(byte[] key, byte[] data, int offset, int length) {
			for (char field : fieldSeq) {
				int fieldIdx = 0;

				switch (field) {
					case 's':
						fieldIdx = SUBJ_IDX;
						break;
					case 'p':
						fieldIdx = PRED_IDX;
						break;
					case 'o':
						fieldIdx = OBJ_IDX;
						break;
					case 'c':
						fieldIdx = CONTEXT_IDX;
						break;
					default:
						throw new IllegalArgumentException("invalid character '" + field + "' in field sequence: "
								+ new String(fieldSeq));
				}

				int diff = ByteArrayUtil.compareRegion(key, fieldIdx, data, offset + fieldIdx, 4);

				if (diff != 0) {
					return diff;
				}
			}

			return 0;
		}
	}
}
