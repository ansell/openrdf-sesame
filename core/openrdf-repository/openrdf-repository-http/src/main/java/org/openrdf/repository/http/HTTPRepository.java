/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

/**
 * A repository that serves as a proxy for a remote repository on a Sesame
 * server.
 * 
 * Methods in this class may throw the specific RepositoryException subclasses
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
	private HTTPClient httpClient;

	private File dataDir;

	private boolean initialized = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	private HTTPRepository() {
		httpClient = new HTTPClient();
		httpClient.setValueFactory(new ValueFactoryImpl());
	}

	public HTTPRepository(String serverURL, String repositoryID) {
		this();
		httpClient.setServerURL(serverURL);
		httpClient.setRepositoryID(repositoryID);
	}

	public HTTPRepository(String repositoryURL) {
		this();
		httpClient.setRepositoryURL(repositoryURL);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// httpClient is shared with HTTPConnection
	HTTPClient getHTTPClient() {
		return httpClient;
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
	}

	public void initialize()
		throws RepositoryException
	{
		initialized = true;
	}

	public void shutDown()
		throws RepositoryException
	{
		initialized = false;
	}

	public ValueFactory getValueFactory() {
		return httpClient.getValueFactory();
	}

	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new HTTPRepositoryConnection(this);
	}

	public boolean isWritable()
		throws RepositoryException
	{
		if (!initialized) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}

		boolean isWritable = false;
		String repositoryURL = httpClient.getRepositoryURL();

		try {
			TupleQueryResult repositoryList = httpClient.getRepositoryList();
			try {
				while (repositoryList.hasNext()) {
					BindingSet bindingSet = repositoryList.next();
					Value uri = bindingSet.getValue("uri");

					if (uri instanceof URI && ((URI)uri).toString().equals(repositoryURL)) {
						Value writable = bindingSet.getValue("writable");
						if (writable instanceof Literal) {
							isWritable = ((Literal)writable).booleanValue();
						}

						break;
					}
				}
			}
			catch (QueryEvaluationException e) {
				throw new RepositoryException(e);
			}
			finally {
				try {
					repositoryList.close();
				}
				catch (QueryEvaluationException e) {
					throw new RepositoryException(e);
				}
			}
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}

		return isWritable;
	}

	/**
	 * Sets the preferred serialization format for tuple query results to the
	 * supplied {@link TupleQueryResultFormat}, overriding the
	 * {@link HTTPClient}'s default preference. Setting this parameter is not
	 * necessary in most cases as the {@link HTTPClient} by default indicates a
	 * preference for the most compact and efficient format available.
	 * 
	 * @param format
	 *        the preferred {@link TupleQueryResultFormat}. If set to 'null' no
	 *        explicit preference will be stated.
	 */
	public void setPreferredTupleQueryResultFormat(TupleQueryResultFormat format) {
		httpClient.setPreferredTupleQueryResultFormat(format);
	}

	/**
	 * Indicates the current preferred {@link TupleQueryResultFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public TupleQueryResultFormat getPreferredTupleQueryResultFormat() {
		return httpClient.getPreferredTupleQueryResultFormat();
	}

	/**
	 * Sets the preferred serialization format for RDF to the supplied
	 * {@link RDFFormat}, overriding the {@link HTTPClient}'s default
	 * preference. Setting this parameter is not necessary in most cases as the
	 * {@link HTTPClient} by default indicates a preference for the most compact
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
		httpClient.setPreferredRDFFormat(format);
	}

	/**
	 * Indicates the current preferred {@link RDFFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public RDFFormat getPreferredRDFFormat() {
		return httpClient.getPreferredRDFFormat();
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
		httpClient.setUsernameAndPassword(username, password);
	}
}
