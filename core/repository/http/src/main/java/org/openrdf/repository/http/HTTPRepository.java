/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.SesameClientImpl;
import org.openrdf.http.client.SesameSession;
import org.openrdf.http.client.SparqlSession;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Literals;
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
 * @author Jeen Broekstra
 * @author Herko ter Horst
 */
public class HTTPRepository extends RepositoryBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private SesameClient client;

	private String username;

	private String password;

	private String serverURL;

	private String repositoryURL;

	private RDFFormat rdfFormat;

	private TupleQueryResultFormat tupleFormat;

	private File dataDir;

	private Boolean compatibleMode = null;

	/*--------------*
	 * Constructors *
	 *--------------*/

	private HTTPRepository() {
		super();
	}

	public HTTPRepository(final String serverURL, final String repositoryID) {
		this();
		this.serverURL = serverURL;
		this.repositoryURL = Protocol.getRepositoryLocation(serverURL, repositoryID);
	}

	public HTTPRepository(final String repositoryURL) {
		this();
		// Try to parse the server URL from the repository URL
		Pattern urlPattern = Pattern.compile("(.*)/" + Protocol.REPOSITORIES + "/[^/]*/?");
		Matcher matcher = urlPattern.matcher(repositoryURL);

		if (matcher.matches() && matcher.groupCount() == 1) {
			this.serverURL = matcher.group(1);
		}
		else {
			throw new IllegalArgumentException("URL must be to a Sesame Repository (not just the server)");
		}
		this.repositoryURL = repositoryURL;
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

	public synchronized SesameClient getSesameClient() {
		if (client == null) {
			client = new SesameClientImpl();
		}
		return client;
	}

	public synchronized void setSesameClient(SesameClient client) {
		this.client = client;
	}

	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new HTTPRepositoryConnection(this, createHTTPClient());
	}

	public boolean isWritable()
		throws RepositoryException
	{
		if (!isInitialized()) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}

		boolean isWritable = false;
		final String repositoryURL = createHTTPClient().getRepositoryURL();

		try {
			final TupleQueryResult repositoryList = createHTTPClient().getRepositoryList();
			try {
				while (repositoryList.hasNext()) {
					final BindingSet bindingSet = repositoryList.next();
					final Value uri = bindingSet.getValue("uri");

					if (uri != null && uri.stringValue().equals(repositoryURL)) {
						isWritable = Literals.getBooleanValue(bindingSet.getValue("writable"), false);
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
	 * supplied {@link TupleQueryResultFormat}, overriding the
	 * {@link SparqlSession} 's default preference. Setting this parameter is not
	 * necessary in most cases as the {@link SparqlSession} by default indicates
	 * a preference for the most compact and efficient format available.
	 * 
	 * @param format
	 *        the preferred {@link TupleQueryResultFormat}. If set to 'null' no
	 *        explicit preference will be stated.
	 */
	public void setPreferredTupleQueryResultFormat(final TupleQueryResultFormat format) {
		this.tupleFormat = format;
	}

	/**
	 * Indicates the current preferred {@link TupleQueryResultFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public TupleQueryResultFormat getPreferredTupleQueryResultFormat() {
		return tupleFormat;
	}

	/**
	 * Sets the preferred serialization format for RDF to the supplied
	 * {@link RDFFormat}, overriding the {@link SparqlSession}'s default
	 * preference. Setting this parameter is not necessary in most cases as the
	 * {@link SparqlSession} by default indicates a preference for the most
	 * compact and efficient format available.
	 * <p>
	 * Use with caution: if set to a format that does not support context
	 * serialization any context info contained in the query result will be lost.
	 * 
	 * @param format
	 *        the preferred {@link RDFFormat}. If set to 'null' no explicit
	 *        preference will be stated.
	 */
	public void setPreferredRDFFormat(final RDFFormat format) {
		this.rdfFormat = format;
	}

	/**
	 * Indicates the current preferred {@link RDFFormat}.
	 * 
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public RDFFormat getPreferredRDFFormat() {
		return rdfFormat;
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
		this.username = username;
		this.password = password;
	}

	public String getRepositoryURL() {
		return repositoryURL;
	}

	/* -------------------*
	 * non-public methods *
	 * -------------------*/

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		// no-op
	}

	protected void shutDownInternal()
		throws RepositoryException
	{
		if (client != null) {
			client.shutDown();
			client = null;
		}
	}

	/**
	 * Creates a new HTTPClient object. Subclasses may override to return a more
	 * specific HTTPClient subtype.
	 * 
	 * @return a HTTPClient object.
	 */
	protected SesameSession createHTTPClient() {
		// initialize HTTP client
		SesameSession httpClient = getSesameClient().createSesameSession(serverURL);
		httpClient.setValueFactory(ValueFactoryImpl.getInstance());
		if (repositoryURL != null) {
			httpClient.setRepository(repositoryURL);
		}
		if (tupleFormat != null) {
			httpClient.setPreferredTupleQueryResultFormat(tupleFormat);
		}
		if (rdfFormat != null) {
			httpClient.setPreferredRDFFormat(rdfFormat);
		}
		if (username != null) {
			httpClient.setUsernameAndPassword(username, password);
		}
		return httpClient;
	}

	/**
	 * Verify if transaction handling should be done in backward-compatible mode
	 * (this is the case when communicating with an older Sesame Server).
	 * 
	 * @return <code>true</code> if the Server does not support the extended transaction
	 *         protocol, <code>false</code> otherwise.
	 * @throws RepositoryException
	 *         if something went wrong while querying the server for the protocol
	 *         version.
	 */
	synchronized boolean useCompatibleMode()
		throws RepositoryException
	{
		if (compatibleMode == null) {
			try {
				final String serverProtocolVersion = createHTTPClient().getServerProtocol();

				// protocol version 7 supports the new transaction handling. If
				// the server is older, we need to run in backward-compatible mode.
				this.compatibleMode = (Integer.parseInt(serverProtocolVersion) < 7);
			}
			catch (NumberFormatException e) {
				throw new RepositoryException("could not read protocol version from server: ", e);
			}
			catch (IOException e) {
				throw new RepositoryException("could not read protocol version from server: ", e);
			}
		}
		return compatibleMode;
	}
}
