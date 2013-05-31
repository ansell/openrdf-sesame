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
package org.openrdf.http.client;

import static org.openrdf.http.protocol.Protocol.ACCEPT_PARAM_NAME;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.BooleanQueryResultParserRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultParserRegistry;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.ParseErrorLogger;

/**
 * The HTTP client provides low level HTTP methods for the HTTP communication of
 * the SPARQL repository as well as the HTTP Repository. All methods are
 * compliant to the SPARQL 1.1 protocol. For both Tuple and Graph queries there
 * is a variant which parses the result in the background, see
 * {@link BackgroundTupleResult} and {@link BackgroundGraphResult}. For boolean
 * queries the result is parsed in the current thread. All methods in this class
 * guarantee that HTTP connections are closed properly and returned to the
 * connection pool. Functionality specific to the Sesame HTTP protocol can be
 * found in {@link SesameHTTPClient} (which is used by Remote Repositories).
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author Andreas Schwarte
 * @see SesameHTTPClient
 */
public class HTTPClient {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * 
	 */
	private static final Charset UTF8 = Charset.forName("UTF-8");

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory valueFactory;

	private String queryURL;

	private String updateURL;

	private final HttpClient httpClient;

	private final ExecutorService executor;

	private final HttpContext httpContext;

	private final HttpParams params = new BasicHttpParams();

	private ParserConfig parserConfig = new ParserConfig();

	private TupleQueryResultFormat preferredTQRFormat = TupleQueryResultFormat.BINARY;

	private BooleanQueryResultFormat preferredBQRFormat = BooleanQueryResultFormat.TEXT;

	private RDFFormat preferredRDFFormat = RDFFormat.TURTLE;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPClient(HttpClient client, ExecutorService executor) {
		this.httpClient = client;
		this.httpContext = new BasicHttpContext();
		this.executor = executor;
		valueFactory = ValueFactoryImpl.getInstance();
		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
		CookieStore cookieStore = new BasicCookieStore();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

		// parser used for processing server response data should be lenient
		parserConfig.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		parserConfig.addNonFatalError(BasicParserSettings.VERIFY_LANGUAGE_TAGS);
	}

	/*-----------------*
	 * Get/set methods *
	 *-----------------*/

	protected final HttpClient getHttpClient() {
		return httpClient;
	}

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	protected void setQueryURL(String queryURL) {
		if (queryURL == null) {
			throw new IllegalArgumentException("queryURL must not be null");
		}
		this.queryURL = queryURL;
	}

	protected void setUpdateURL(String updateURL) {
		if (updateURL == null) {
			throw new IllegalArgumentException("updateURL must not be null");
		}
		this.updateURL = updateURL;
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
	 * Set the username and password for authentication with the remote server.
	 * 
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 */
	public void setUsernameAndPassword(String username, String password) {
		// checkServerURL();

		if (username != null && password != null) {
			logger.debug("Setting username '{}' and password for server at {}.", username, queryURL);
			java.net.URI requestURI = java.net.URI.create(queryURL);
			String host = requestURI.getHost();
         int port = requestURI.getPort();
         AuthScope scope = new AuthScope(host,port);
			UsernamePasswordCredentials cred = new UsernamePasswordCredentials(username, password);
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(scope, cred);
			httpContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
			params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
		}
		else {
			httpContext.removeAttribute(ClientContext.CREDS_PROVIDER);
		}
	}

	protected void execute(Runnable command) {
		executor.execute(command);
	}

	public String getQueryURL() {
		return queryURL;
	}

	public String getUpdateURL() {
		return updateURL;
	}

	/*------------------*
	 * Query evaluation *
	 *------------------*/

	public TupleQueryResult sendTupleQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		return sendTupleQuery(ql, query, null, dataset, includeInferred, 0, bindings);
	}

	public TupleQueryResult sendTupleQuery(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		HttpUriRequest method = getQueryMethod(ql, query, baseURI, dataset, includeInferred, maxQueryTime, bindings);
		return getBackgroundTupleQueryResult(method);
	}

	public void sendTupleQuery(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, TupleQueryResultHandler handler, Binding... bindings)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException, QueryInterruptedException
	{
		HttpUriRequest method = getQueryMethod(ql, query, baseURI, dataset, includeInferred, maxQueryTime, bindings);
		getTupleQueryResult(method, handler);
	}

	public void sendUpdate(QueryLanguage ql, String update, String baseURI, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		HttpUriRequest method = getUpdateMethod(ql, update, baseURI, dataset, includeInferred, bindings);

		try {
			executeNoContent(method);
		}
		catch (RepositoryException|MalformedQueryException|QueryInterruptedException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	public GraphQueryResult sendGraphQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		return sendGraphQuery(ql, query, null, dataset, includeInferred, 0, bindings);
	}

	public GraphQueryResult sendGraphQuery(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		try {
			HttpUriRequest method = getQueryMethod(ql, query, baseURI, dataset, includeInferred, maxQueryTime,
					bindings);
			return getRDFBackground(method, false);
		}
		catch (RDFHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendGraphQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			RDFHandler handler, Binding... bindings)
		throws IOException, RDFHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException, QueryInterruptedException
	{
		sendGraphQuery(ql, query, null, dataset, includeInferred, 0, handler, bindings);
	}

	public void sendGraphQuery(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, RDFHandler handler, Binding... bindings)
		throws IOException, RDFHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException, QueryInterruptedException
	{
		HttpUriRequest method = getQueryMethod(ql, query, baseURI, dataset, includeInferred, maxQueryTime, bindings);
		getRDF(method, handler, false);
	}

	public boolean sendBooleanQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		return sendBooleanQuery(ql, query, null, dataset, includeInferred, 0, bindings);
	}

	public boolean sendBooleanQuery(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException,
		QueryInterruptedException
	{
		HttpUriRequest method = getQueryMethod(ql, query, baseURI, dataset, includeInferred, maxQueryTime,
				bindings);
		try {
			return getBoolean(method);
		}
		catch (RepositoryException|MalformedQueryException|QueryInterruptedException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	protected HttpUriRequest getQueryMethod(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, Binding... bindings)
	{
		HttpPost method = new HttpPost(getQueryURL());

		method.setHeader("Content-Type", Protocol.FORM_MIME_TYPE + "; charset=utf-8");

		List<NameValuePair> queryParams = getQueryMethodParameters(ql, query, baseURI, dataset,
				includeInferred, maxQueryTime, bindings);

		method.setEntity(new UrlEncodedFormEntity(queryParams, UTF8));

		return method;
	}

	protected HttpUriRequest getUpdateMethod(QueryLanguage ql, String update, String baseURI, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		HttpPost method = new HttpPost(getUpdateURL());

		method.setHeader("Content-Type", Protocol.FORM_MIME_TYPE + "; charset=utf-8");

		List<NameValuePair> queryParams = getUpdateMethodParameters(ql, update, baseURI, dataset,
				includeInferred, bindings);

		method.setEntity(new UrlEncodedFormEntity(queryParams, UTF8));

		return method;
	}

	protected List<NameValuePair> getQueryMethodParameters(QueryLanguage ql, String query, String baseURI,
			Dataset dataset, boolean includeInferred, int maxQueryTime, Binding... bindings)
	{
		// TODO there is a bunch of HttpRepository specific parameters here
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>(bindings.length + 10);

		queryParams.add(new BasicNameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.getName()));
		queryParams.add(new BasicNameValuePair(Protocol.QUERY_PARAM_NAME, query));
		if (baseURI != null) {
			queryParams.add(new BasicNameValuePair(Protocol.BASEURI_PARAM_NAME, baseURI));
		}
		queryParams.add(new BasicNameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME,
				Boolean.toString(includeInferred)));
		if (maxQueryTime > 0) {
			queryParams.add(new BasicNameValuePair(Protocol.TIMEOUT_PARAM_NAME, Integer.toString(maxQueryTime)));
		}

		if (dataset != null) {
			for (URI defaultGraphURI : dataset.getDefaultGraphs()) {
				queryParams.add(new BasicNameValuePair(Protocol.DEFAULT_GRAPH_PARAM_NAME,
						String.valueOf(defaultGraphURI)));
			}
			for (URI namedGraphURI : dataset.getNamedGraphs()) {
				queryParams.add(new BasicNameValuePair(Protocol.NAMED_GRAPH_PARAM_NAME, String.valueOf(namedGraphURI)));
			}
		}

		for (int i = 0; i < bindings.length; i++) {
			String paramName = Protocol.BINDING_PREFIX + bindings[i].getName();
			String paramValue = Protocol.encodeValue(bindings[i].getValue());
			queryParams.add(new BasicNameValuePair(paramName, paramValue));
		}

		return queryParams;
	}

	protected List<NameValuePair> getUpdateMethodParameters(QueryLanguage ql, String update, String baseURI,
			Dataset dataset, boolean includeInferred, Binding... bindings)
	{
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>(bindings.length + 10);

		queryParams.add(new BasicNameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.getName()));
		queryParams.add(new BasicNameValuePair(Protocol.UPDATE_PARAM_NAME, update));
		if (baseURI != null) {
			queryParams.add(new BasicNameValuePair(Protocol.BASEURI_PARAM_NAME, baseURI));
		}
		queryParams.add(new BasicNameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME,
				Boolean.toString(includeInferred)));

		if (dataset != null) {
			for (URI graphURI : dataset.getDefaultRemoveGraphs()) {
				queryParams.add(new BasicNameValuePair(Protocol.REMOVE_GRAPH_PARAM_NAME, String.valueOf(graphURI)));
			}
			if (dataset.getDefaultInsertGraph() != null) {
				queryParams.add(new BasicNameValuePair(Protocol.INSERT_GRAPH_PARAM_NAME,
						String.valueOf(dataset.getDefaultInsertGraph())));
			}
			for (URI defaultGraphURI : dataset.getDefaultGraphs()) {
				queryParams.add(new BasicNameValuePair(Protocol.USING_GRAPH_PARAM_NAME,
						String.valueOf(defaultGraphURI)));
			}
			for (URI namedGraphURI : dataset.getNamedGraphs()) {
				queryParams.add(new BasicNameValuePair(Protocol.USING_NAMED_GRAPH_PARAM_NAME,
						String.valueOf(namedGraphURI)));
			}
		}

		for (int i = 0; i < bindings.length; i++) {
			String paramName = Protocol.BINDING_PREFIX + bindings[i].getName();
			String paramValue = Protocol.encodeValue(bindings[i].getValue());
			queryParams.add(new BasicNameValuePair(paramName, paramValue));
		}

		return queryParams;
	}

	/*------------------*
	 * Response parsing *
	 *------------------*/

	/**
	 * Parse the response in a background thread. HTTP connections are dealt with
	 * in the {@link BackgroundTupleResult} or (in the error-case) in this
	 * method.
	 */
	protected BackgroundTupleResult getBackgroundTupleQueryResult(HttpUriRequest method)
		throws RepositoryException, QueryInterruptedException, MalformedQueryException,
		IOException
	{

		boolean submitted = false;

		// Specify which formats we support
		Set<TupleQueryResultFormat> tqrFormats = TupleQueryResultParserRegistry.getInstance().getKeys();
		if (tqrFormats.isEmpty()) {
			throw new RepositoryException("No tuple query result parsers have been registered");
		}

		// send the tuple query
		HttpResponse response = sendTupleQueryViaHttp(method, tqrFormats);
		try {

			// if we get here, HTTP code is 200
			String mimeType = getResponseMIMEType(response);
			try {
				TupleQueryResultFormat format = TupleQueryResultFormat.matchMIMEType(mimeType, tqrFormats);
				TupleQueryResultParser parser = QueryResultIO.createParser(format, getValueFactory());
				BackgroundTupleResult tRes = new BackgroundTupleResult(parser, response.getEntity().getContent());
				execute(tRes);
				submitted = true;
				return tRes;
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
		}
		finally {
			if (!submitted)
				EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	/**
	 * Parse the response in this thread using the provided
	 * {@link TupleQueryResultHandler}. All HTTP connections are closed and
	 * released in this method
	 */
	protected void getTupleQueryResult(HttpUriRequest method, TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException, QueryInterruptedException
	{
		// Specify which formats we support
		Set<TupleQueryResultFormat> tqrFormats = TupleQueryResultParserRegistry.getInstance().getKeys();
		if (tqrFormats.isEmpty()) {
			throw new RepositoryException("No tuple query result parsers have been registered");
		}

		// send the tuple query
		HttpResponse response = sendTupleQueryViaHttp(method, tqrFormats);
		try {

			// if we get here, HTTP code is 200
			String mimeType = getResponseMIMEType(response);
			try {
				TupleQueryResultFormat format = TupleQueryResultFormat.matchMIMEType(mimeType, tqrFormats);
				TupleQueryResultParser parser = QueryResultIO.createParser(format, getValueFactory());
				parser.setQueryResultHandler(handler);
				parser.parseQueryResult(response.getEntity().getContent());
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (QueryResultParseException e) {
				throw new RepositoryException("Malformed query result from server", e);
			}
			catch (QueryResultHandlerException e) {
				if (e instanceof TupleQueryResultHandlerException) {
					throw (TupleQueryResultHandlerException)e;
				}
				else {
					throw new TupleQueryResultHandlerException(e);
				}
			}
		}
		finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	/**
	 * Send the tuple query via HTTP and throws an exception in case anything
	 * goes wrong, i.e. only for HTTP 200 the method returns without exception.
	 * If HTTP status code is not equal to 200, the request is aborted, however
	 * pooled connections are not released.
	 * 
	 * @param method
	 * @throws RepositoryException
	 * @throws HttpException
	 * @throws IOException
	 * @throws QueryInterruptedException
	 * @throws MalformedQueryException
	 */
	private HttpResponse sendTupleQueryViaHttp(HttpUriRequest method, Set<TupleQueryResultFormat> tqrFormats)
		throws RepositoryException, IOException, QueryInterruptedException, MalformedQueryException
	{

		for (TupleQueryResultFormat format : tqrFormats) {
			// Determine a q-value that reflects the user specified preference
			int qValue = 10;

			if (preferredTQRFormat != null && !preferredTQRFormat.equals(format)) {
				// Prefer specified format over other formats
				qValue -= 2;
			}

			for (String mimeType : format.getMIMETypes()) {
				String acceptParam = mimeType;

				if (qValue < 10) {
					acceptParam += ";q=0." + qValue;
				}

				method.addHeader(ACCEPT_PARAM_NAME, acceptParam);
			}
		}

		try {
			return executeOK(method);
		}
		catch (RepositoryException|QueryInterruptedException|MalformedQueryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Parse the response in a background thread. HTTP connections are dealt with
	 * in the {@link BackgroundGraphResult} or (in the error-case) in this
	 * method.
	 */
	protected BackgroundGraphResult getRDFBackground(HttpUriRequest method, boolean requireContext)
		throws IOException, RDFHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException, QueryInterruptedException
	{

		boolean submitted = false;

		// Specify which formats we support using Accept headers
		Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
		if (rdfFormats.isEmpty()) {
			throw new RepositoryException("No tuple RDF parsers have been registered");
		}

		// send the tuple query
		HttpResponse response = sendGraphQueryViaHttp(method, requireContext, rdfFormats);
		try {

			// if we get here, HTTP code is 200
			String mimeType = getResponseMIMEType(response);
			try {
				RDFFormat format = RDFFormat.matchMIMEType(mimeType, rdfFormats);
				RDFParser parser = Rio.createParser(format, getValueFactory());
				parser.setParserConfig(getParserConfig());
				parser.setParseErrorListener(new ParseErrorLogger());

				Charset charset = null;

				// SES-1793 : Do not attempt to check for a charset if the format is
				// defined not to have a charset
				// This prevents errors caused by people erroneously attaching a
				// charset to a binary formatted document
				HttpEntity entity = response.getEntity();
				if (format.hasCharset() && entity != null && entity.getContentType() != null) {
					// TODO copied from SPARQLGraphQuery repository, is this
					// required?
					try {
						charset = ContentType.parse(entity.getContentType().getValue()).getCharset();
					}
					catch (IllegalCharsetNameException e) {
						// work around for Joseki-3.2
						// Content-Type: application/rdf+xml;
						// charset=application/rdf+xml
					}
					if (charset == null) {
						charset = UTF8;
					}
				}

				String baseURI = method.getURI().toASCIIString();
				BackgroundGraphResult gRes = new BackgroundGraphResult(parser, entity.getContent(),
						charset, baseURI);
				execute(gRes);
				submitted = true;
				return gRes;
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
		}
		finally {
			if (!submitted)
				EntityUtils.consumeQuietly(response.getEntity());
		}

	}

	/**
	 * Parse the response in this thread using the provided {@link RDFHandler}.
	 * All HTTP connections are closed and released in this method
	 */
	protected void getRDF(HttpUriRequest method, RDFHandler handler, boolean requireContext)
		throws IOException, RDFHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException, QueryInterruptedException
	{
		// Specify which formats we support using Accept headers
		Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
		if (rdfFormats.isEmpty()) {
			throw new RepositoryException("No tuple RDF parsers have been registered");
		}

		// send the tuple query
		HttpResponse response = sendGraphQueryViaHttp(method, requireContext, rdfFormats);
		try {

			String mimeType = getResponseMIMEType(response);
			try {
				RDFFormat format = RDFFormat.matchMIMEType(mimeType, rdfFormats);
				RDFParser parser = Rio.createParser(format, getValueFactory());
				parser.setParserConfig(getParserConfig());
				parser.setParseErrorListener(new ParseErrorLogger());
				parser.setRDFHandler(handler);
				parser.parse(response.getEntity().getContent(), method.getURI().toASCIIString());
			}
			catch (UnsupportedRDFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (RDFParseException e) {
				throw new RepositoryException("Malformed query result from server", e);
			}
		}
		finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	private HttpResponse sendGraphQueryViaHttp(HttpUriRequest method, boolean requireContext, Set<RDFFormat> rdfFormats)
		throws RepositoryException, IOException, QueryInterruptedException, MalformedQueryException
	{

		List<String> acceptParams = RDFFormat.getAcceptParams(rdfFormats, requireContext, getPreferredRDFFormat());
		for (String acceptParam : acceptParams) {
			method.addHeader(ACCEPT_PARAM_NAME, acceptParam);
		}

		try {
			return executeOK(method);
		}
		catch (RepositoryException|QueryInterruptedException|MalformedQueryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Parse the response in this thread using a suitable
	 * {@link BooleanQueryResultParser}. All HTTP connections are closed and
	 * released in this method
	 * @throws OpenRDFException 
	 */
	protected boolean getBoolean(HttpUriRequest method)
		throws IOException, OpenRDFException
	{
		// Specify which formats we support using Accept headers
		Set<BooleanQueryResultFormat> booleanFormats = BooleanQueryResultParserRegistry.getInstance().getKeys();
		if (booleanFormats.isEmpty()) {
			throw new RepositoryException("No boolean query result parsers have been registered");
		}

		// send the tuple query
		HttpResponse response = sendBooleanQueryViaHttp(method, booleanFormats);
		try {

			// if we get here, HTTP code is 200
			String mimeType = getResponseMIMEType(response);
			try {
				BooleanQueryResultFormat format = BooleanQueryResultFormat.matchMIMEType(mimeType, booleanFormats);
				BooleanQueryResultParser parser = QueryResultIO.createParser(format);
				return parser.parse(response.getEntity().getContent());
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (QueryResultParseException e) {
				throw new RepositoryException("Malformed query result from server", e);
			}
		}
		finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}

	}

	private HttpResponse sendBooleanQueryViaHttp(HttpUriRequest method, Set<BooleanQueryResultFormat> booleanFormats)
		throws IOException, OpenRDFException
	{

		for (BooleanQueryResultFormat format : booleanFormats) {
			// Determine a q-value that reflects the user specified preference
			int qValue = 10;

			if (preferredBQRFormat != null && !preferredBQRFormat.equals(format)) {
				// Prefer specified format over other formats
				qValue -= 2;
			}

			for (String mimeType : format.getMIMETypes()) {
				String acceptParam = mimeType;

				if (qValue < 10) {
					acceptParam += ";q=0." + qValue;
				}

				method.addHeader(ACCEPT_PARAM_NAME, acceptParam);
			}
		}

		return executeOK(method);
	}

	/**
	 * Convenience method to deal with HTTP level errors of tuple, graph and
	 * boolean queries in the same way. This method aborts the HTTP connection.
	 * 
	 * @param method
	 * @throws OpenRDFException 
	 */
	protected HttpResponse executeOK(HttpUriRequest method)
		throws IOException, OpenRDFException
	{
		boolean fail = true;
		HttpResponse response = execute(method);

		try {
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode == HttpURLConnection.HTTP_OK) {
				fail = false;
				return response; // everything OK, control flow can continue
			} else {
				ErrorInfo errInfo = getErrorInfo(response);

				// Throw appropriate exception
				if (errInfo.getErrorType() == ErrorType.MALFORMED_QUERY) {
					throw new MalformedQueryException(errInfo.getErrorMessage());
				}
				else if (errInfo.getErrorType() == ErrorType.UNSUPPORTED_QUERY_LANGUAGE) {
					throw new UnsupportedQueryLanguageException(errInfo.getErrorMessage());
				}
				else {
					throw new RepositoryException(errInfo.toString());
				}
			}
		} finally {
			if (fail) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	protected void executeNoContent(HttpUriRequest method)
		throws IOException, OpenRDFException
	{
		EntityUtils.consume(execute(method).getEntity());
	}

	private HttpResponse execute(HttpUriRequest method)
		throws IOException, OpenRDFException
	{
		boolean fail = true;
		method.setParams(params);
		HttpResponse response = httpClient.execute(method, httpContext);

		try {
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode >= 200 && httpCode < 300) {
				fail = false;
				return response; // everything OK, control flow can continue
			} else {
				if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					throw new UnauthorizedException();
				}
				else if (httpCode == HttpURLConnection.HTTP_UNAVAILABLE) {
					throw new QueryInterruptedException();
				}
				else if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
					// trying to contact a non-Sesame server?
					throw new RepositoryException("Failed to get server protocol; no such resource on this server");
				}
				else {
					ErrorInfo errInfo = getErrorInfo(response);
	
					// Throw appropriate exception
					if (errInfo.getErrorType() == ErrorType.MALFORMED_DATA) {
						throw new RDFParseException(errInfo.getErrorMessage());
					}
					else if (errInfo.getErrorType() == ErrorType.UNSUPPORTED_FILE_FORMAT) {
						throw new UnsupportedRDFormatException(errInfo.getErrorMessage());
					}
					else if (errInfo.getErrorType() == ErrorType.MALFORMED_QUERY) {
						throw new MalformedQueryException(errInfo.getErrorMessage());
					}
					else if (errInfo.getErrorType() == ErrorType.UNSUPPORTED_QUERY_LANGUAGE) {
						throw new UnsupportedQueryLanguageException(errInfo.getErrorMessage());
					}
					else {
						throw new RepositoryException(errInfo.toString());
					}
				}
			}
		} finally {
			if (fail) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	/*-------------------------*
	 * General utility methods *
	 *-------------------------*/

	/**
	 * Gets the MIME type specified in the response headers of the supplied
	 * method, if any. For example, if the response headers contain
	 * <tt>Content-Type: application/xml;charset=UTF-8</tt>, this method will
	 * return <tt>application/xml</tt> as the MIME type.
	 * 
	 * @param method
	 *        The method to get the reponse MIME type from.
	 * @return The response MIME type, or <tt>null</tt> if not available.
	 */
	protected String getResponseMIMEType(HttpResponse method)
		throws IOException
	{
		Header[] headers = method.getHeaders("Content-Type");

		for (Header header : headers) {
			HeaderElement[] headerElements = header.getElements();

			for (HeaderElement headerEl : headerElements) {
				String mimeType = headerEl.getName();
				if (mimeType != null) {
					logger.debug("reponse MIME type is {}", mimeType);
					return mimeType;
				}
			}
		}

		return null;
	}

	protected ErrorInfo getErrorInfo(HttpResponse response)
		throws RepositoryException
	{
		try {
			ErrorInfo errInfo = ErrorInfo.parse(EntityUtils.toString(response.getEntity()));
			logger.warn("Server reports problem: {}", errInfo.getErrorMessage());
			return errInfo;
		}
		catch (IOException e) {
			logger.warn("Unable to retrieve error info from server");
			throw new RepositoryException("Unable to retrieve error info from server", e);
		}
	}

	/**
	 * Sets the parser configuration used to process HTTP response data.
	 * 
	 * @param parserConfig
	 *        The parserConfig to set.
	 */
	public void setParserConfig(ParserConfig parserConfig) {
		this.parserConfig = parserConfig;
	}

	/**
	 * @return Returns the parser configuration used to process HTTP response
	 *         data.
	 */
	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	/**
	 * Gets the http connection read timeout in milliseconds.
	 */
	public long getConnectionTimeout() {
		return (long) params.getIntParameter(CoreConnectionPNames.SO_TIMEOUT, 0);
	}

	/**
	 * Sets the http connection read timeout.
	 * 
	 * @param timeout
	 *        timeout in milliseconds. Zero sets to infinity.
	 */
	public void setConnectionTimeout(long timeout) {
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, (int) timeout);
	}
}
