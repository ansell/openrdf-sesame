/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.connections;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.StoreException;

/**
 * Store the url, authentication, preference, and a shared {@link HttpClient}
 * for managed {@link HTTPRequest}.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public final class HTTPConnectionPool implements Cloneable {

	private final Logger logger = LoggerFactory.getLogger(HTTPConnectionPool.class);

	private final ValueFactory valueFactory;

	private final MultiThreadedHttpConnectionManager manager;

	final HttpClient httpClient;

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final String serverURL;

	private String url;

	private AuthScope authScope;

	private TupleQueryResultFormat preferredTQRFormat = TupleQueryResultFormat.BINARY;

	private BooleanQueryResultFormat preferredBQRFormat = BooleanQueryResultFormat.TEXT;

	private RDFFormat preferredRDFFormat = RDFFormat.TURTLE;

	private boolean preferredTQRFormatUsed;

	private boolean preferredBQRFormatUsed;

	private boolean preferredRDFFormatUsed;

	public HTTPConnectionPool(String url) {
		this(url, ValueFactoryImpl.getInstance());
	}

	public HTTPConnectionPool(String url, ValueFactory valueFactory) {
		if (url == null) {
			throw new IllegalArgumentException("serverURL must not be null");
		}
		if (valueFactory == null) {
			throw new IllegalArgumentException("valueFactory must not be null");
		}

		this.valueFactory = valueFactory;
		this.url = serverURL = url;

		// Use MultiThreadedHttpConnectionManager to allow concurrent access on
		// HttpClient
		manager = new MultiThreadedHttpConnectionManager();

		// Allow 20 concurrent connections to the same host (default is 2)
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(20);
		// TODO 20% speed up by params.setStaleCheckingEnabled(false);
		manager.setParams(params);

		// No automatic handling of cookies, we handle sesame's session cookies
		// manually
		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		httpClient = new HttpClient(clientParams, manager);
	}

	public String getServerURL() {
		return serverURL;
	}

	public String getURL() {
		return url;
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

	public boolean isPreferredTupleQueryResultFormatUsed() {
		return preferredTQRFormatUsed;
	}

	public boolean isPreferredBooleanQueryResultFormatUsed() {
		return preferredBQRFormatUsed;
	}

	public boolean isPreferredRDFFormatUsed() {
		return preferredRDFFormatUsed;
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
		if (username != null && password != null) {
			logger.debug("Setting username '{}' and password for server at {}.", username, serverURL);
			try {
				URL server = new URL(serverURL);
				authScope = new AuthScope(server.getHost(), AuthScope.ANY_PORT);
				httpClient.getState().setCredentials(authScope,
						new UsernamePasswordCredentials(username, password));
				httpClient.getParams().setAuthenticationPreemptive(true);
			}
			catch (MalformedURLException e) {
				logger.warn("Unable to set username and password for malformed URL {}", serverURL);
			}
		}
		else {
			authScope = null;
			httpClient.getState().clearCredentials();
			httpClient.getParams().setAuthenticationPreemptive(false);
		}
	}

	public void shutdown() {
		executor.shutdown();
		manager.shutdown();
	}

	public HTTPConnectionPool slash(String path) {
		if (path == null) {
			throw new IllegalArgumentException("path must not be null");
		}
		return location(url + "/" + path);
	}

	public HTTPConnectionPool location(String url) {
		if (url == null) {
			throw new IllegalArgumentException("url must not be null");
		}
		try {
			HTTPConnectionPool clone = (HTTPConnectionPool)clone();
			clone.url = url;
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	public HTTPRequest head() {
		// FIXME: Allow HEAD request to send a message body?
		HttpMethodBase method = new PostMethod(url) {

			@Override
			public String getName() {
				return "HEAD";
			}
		};
		setDoAuthentication(method);
		return new HTTPRequest(this, method);
	}

	public HTTPRequest get() {
		// FIXME: Allow GET request to send a message body?
		HttpMethodBase method = new PostMethod(url) {

			@Override
			public String getName() {
				return "GET";
			}
		};
		setDoAuthentication(method);
		return new HTTPRequest(this, method);
	}

	public HTTPRequest post() {
		PostMethod method = new PostMethod(url);
		setDoAuthentication(method);
		return new HTTPRequest(this, method);
	}

	public HTTPRequest put() {
		PutMethod method = new PutMethod(url);
		setDoAuthentication(method);
		return new HTTPRequest(this, method);
	}

	public HTTPRequest delete() {
		DeleteMethod method = new DeleteMethod(url);
		setDoAuthentication(method);
		return new HTTPRequest(this, method);
	}

	public <V> Future<V> submitTask(final Callable<V> task) {
		return executor.submit(new Callable<V>() {

			public V call()
				throws StoreException
			{
				try {
					return task.call();
				}
				catch (StoreException e) {
					List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
					stack.addAll(Arrays.asList(e.getStackTrace()));
					stack.addAll(Arrays.asList(new Throwable().getStackTrace()));
					e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
					throw e;
				}
				catch (RuntimeException e) {
					List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
					stack.addAll(Arrays.asList(e.getStackTrace()));
					stack.addAll(Arrays.asList(new Throwable().getStackTrace()));
					e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
					throw e;
				}
				catch (Exception e) {
					throw new StoreException(e);
				}
			}
		});
	}

	void executeTask(Runnable task) {
		executor.execute(task);
	}

	int executeMethod(HttpMethod method)
		throws IOException
	{
		int status = httpClient.executeMethod(method);
		if (!preferredBQRFormatUsed || !preferredRDFFormatUsed || !preferredTQRFormatUsed) {
			Header header = method.getResponseHeader("Content-Type");
			if (header != null) {
				String mimetype = header.getValue();
				if (preferredBQRFormat.getDefaultMIMEType().equals(mimetype)) {
					preferredBQRFormatUsed = true;
				}
				if (preferredRDFFormat.getDefaultMIMEType().equals(mimetype)) {
					preferredRDFFormatUsed = true;
				}
				if (preferredTQRFormat.getDefaultMIMEType().equals(mimetype)) {
					preferredTQRFormatUsed = true;
				}
			}
		}
		return status;
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
