/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.protocol.rest.Protocol;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultParseException;
import org.openrdf.queryresult.impl.TupleQueryResultBuilder;
import org.openrdf.queryresult.xml.SPARQLResultsXMLParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.helpers.SailGraphQueryResult;
import org.openrdf.util.iterator.IteratorWrapper;
import org.openrdf.util.log.ThreadLog;

/**
 * 
 */
public class HTTPSail implements Sail {

	static Logger log = Logger.getLogger(HTTPSail.class.getName());

	/**
	 * the http address of the server
	 */
	private String _serverUrl;

	/**
	 * repository id of the current repository
	 */
	private String _repositoryId;

	private String _userName;

	private String _password;

	/**
	 * @see org.openrdf.sesame.sail.Sail#getValueFactory()
	 */
	private ValueFactoryImpl _valueFactory;

	/**
	 * flag identifying if Sail has been configured
	 */
	private boolean _initialized;

	private List<SailChangedListener> _listeners;

	private Boolean _isWritable = null;

	public HTTPSail() {
		this(null, null, null, null);
	}

	/**
	 * constructor
	 */
	public HTTPSail(String serverUrl, String userName, String passwd,
			String repositoryId) {
		_serverUrl = serverUrl;
		_repositoryId = repositoryId;
		_password = passwd;
		_userName = userName;
		_listeners = new Vector<SailChangedListener>();
	}

	/**
	 * this method sets the urls for repository list and for the specified
	 * repository id. It checks whether id is valid otherwise it asks for an
	 * alternative id. It sets the initialized flag to indicate whether
	 * initialization was successful.
	 * 
	 * @see org.openrdf.sesame.sail.Sail#initialize()
	 * 
	 * @throws SailInitializationException
	 *             initialization failed
	 */
	public void initialize() throws SailInitializationException {
		if (_serverUrl == null | _repositoryId == null) {
			_initialized = false;
			throw new SailInitializationException(
					"Needed server url and repository id are not provided");
		}
		boolean serverResponded = _checkServer();
		if (!serverResponded) {
			_initialized = false;
			throw new SailInitializationException(
					"Server did not respond (url: " + _serverUrl + ")");
		}
		// is this a valid repository id?
		boolean valid = _checkRepositoryByID(_repositoryId);
		if (!valid) {
			// if the specified repository is not available, simply throw an
			// error. Don't
			// start guessing.
			ThreadLog
					.error("Sail initialization failed, specified repository not available: "
							+ _repositoryId);
			throw new SailInitializationException(
					"Specified repository not available: " + _repositoryId);
		}
		_initialized = true;
	}
	
	public void shutDown() {
		if (_initialized) {
			_valueFactory = null;
			_initialized = false;
		}
	}

	public void addSailChangedListener(SailChangedListener listener) {
		_listeners.add(listener);
	}

	public void fireSailChangedEvent(SailChangedEvent evt) {
		for (SailChangedListener listener : _listeners) {
			listener.sailChanged(evt);
		}
	}

	public void clear() throws SailException {
		if (!_initialized) {
			throw new IllegalStateException("HTTPSail not initialized.");
		}
		HTTPSailConnection ta = (HTTPSailConnection) getConnection();
		ta.clear();
	}

	public ValueFactory getValueFactory() {
		if (_valueFactory == null) {
			_valueFactory = new ValueFactoryImpl();
		}
		return _valueFactory;
	}

	/**
	 * get the read/write access parameters for the current repository and
	 * decide if the sail object is writable or not. Therefore get the list of
	 * all repositories and extract the relevant infos for the current
	 * repository.
	 * 
	 * @return data in this sail object writable?
	 */
	public boolean isWritable() {
		if (!_initialized) {
			throw new IllegalStateException("HTTPSail not initialized.");
		}
		if (_isWritable != null) {
			return _isWritable;
		}
		_isWritable = false;
		try {
			String repositoryListLocation = Protocol.getRepositoriesLocation(_serverUrl);
			log.finer("getting repository list from " + repositoryListLocation);
			GetMethod get = new GetMethod(repositoryListLocation);
			TupleQueryResult result = _getSparqlQueryResult(get);
			if (result != null) {
				// parse the result
				Iterator<Solution> solutionIter = result.iterator();
				// iterate repository list
				while (solutionIter.hasNext()) {
					Solution solution = solutionIter.next();
					String uri = solution.getBinding("uri").getValue()
							.toString();
					String wstring = solution.getValue("writeable").toString();
					log
							.finest("parsing repo " + uri + " writeable: "
									+ wstring);
					boolean writeAccess = Boolean.parseBoolean(wstring);
					// this is the repository we're looking for
					if (uri.equals(Protocol.getRepositoryLocation(_serverUrl, _repositoryId))) {
						_isWritable = writeAccess;
						break;
					}
				}

			} else
				throw new Exception("Canot get results from server: null");
		} catch (Exception exception) {
			log.log(Level.WARNING,
					"Cannot detect whether repository is writable: "
							+ exception, exception);
		}

		return _isWritable;
	}

	public void removeSailChangedListener(SailChangedListener listener) {
		_listeners.remove(listener);
	}

	public SailConnection getConnection() throws SailException {
		if (!isWritable()) {
			throw new SailException(
					"Unable to start transaction: repository is not writable!");
		}
		if (!_initialized) {
			throw new IllegalStateException("HTTPSail not initialized.");
		}
		return new HTTPSailConnection(this);
	}

	public String getRepositoryId() {
		return _repositoryId;
	}

	/**
	 * @return server address (URL)
	 */
	public String getServerUrl() {
		return _serverUrl;
	}

	public void setParameter(String key, String value) {
		if (_initialized) {
			throw new IllegalStateException("sail has already been intialized");
		}
		if (key == null | value == null) {
			throw new IllegalArgumentException(
					"Key and value must not be null!");
		}
		if (key.equals("serverurl")) {
			_serverUrl = value;
		} else if (key.equals("repositoryid")) {
			_repositoryId = value;
		} else if (key.equals("username")) {
			_userName = value;
		} else if (key.equals("passwd")) {
			_password = value;
		} else {
			throw new IllegalArgumentException("Key " + key
					+ " is not valid (value: " + value + ")");
		}
	}

	// *************************************************************************
	// 
	// private helper methods
	// 
	// *************************************************************************

	/**
	 * method checks whether server is runnig, i.e. whether server url is valid.
	 * Therefor it tries to get the repository list.
	 * 
	 */
	private boolean _checkServer() {
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(Protocol.getRepositoriesLocation(_serverUrl));
		// result is a sparql-document
		get.setRequestHeader("Accept", "application/sparql-results+xml");
		try {
			// send the request
			int http_code = client.executeMethod(get);
			if (http_code == HttpURLConnection.HTTP_OK)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if repository id is valid
	 * 
	 * @param repositoryId
	 *            repository id
	 * @return repository availability
	 */
	private boolean _checkRepositoryByID(String repositoryId) {
		boolean ok;
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(Protocol.getRepositoryLocation(_serverUrl, repositoryId));
		get.getParams().setParameter(Protocol.ACCEPT_PARAM_NAME, "application/rdf+xml");
		try {
			int http_code = client.executeMethod(get);
			if (http_code == HttpURLConnection.HTTP_OK)
				ok = true;
			else
				ok = false;
		} catch (Exception e) {
			ok = false;
		}
		return ok;
	}

	/**
	 * proceeds url (get method) which expects a sparql result document
	 */
	TupleQueryResult _getSparqlQueryResult(GetMethod get) throws IOException,
			TupleQueryResultParseException, TupleQueryResultHandlerException {
		TupleQueryResult result = null;
		HttpClient client = new HttpClient();

		int http_code = client.executeMethod(get);
		// content available
		if (http_code == HttpURLConnection.HTTP_OK) {
			// parse the result
			SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
			TupleQueryResultBuilder handler = new TupleQueryResultBuilder();
			parser.setTupleQueryResultHandler(handler);
			parser.parse(get.getResponseBodyAsStream());
			result = handler.getQueryResult();
			get.releaseConnection();
			return result;
		} else {
			get.releaseConnection();
			log.fine("Error querying, dumping response body");
			log.fine(get.getResponseBodyAsString());
			throw new TupleQueryResultParseException("http code for "
					+ get.getURI() + " is " + " not 200, but " + http_code);
		}

	}

	GraphQueryResult _getGraphQueryResult(GetMethod get, RDFFormat format)
			throws IOException, TupleQueryResultParseException,
			TupleQueryResultHandlerException {

		GraphQueryResult result = null;
		HttpClient client = new HttpClient();

		int httpCode = client.executeMethod(get);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			try {
				RDFParser parser = Rio.createParser(format);
				StatementCollector collector = new StatementCollector();
				parser.setRDFHandler(collector);
				parser.parse(get.getResponseBodyAsStream(), "");

				result = new SailGraphQueryResult(null,
						new IteratorWrapper<Statement>(collector
								.getStatements().iterator()) {
						});

			} catch (UnsupportedRDFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RDFParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			get.releaseConnection();
			log.fine("Error querying, dumping response body");
			log.fine(get.getResponseBodyAsString());
			throw new TupleQueryResultParseException("http code for "
					+ get.getURI() + " is " + " not 200, but " + httpCode);
		}
		return result;
	}
}
