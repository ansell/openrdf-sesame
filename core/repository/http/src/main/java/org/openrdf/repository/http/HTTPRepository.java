/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import static org.openrdf.http.protocol.Protocol.MIN_TIME_OUT;
import static org.openrdf.http.protocol.Protocol.TIME_OUT_UNITS;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.client.ConnectionClient;
import org.openrdf.http.client.RepositoryClient;
import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.model.impl.LiteralFactoryImpl;
import org.openrdf.model.impl.URIFactoryImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryMetaData;
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

	Logger logger = LoggerFactory.getLogger(HTTPRepository.class);

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	private URIFactory uf = new URIFactoryImpl();

	private LiteralFactory lf = new LiteralFactoryImpl();

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private RepositoryClient client;

	private RepositoryCache cache;

	private File dataDir;

	private boolean initialized = false;

	private RepositoryMetaData metadata;

	private HTTPConnectionPool pool;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepository(String serverURL, String repositoryID) {
		pool = new HTTPConnectionPool(serverURL);
		pool.setValueFactory(new ValueFactoryImpl(new BNodeFactoryImpl(), uf, lf));
		client = new SesameClient(pool).repositories().slash(repositoryID);
		cache = new RepositoryCache(client);
	}

	public HTTPRepository(String repositoryURL) {
		pool = new HTTPConnectionPool(repositoryURL);
		pool.setValueFactory(new ValueFactoryImpl(new BNodeFactoryImpl(), uf, lf));
		client = new RepositoryClient(pool);
		cache = new RepositoryCache(client);
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
		pool.shutdown();
		executor.shutdown();
	}

	public URIFactory getURIFactory() {
		return uf;
	}

	public LiteralFactory getLiteralFactory() {
		return lf;
	}

	public ValueFactory getValueFactory() {
		return new ValueFactoryImpl(uf, lf);
	}

	public RepositoryConnection getConnection()
		throws StoreException
	{
		final ConnectionClient connection = client.connections().post();
		final HTTPRepositoryConnection con = new HTTPRepositoryConnection(this, connection);
		executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				try {
					if (con.isOpen()) {
						connection.ping();
					} else {
						throw new RuntimeException("connection already closed");
					}
				} catch (StoreException e) {
					logger.warn(e.toString());
					throw new RuntimeException(e);
				}
			}
		}, MIN_TIME_OUT, MIN_TIME_OUT, TIME_OUT_UNITS);
		return con;
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
		pool.setPreferredTupleQueryResultFormat(format);
	}

	/**
	 * Indicates the current preferred {@link TupleQueryResultFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public TupleQueryResultFormat getPreferredTupleQueryResultFormat() {
		return pool.getPreferredTupleQueryResultFormat();
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
		pool.setPreferredRDFFormat(format);
	}

	/**
	 * Indicates the current preferred {@link RDFFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public RDFFormat getPreferredRDFFormat() {
		return pool.getPreferredRDFFormat();
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
		pool.setUsernameAndPassword(username, password);
	}

	@Override
	public String toString() {
		return client.toString();
	}

	/**
	 * Indicates that the cache needs validation.
	 */
	void modified() {
		cache.modified();
	}

	boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred, Resource[] contexts)
		throws StoreException
	{
		return cache.hasStatement(subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Will never connect to the remote server.
	 * 
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
