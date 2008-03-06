/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Literal;
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
 * @author Arjohn Kampman
 */
public class HTTPRepository implements Repository {

	/*-----------*
	 * Constants *
	 *-----------*/

	final Logger logger = LoggerFactory.getLogger(this.getClass());

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

	public HTTPRepository(String serverURL, String repositoryID) {
		_httpClient = new HTTPClient();
		_httpClient.setValueFactory(new ValueFactoryImpl());
		_httpClient.setServerURL(serverURL);
		_httpClient.setRepositoryID(repositoryID);
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
		String repositoryID = _httpClient.getRepositoryID();

		try {
			TupleQueryResult repositoryList = _httpClient.getRepositoryList();
			try {
				while(repositoryList.hasNext()) {
					BindingSet bindingSet = repositoryList.next();
					Value id = bindingSet.getValue("id");

					if (id instanceof Literal && ((Literal)id).getLabel().equals(repositoryID)) {
						Value writable = bindingSet.getValue("writable");
						if (writable instanceof Literal) {
							isWritable = Boolean.parseBoolean(((Literal)writable).getLabel());
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
