/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;

import org.openrdf.http.client.RepositoryClient;
import org.openrdf.http.client.SesameClient;
import org.openrdf.model.LiteralFactory;
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
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.StoreException;

/**
 * A repository that serves as a proxy for a remote repository on a Sesame
 * server.
 * 
 * Methods in this class may throw the specific StoreException subclasses
 * UnautorizedException and NotAllowedException, the semantics of which are
 * defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author Herko ter Horst
 */
public class HTTPRepository implements Repository {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private SesameClient server;

	private RepositoryClient client;

	private File dataDir;

	private boolean initialized = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepository(String serverURL, String repositoryID) {
		server = new SesameClient(serverURL);
		client = server.repositories().slash(repositoryID);
	}

	public HTTPRepository(String repositoryURL) {
		client = new RepositoryClient(repositoryURL);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// httpClient is shared with HTTPConnection
	RepositoryClient getClient() {
		return client;
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
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
		return client.getValueFactory();
	}

	public URIFactory getURIFactory() {
		return client.getValueFactory();
	}

	public ValueFactory getValueFactory() {
		return client.getValueFactory();
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

		TupleQueryResult repositoryList = server.repositories().get();
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
	 * supplied {@link TupleQueryResultFormat}, overriding the
	 * default preference. Setting this parameter is not
	 * necessary in most cases as the default indicates a
	 * preference for the most compact and efficient format available.
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
	 * {@link RDFFormat}, overriding the default
	 * preference. Setting this parameter is not necessary in most cases as the
	 * default indicates a preference for the most compact
	 * and efficient format available.
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
}
