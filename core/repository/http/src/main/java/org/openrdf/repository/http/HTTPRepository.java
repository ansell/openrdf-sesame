/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.http.client.RepositoryClient;
import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.SizeClient;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.repository.http.helpers.CachedLong;
import org.openrdf.repository.http.helpers.PrefixHashSet;
import org.openrdf.repository.http.helpers.StatementPattern;
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.StoreException;

/**
 * A repository that serves as a proxy for a remote repository on a Sesame
 * server. Methods in this class may throw the specific StoreException
 * subclasses UnautorizedException and NotAllowedException, the semantics of
 * which are defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author jeen
 * @author Herko ter Horst
 */
public class HTTPRepository implements Repository {

	/*-----------*
	 * Variables *
	 *-----------*/

	private HTTPValueFactory vf = new HTTPValueFactory();

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private SesameClient server;

	private RepositoryClient client;

	private PrefixHashSet subjectSpace;

	private File dataDir;

	private boolean initialized = false;

	private Map<StatementPattern, CachedLong> cachedSizes = new ConcurrentHashMap<StatementPattern, CachedLong>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepository(String serverURL, String repositoryID) {
		HTTPConnectionPool pool = new HTTPConnectionPool(serverURL);
		pool.setValueFactory(vf);
		server = new SesameClient(pool);
		client = server.repositories().slash(repositoryID);
	}

	public HTTPRepository(String repositoryURL) {
		String serverURL = Protocol.getServerLocation(repositoryURL);
		if (serverURL != null) {
			HTTPConnectionPool pool = new HTTPConnectionPool(serverURL);
			pool.setValueFactory(vf);
			server = new SesameClient(pool);
		}
		HTTPConnectionPool pool = new HTTPConnectionPool(repositoryURL);
		pool.setValueFactory(vf);
		client = new RepositoryClient(pool);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
	}

	public void setSubjectSpace(Set<String> uriSpace) {
		this.subjectSpace = new PrefixHashSet(uriSpace);
	}

	public void initialize()
		throws StoreException
	{
		initialized = true;
	}

	public RepositoryMetaData getRepositoryMetaData() {
		return new HTTPRepositoryMetaData(this);
	}

	public void shutDown()
		throws StoreException
	{
		initialized = false;
	}

	public LiteralFactory getLiteralFactory() {
		return vf;
	}

	public URIFactory getURIFactory() {
		return vf;
	}

	public ValueFactory getValueFactory() {
		return vf;
	}

	public RepositoryConnection getConnection()
		throws StoreException
	{
		return new HTTPRepositoryConnection(this);
	}

	public boolean isWritable()
		throws StoreException
	{
		if (!initialized) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}
		if (server == null) {
			// we don't have the server URL
			return false;
		}

		boolean isWritable = false;
		String repositoryURL = client.getURL();

		TupleQueryResult repositoryList = server.repositories().list();
		try {
			while (repositoryList.hasNext()) {
				BindingSet bindingSet = repositoryList.next();
				Value uri = bindingSet.getValue("uri");

				if (uri != null && uri.stringValue().equals(repositoryURL)) {
					isWritable = LiteralUtil.getBooleanValue(bindingSet.getValue("writable"), false);
					break;
				}
			}
		}
		finally {
			repositoryList.close();
		}

		return isWritable;
	}

	/**
	 * Sets the preferred serialization format for tuple query results to the
	 * supplied {@link TupleQueryResultFormat}, overriding the default
	 * preference. Setting this parameter is not necessary in most cases as the
	 * default indicates a preference for the most compact and efficient format
	 * available.
	 * 
	 * @param format
	 *        the preferred {@link TupleQueryResultFormat}. If set to 'null' no
	 *        explicit preference will be stated.
	 */
	public void setPreferredTupleQueryResultFormat(TupleQueryResultFormat format) {
		client.getPool().setPreferredTupleQueryResultFormat(format);
	}

	/**
	 * Indicates the current preferred {@link TupleQueryResultFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public TupleQueryResultFormat getPreferredTupleQueryResultFormat() {
		return client.getPool().getPreferredTupleQueryResultFormat();
	}

	/**
	 * Sets the preferred serialization format for RDF to the supplied
	 * {@link RDFFormat}, overriding the default preference. Setting this
	 * parameter is not necessary in most cases as the default indicates a
	 * preference for the most compact and efficient format available.
	 * <p>
	 * Use with caution: if set to a format that does not support context
	 * serialization any context info contained in the query result will be lost.
	 * 
	 * @param format
	 *        the preferred {@link RDFFormat}. If set to 'null' no explicit
	 *        preference will be stated.
	 */
	public void setPreferredRDFFormat(RDFFormat format) {
		client.getPool().setPreferredRDFFormat(format);
	}

	/**
	 * Indicates the current preferred {@link RDFFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public RDFFormat getPreferredRDFFormat() {
		return client.getPool().getPreferredRDFFormat();
	}

	/**
	 * Set the username and password to use for authenticating with the remote
	 * repository.
	 * 
	 * @param username
	 *        the username. Setting this to null will disable authentication.
	 * @param password
	 *        the password. Setting this to null will disable authentication.
	 */
	public void setUsernameAndPassword(String username, String password) {
		client.setUsernameAndPassword(username, password);
	}

	// httpClient is shared with HTTPConnection
	RepositoryClient getClient() {
		return client;
	}

	/**
	 * Indicates that the cache needs validation.
	 */
	void modified() {
		for (CachedLong cached : cachedSizes.values()) {
			cached.stale();
		}
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern (or super set) has no matches.
	 */
	boolean noMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (!vf.member(subj) || !vf.member(obj) || !vf.member(contexts))
			return true;
		if (noSubject(subj))
			return true;
		long now = System.currentTimeMillis();
		if (noExactMatch(now, subj, pred, obj, includeInferred, contexts))
			return true;
		if (noExactMatch(now, null, pred, null, true))
			return true;
		if (noExactMatch(now, null, null, null, true, contexts))
			return true;
		return false; // don't know, maybe
	}

	/**
	 * Uses cache of given pattern or super patterns before loading size.
	 */
	long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (!vf.member(subj) || !vf.member(obj) || !vf.member(contexts))
			return 0;
		if (noSubject(subj))
			return 0;
		long now = System.currentTimeMillis();
		if (noExactMatchRefreshable(now, subj, pred, obj, includeInferred, contexts))
			return 0;
		if (noExactMatchRefreshable(now, null, pred, null, true))
			return 0;
		if (noExactMatchRefreshable(now, null, null, null, true, contexts))
			return 0;
		return loadSize(now, subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * If this repository cannot contain this subject.
	 */
	private boolean noSubject(Resource subj) {
		if (subj instanceof URI && subjectSpace != null) {
			return !subjectSpace.match(subj.stringValue());
		}
		return false;
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern has no matches.
	 */
	private boolean noExactMatch(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedLong cached = cachedSizes.get(pattern);
		if (cached == null)
			return false; // don't know
		return cached.isFresh(now) && cached.getValue() == 0;
	}

	/**
	 * Will connect to the remote server. If no matches, may query the server for
	 * super patterns not in cache.
	 */
	private long loadSize(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		long size = loadExactSize(now, subj, pred, obj, includeInferred, contexts);
		if (size == 0) {
			StatementPattern orig = new StatementPattern(subj, pred, obj, includeInferred, contexts);
			StatementPattern predOnly = new StatementPattern(null, pred, null, true);
			StatementPattern ctxOnly = new StatementPattern(null, null, null, true, contexts);
			if (pred != null && !orig.equals(predOnly)) {
				// no values, does it have this predicate?
				if (!cachedSizes.containsKey(predOnly)) {
					loadExactSize(now, null, pred, null, true);
				}
			}
			if ((contexts == null || contexts.length > 0) && !orig.equals(ctxOnly)) {
				// no values, does it have this context?
				if (!cachedSizes.containsKey(ctxOnly)) {
					loadExactSize(now, null, null, null, true, contexts);
				}
			}
		}
		return size;
	}

	/**
	 * Will always connect to the remote server to ensure cache is valid (if
	 * available).
	 */
	private long loadExactSize(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedLong cached = cachedSizes.get(pattern);
		SizeClient client = getClient().size();
		if (cached != null) {
			// Only calculate size if cached value is old
			client.ifNoneMatch(cached.getETag());
		}
		Long size = client.get(subj, pred, obj, includeInferred, contexts);
		if (size == null) {
			assert cached != null : "Server did not return a size value";
			cached.refreshed(now, client.getMaxAge());
		}
		else {
			cached = new CachedLong(size, client.getETag());
			cached.refreshed(now, client.getMaxAge());
			cachedSizes.put(pattern, cached);
		}
		return cached.getValue();
	}

	/**
	 * Will connect to the remote server for validation, if it is believed that
	 * there will be no match.
	 * 
	 * @return if it is known that this pattern has no matches.
	 */
	private boolean noExactMatchRefreshable(long now, Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedLong cached = cachedSizes.get(pattern);
		if (cached == null || cached.getValue() != 0)
			return false; // might have a match
		if (cached.isFresh(now))
			return true; // no match
		return 0 == loadExactSize(now, subj, pred, obj, includeInferred, contexts);
	}
}
