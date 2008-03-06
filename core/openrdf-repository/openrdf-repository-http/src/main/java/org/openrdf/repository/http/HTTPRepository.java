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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * A repository that serves as a proxy for a remote repository on a Sesame
 * server.
 * 
 * @author Arjohn Kampman
 */
public class HTTPRepository implements Repository {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private HTTPClient _httpClient;

	private File _dataDir;

	private boolean _initialized = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	private HTTPRepository() {
		_httpClient = new HTTPClient();
		_httpClient.setValueFactory(new ValueFactoryImpl());
	}

	public HTTPRepository(String serverURL, String repositoryID) {
		this();
		_httpClient.setServerURL(serverURL);
		_httpClient.setRepositoryID(repositoryID);
	}

	public HTTPRepository(String repositoryURL) {
		this();
		_httpClient.setRepositoryURL(repositoryURL);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// _httpClient is shared with HTTPConnection
	HTTPClient getHTTPClient() {
		return _httpClient;
	}

	public void setDataDir(File dataDir) {
		_dataDir = dataDir;
	}

	public File getDataDir() {
		return _dataDir;
	}

	public void initialize()
		throws RepositoryException
	{
		_initialized = true;
	}

	public void shutDown()
		throws RepositoryException
	{
		_initialized = false;
	}

	public ValueFactory getValueFactory() {
		return _httpClient.getValueFactory();
	}

	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new HTTPRepositoryConnection(this);
	}

	public boolean isWritable()
		throws RepositoryException
	{
		if (!_initialized) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}

		boolean isWritable = false;
		String repositoryURL = _httpClient.getRepositoryURL();

		try {
			TupleQueryResult repositoryList = _httpClient.getRepositoryList();
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
}
