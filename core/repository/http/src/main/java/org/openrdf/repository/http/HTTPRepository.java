/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.util.Set;

import org.openrdf.http.client.ConnectionClient;
import org.openrdf.http.client.RepositoryClient;
import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.repository.http.helpers.HTTPValueFactory;
import org.openrdf.repository.http.helpers.RepositoryCache;
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
	private RepositoryClient client;

	private RepositoryCache cache;

	private File dataDir;

	private boolean initialized = false;

	private RepositoryMetaData metadata;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepository(String serverURL, String repositoryID) {
		HTTPConnectionPool pool = new HTTPConnectionPool(serverURL);
		pool.setValueFactory(vf);
		client = new SesameClient(pool).repositories().slash(repositoryID);
		cache = new RepositoryCache(client, vf);
	}

	public HTTPRepository(String repositoryURL) {
		HTTPConnectionPool pool = new HTTPConnectionPool(repositoryURL);
		pool.setValueFactory(vf);
		client = new RepositoryClient(pool);
		cache = new RepositoryCache(client, vf);
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
		cache.setSubjectSpace(uriSpace);
	}

	public void setTypeSpace(Set<String> uriSpace) {
		cache.setTypeSpace(uriSpace);
	}

	public void initialize()
		throws StoreException
	{
		initialized = true;
	}

	public RepositoryMetaData getMetaData()
		throws StoreException
	{
		if (metadata == null) {
			metadata = HTTPRepositoryMetaData.create(client.metadata().get());
		}
		return metadata;
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

	public HTTPValueFactory getValueFactory() {
		return vf;
	}

	public RepositoryConnection getConnection()
		throws StoreException
	{
		ConnectionClient connection = client.connections().post();
		return new HTTPRepositoryConnection(this, connection);
	}

	public boolean isWritable()
		throws StoreException
	{
		if (!initialized) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}
		return !getMetaData().isReadOnly();
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

	@Override
	public String toString() {
		return client.getURL();
	}

	/**
	 * Indicates that the cache needs validation.
	 */
	void modified() {
		cache.modified();
	}

	boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource[] contexts) throws StoreException
	{
		return cache.hasStatement(subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Will never connect to the remote server.
	 * @return if this statement cannot be stored in the remote server
	 */
	boolean isIllegal(Resource subj, URI pred, Value obj, Resource... contexts) {
		return cache.isIllegal(subj, pred, obj, contexts);
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern (or super set) has no matches.
	 */
	boolean noMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return cache.noMatch(subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Uses cache of given pattern or super patterns before loading size.
	 */
	long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return cache.size(subj, pred, obj, includeInferred, contexts);
	}
}
