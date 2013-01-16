/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryBase;
import org.openrdf.rio.RDFFormat;

/**
 * A repository that serves as a proxy for a remote repository on a Sesame
 * server. Methods in this class may throw the specific RepositoryException
 * subclass UnautorizedException, the semantics of which is defined by the HTTP
 * protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Arjohn Kampman
 * @author jeen
 * @author Herko ter Horst
 */
public class HTTPRepository extends RepositoryBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private final HTTPClient httpClient;

	private File dataDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	private HTTPRepository() {
		super();
		httpClient = new HTTPClient();
		httpClient.setValueFactory(new ValueFactoryImpl());
	}

	public HTTPRepository(final String serverURL, final String repositoryID) {
		this();
		httpClient.setServerURL(serverURL);
		httpClient.setRepositoryID(repositoryID);
	}

	public HTTPRepository(final String repositoryURL) {
		this();
		httpClient.setRepositoryURL(repositoryURL);
	}

	/* ---------------*
	 * public methods *
	 * ---------------*/

	public void setDataDir(final File dataDir) {
		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
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
		if (!isInitialized()) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}

		boolean isWritable = false;
		final String repositoryURL = httpClient.getRepositoryURL();

		try {
			final TupleQueryResult repositoryList = httpClient.getRepositoryList();
			try {
				while (repositoryList.hasNext()) {
					final BindingSet bindingSet = repositoryList.next();
					final Value uri = bindingSet.getValue("uri");

					if (uri != null && uri.stringValue().equals(repositoryURL)) {
						isWritable = LiteralUtil.getBooleanValue(bindingSet.getValue("writable"), false);
						break;
					}
				}
			}
			finally {
				repositoryList.close();
			}
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}

		return isWritable;
	}

	/**
	 * Sets the preferred serialization format for tuple query results to the
	 * supplied {@link TupleQueryResultFormat}, overriding the {@link HTTPClient}
	 * 's default preference. Setting this parameter is not necessary in most
	 * cases as the {@link HTTPClient} by default indicates a preference for the
	 * most compact and efficient format available.
	 * 
	 * @param format
	 *        the preferred {@link TupleQueryResultFormat}. If set to 'null' no
	 *        explicit preference will be stated.
	 */
	public void setPreferredTupleQueryResultFormat(final TupleQueryResultFormat format) {
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
	 * {@link RDFFormat}, overriding the {@link HTTPClient}'s default preference.
	 * Setting this parameter is not necessary in most cases as the
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
	public void setPreferredRDFFormat(final RDFFormat format) {
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
	public void setUsernameAndPassword(final String username, final String password) {
		httpClient.setUsernameAndPassword(username, password);
	}

	public String getRepositoryURL() {
		return this.httpClient.getRepositoryURL();
	}

	/* -------------------*
	 * non-public methods *
	 * -------------------*/

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		// empty implementation of abstract superclass method
	}

	protected void shutDownInternal()
		throws RepositoryException
	{
		// httpclient shutdown moved to finalize method, to avoid problems with
		// shutdown followed by re-initialization. See SES-1059.
		// httpClient.shutDown();
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		httpClient.shutDown();
		super.finalize();
	}

	// httpClient is shared with HTTPConnection
	HTTPClient getHTTPClient() {
		return httpClient;
	}
}
