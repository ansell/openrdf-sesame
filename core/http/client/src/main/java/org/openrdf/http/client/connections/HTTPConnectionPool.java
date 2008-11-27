/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.connections;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * Store the url, authentication, preference, and a shared {@link HttpClient}
 * for managed {@link HTTPConnection}.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class HTTPConnectionPool implements Cloneable {

	private final Logger logger = LoggerFactory.getLogger(HTTPConnectionPool.class);

	private ValueFactory valueFactory;

	private HttpClient httpClient;

	private String url;

	private AuthScope authScope;

	private TupleQueryResultFormat preferredTQRFormat = TupleQueryResultFormat.BINARY;

	private BooleanQueryResultFormat preferredBQRFormat = BooleanQueryResultFormat.TEXT;

	private RDFFormat preferredRDFFormat = RDFFormat.TURTLE;

	public HTTPConnectionPool(String url) {
		this();
		if (url == null) {
			throw new IllegalArgumentException("serverURL must not be null");
		}

		this.url = url;
	}

	public HTTPConnectionPool() {
		valueFactory = ValueFactoryImpl.getInstance();

		// Use MultiThreadedHttpConnectionManager to allow concurrent access on
		// HttpClient
		HttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

		// Allow 20 concurrent connections to the same host (default is 2)
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(20);
		manager.setParams(params);

		httpClient = new HttpClient(manager);
	}

	public String getURL() {
		return url;
	}

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	/**
	 * Sets the preferred format for encoding tuple query results. The
	 * {@link TupleQueryResultFormat#BINARY binary} format is preferred by
	 * default.
	 * 
	 * @param format
	 *        The preferred {@link TupleQueryResultFormat}, or <tt>null</tt> to
	 *        indicate no specific format is preferred.
	 */
	public void setPreferredTupleQueryResultFormat(TupleQueryResultFormat format) {
		preferredTQRFormat = format;
	}

	/**
	 * Gets the preferred {@link TupleQueryResultFormat} for encoding tuple query
	 * results.
	 * 
	 * @return The preferred format, of <tt>null</tt> if no specific format is
	 *         preferred.
	 */
	public TupleQueryResultFormat getPreferredTupleQueryResultFormat() {
		return preferredTQRFormat;
	}

	/**
	 * Sets the preferred format for encoding RDF documents. The
	 * {@link RDFFormat#TURTLE Turtle} format is preferred by default.
	 * 
	 * @param format
	 *        The preferred {@link RDFFormat}, or <tt>null</tt> to indicate no
	 *        specific format is preferred.
	 */
	public void setPreferredRDFFormat(RDFFormat format) {
		preferredRDFFormat = format;
	}

	/**
	 * Gets the preferred {@link RDFFormat} for encoding RDF documents.
	 * 
	 * @return The preferred format, of <tt>null</tt> if no specific format is
	 *         preferred.
	 */
	public RDFFormat getPreferredRDFFormat() {
		return preferredRDFFormat;
	}

	/**
	 * Sets the preferred format for encoding boolean query results. The
	 * {@link BooleanQueryResultFormat#TEXT binary} format is preferred by
	 * default.
	 * 
	 * @param format
	 *        The preferred {@link BooleanQueryResultFormat}, or <tt>null</tt> to
	 *        indicate no specific format is preferred.
	 */
	public void setPreferredBooleanQueryResultFormat(BooleanQueryResultFormat format) {
		preferredBQRFormat = format;
	}

	/**
	 * Gets the preferred {@link BooleanQueryResultFormat} for encoding boolean
	 * query results.
	 * 
	 * @return The preferred format, of <tt>null</tt> if no specific format is
	 *         preferred.
	 */
	public BooleanQueryResultFormat getPreferredBooleanQueryResultFormat() {
		return preferredBQRFormat;
	}

	/**
	 * Set the username and password for authenication with the remote server.
	 * 
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 */
	public void setUsernameAndPassword(String username, String password) {
		if (url == null) {
			throw new IllegalStateException("URL has not been set");
		}

		if (username != null && password != null) {
			logger.debug("Setting username '{}' and password for server at {}.", username, url);
			try {
				URL server = new URL(url);
				authScope = new AuthScope(server.getHost(), AuthScope.ANY_PORT);
				httpClient.getState().setCredentials(authScope,
						new UsernamePasswordCredentials(username, password));
				httpClient.getParams().setAuthenticationPreemptive(true);
			}
			catch (MalformedURLException e) {
				logger.warn("Unable to set username and password for malformed URL {}", url);
			}
		}
		else {
			authScope = null;
			httpClient.getState().clearCredentials();
			httpClient.getParams().setAuthenticationPreemptive(false);
		}
	}

	public HTTPConnectionPool slash(String path) {
		try {
			if (url == null) {
				throw new IllegalStateException("URL has not been set");
			}
			HTTPConnectionPool clone = (HTTPConnectionPool)clone();
			clone.url = url + "/" + path;
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	public HTTPConnection head() {
		// Allow HEAD request to send a message body
		HttpMethod method = new PostMethod(url) {

			@Override
			public String getName() {
				return "HEAD";
			}
		};
		setDoAuthentication(method);
		return new HTTPConnection(this, method);
	}

	public HTTPConnection get() {
		HttpMethod method = new PostMethod(url) {

			@Override
			public String getName() {
				return "GET";
			}
		};
		setDoAuthentication(method);
		return new HTTPConnection(this, method);
	}

	public HTTPConnection post() {
		PostMethod method = new PostMethod(url);
		setDoAuthentication(method);
		return new HTTPConnection(this, method);
	}

	public HTTPConnection put() {
		PutMethod method = new PutMethod(url);
		setDoAuthentication(method);
		return new HTTPConnection(this, method);
	}

	public HTTPConnection delete() {
		DeleteMethod method = new DeleteMethod(url);
		setDoAuthentication(method);
		return new HTTPConnection(this, method);
	}

	int executeMethod(HttpMethod method)
		throws IOException
	{
		return httpClient.executeMethod(method);
	}

	protected final void setDoAuthentication(HttpMethod method) {
		if (authScope != null && httpClient.getState().getCredentials(authScope) != null) {
			method.setDoAuthentication(true);
		}
		else {
			method.setDoAuthentication(false);
		}
	}

}
