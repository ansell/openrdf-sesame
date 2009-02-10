/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import static org.openrdf.http.protocol.Protocol.ACCEPT_PARAM_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;
import info.aduna.net.http.HttpClientUtil;

import org.openrdf.OpenRDFUtil;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.query.impl.TupleQueryResultBuilder;
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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Low-level HTTP client for Sesame's HTTP protocol. Methods correspond directly
 * to the functionality offered by the protocol.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class HTTPClient {

	/*-----------*
	 * Constants *
	 *-----------*/

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory valueFactory;

	private String serverURL;

	private String repositoryURL;

	private HttpClient httpClient;

	private AuthScope authScope;

	private TupleQueryResultFormat preferredTQRFormat = TupleQueryResultFormat.BINARY;

	private BooleanQueryResultFormat preferredBQRFormat = BooleanQueryResultFormat.TEXT;

	private RDFFormat preferredRDFFormat = RDFFormat.TURTLE;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPClient() {
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

	public void setServerURL(String serverURL) {
		if (serverURL == null) {
			throw new IllegalArgumentException("serverURL must not be null");
		}

		this.serverURL = serverURL;
	}

	public String getServerURL() {
		return serverURL;
	}

	protected void checkServerURL() {
		if (serverURL == null) {
			throw new IllegalStateException("Server URL has not been set");
		}
	}

	public void setRepositoryURL(final String repositoryURL) {
		if (repositoryURL == null) {
			throw new IllegalArgumentException("repositoryURL must not be null");
		}

		this.repositoryURL = repositoryURL;

		// Try to parse the server URL from the repository URL
		Pattern urlPattern = Pattern.compile("(.*)/" + Protocol.REPOSITORIES + "/[^/]*/?");
		Matcher matcher = urlPattern.matcher(repositoryURL);

		if (matcher.matches() && matcher.groupCount() == 1) {
			setServerURL(matcher.group(1));
		}
	}

	public String getRepositoryURL() {
		return repositoryURL;
	}

	protected void checkRepositoryURL() {
		if (repositoryURL == null) {
			throw new IllegalStateException("Repository URL has not been set");
		}
	}

	public void setRepositoryID(String repositoryID) {
		checkServerURL();
		repositoryURL = Protocol.getRepositoryLocation(serverURL, repositoryID);
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
		checkServerURL();

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

	/*------------------*
	 * Protocol version *
	 *------------------*/

	public String getServerProtocol()
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkServerURL();

		GetMethod method = new GetMethod(Protocol.getProtocolLocation(serverURL));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_OK) {
				return method.getResponseBodyAsString();
			}
			else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to get server protocol: " + errInfo);
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	/*-----------------*
	 * Repository list *
	 *-----------------*/

	public TupleQueryResult getRepositoryList()
		throws IOException, RepositoryException, UnauthorizedException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			getRepositoryList(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void getRepositoryList(TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, UnauthorizedException
	{
		checkServerURL();

		GetMethod method = new GetMethod(Protocol.getRepositoriesLocation(serverURL));
		setDoAuthentication(method);

		try {
			getTupleQueryResult(method, handler);
		}
		catch (MalformedQueryException e) {
			// This shouldn't happen as no queries are involved
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
		finally {
			releaseConnection(method);
		}
	}

	/*------------------*
	 * Query evaluation *
	 *------------------*/

	public TupleQueryResult sendTupleQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			sendTupleQuery(ql, query, dataset, includeInferred, builder, bindings);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendTupleQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			TupleQueryResultHandler handler, Binding... bindings)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException
	{
		HttpMethod method = getQueryMethod(ql, query, dataset, includeInferred, bindings);

		try {
			getTupleQueryResult(method, handler);
		}
		finally {
			releaseConnection(method);
		}
	}

	public GraphQueryResult sendGraphQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException
	{
		try {
			StatementCollector collector = new StatementCollector();
			sendGraphQuery(ql, query, dataset, includeInferred, collector, bindings);
			return new GraphQueryResultImpl(collector.getNamespaces(), collector.getStatements());
		}
		catch (RDFHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendGraphQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			RDFHandler handler, Binding... bindings)
		throws IOException, RDFHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException
	{
		HttpMethod method = getQueryMethod(ql, query, dataset, includeInferred, bindings);

		try {
			getRDF(method, handler, false);
		}
		finally {
			releaseConnection(method);
		}
	}

	public boolean sendBooleanQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			Binding... bindings)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException
	{
		HttpMethod method = getQueryMethod(ql, query, dataset, includeInferred, bindings);

		try {
			return getBoolean(method);
		}
		finally {
			releaseConnection(method);
		}
	}

	protected HttpMethod getQueryMethod(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		PostMethod method = new PostMethod(getRepositoryURL());
		setDoAuthentication(method);

		method.setRequestHeader("Content-Type", Protocol.FORM_MIME_TYPE + "; charset=utf-8");

		List<NameValuePair> queryParams = getQueryMethodParameters(ql, query, dataset, includeInferred,
				bindings);

		method.setRequestBody(queryParams.toArray(new NameValuePair[queryParams.size()]));

		return method;
	}

	protected List<NameValuePair> getQueryMethodParameters(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>(bindings.length + 10);

		queryParams.add(new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.getName()));
		queryParams.add(new NameValuePair(Protocol.QUERY_PARAM_NAME, query));
		queryParams.add(new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME,
				Boolean.toString(includeInferred)));

		if (dataset != null) {
			for (URI defaultGraphURI : dataset.getDefaultGraphs()) {
				queryParams.add(new NameValuePair(Protocol.DEFAULT_GRAPH_PARAM_NAME, defaultGraphURI.toString()));
			}
			for (URI namedGraphURI : dataset.getNamedGraphs()) {
				queryParams.add(new NameValuePair(Protocol.NAMED_GRAPH_PARAM_NAME, namedGraphURI.toString()));
			}
		}

		for (int i = 0; i < bindings.length; i++) {
			String paramName = Protocol.BINDING_PREFIX + bindings[i].getName();
			String paramValue = Protocol.encodeValue(bindings[i].getValue());
			queryParams.add(new NameValuePair(paramName, paramValue));
		}

		return queryParams;
	}

	/*---------------------------*
	 * Get/add/remove statements *
	 *---------------------------*/

	public void getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws IOException, RDFHandlerException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getStatementsLocation(getRepositoryURL()));
		setDoAuthentication(method);

		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		if (subj != null) {
			params.add(new NameValuePair(Protocol.SUBJECT_PARAM_NAME, Protocol.encodeValue(subj)));
		}
		if (pred != null) {
			params.add(new NameValuePair(Protocol.PREDICATE_PARAM_NAME, Protocol.encodeValue(pred)));
		}
		if (obj != null) {
			params.add(new NameValuePair(Protocol.OBJECT_PARAM_NAME, Protocol.encodeValue(obj)));
		}
		for (String encodedContext : Protocol.encodeContexts(contexts)) {
			params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		params.add(new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred)));

		method.setQueryString(params.toArray(new NameValuePair[params.size()]));

		try {
			getRDF(method, handler, true);
		}
		catch (MalformedQueryException e) {
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
		finally {
			releaseConnection(method);
		}
	}

	public void sendTransaction(final Iterable<? extends TransactionOperation> txn)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		PostMethod method = new PostMethod(Protocol.getStatementsLocation(getRepositoryURL()));
		setDoAuthentication(method);

		// Create a RequestEntity for the transaction data
		method.setRequestEntity(new RequestEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public String getContentType() {
				return Protocol.TXN_MIME_TYPE;
			}

			public boolean isRepeatable() {
				return true;
			}

			public void writeRequest(OutputStream out)
				throws IOException
			{
				TransactionWriter txnWriter = new TransactionWriter();
				txnWriter.serialize(txn, out);
			}
		});

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!HttpClientUtil.is2xx(httpCode)) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Transaction failed: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	public void upload(final Reader contents, String baseURI, final RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		final Charset charset = dataFormat.hasCharset() ? dataFormat.getCharset() : Charset.forName("UTF-8");

		RequestEntity entity = new RequestEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public String getContentType() {
				return dataFormat.getDefaultMIMEType() + "; charset=" + charset.name();
			}

			public boolean isRepeatable() {
				return false;
			}

			public void writeRequest(OutputStream out)
				throws IOException
			{
				OutputStreamWriter writer = new OutputStreamWriter(out, charset);
				IOUtil.transfer(contents, writer);
				writer.flush();
			}
		};

		upload(entity, baseURI, overwrite, contexts);
	}

	public void upload(InputStream contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		// Set Content-Length to -1 as we don't know it and we also don't want to
		// cache
		RequestEntity entity = new InputStreamRequestEntity(contents, -1, dataFormat.getDefaultMIMEType());
		upload(entity, baseURI, overwrite, contexts);
	}

	protected void upload(RequestEntity reqEntity, String baseURI, boolean overwrite, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		checkRepositoryURL();

		String uploadURL = Protocol.getStatementsLocation(getRepositoryURL());

		// Select appropriate HTTP method
		EntityEnclosingMethod method;
		if (overwrite) {
			method = new PutMethod(uploadURL);
		}
		else {
			method = new PostMethod(uploadURL);
		}

		setDoAuthentication(method);

		// Set relevant query parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		for (String encodedContext : Protocol.encodeContexts(contexts)) {
			params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		if (baseURI != null && baseURI.trim().length() != 0) {
			String encodedBaseURI = Protocol.encodeValue(new URIImpl(baseURI));
			params.add(new NameValuePair(Protocol.BASEURI_PARAM_NAME, encodedBaseURI));
		}
		method.setQueryString(params.toArray(new NameValuePair[params.size()]));

		// Set payload
		method.setRequestEntity(reqEntity);

		// Send request
		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (httpCode == HttpURLConnection.HTTP_UNSUPPORTED_TYPE) {
				throw new UnsupportedRDFormatException(method.getResponseBodyAsString());
			}
			else if (!HttpClientUtil.is2xx(httpCode)) {
				ErrorInfo errInfo = ErrorInfo.parse(method.getResponseBodyAsString());

				if (errInfo.getErrorType() == ErrorType.MALFORMED_DATA) {
					throw new RDFParseException(errInfo.getErrorMessage());
				}
				else if (errInfo.getErrorType() == ErrorType.UNSUPPORTED_FILE_FORMAT) {
					throw new UnsupportedRDFormatException(errInfo.getErrorMessage());
				}
				else {
					throw new RepositoryException("Failed to upload data: " + errInfo);
				}
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	/*-------------*
	 * Context IDs *
	 *-------------*/

	public TupleQueryResult getContextIDs()
		throws IOException, RepositoryException, UnauthorizedException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			getContextIDs(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void getContextIDs(TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getContextsLocation(getRepositoryURL()));
		setDoAuthentication(method);

		try {
			getTupleQueryResult(method, handler);
		}
		catch (MalformedQueryException e) {
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
		finally {
			releaseConnection(method);
		}
	}

	/*---------------------------*
	 * Get/add/remove namespaces *
	 *---------------------------*/

	public TupleQueryResult getNamespaces()
		throws IOException, RepositoryException, UnauthorizedException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			getNamespaces(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void getNamespaces(TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpMethod method = new GetMethod(Protocol.getNamespacesLocation(repositoryURL));
		setDoAuthentication(method);

		try {
			getTupleQueryResult(method, handler);
		}
		catch (MalformedQueryException e) {
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
		finally {
			releaseConnection(method);
		}
	}

	public String getNamespace(String prefix)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpMethod method = new GetMethod(Protocol.getNamespacePrefixLocation(repositoryURL, prefix));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_OK) {
				return method.getResponseBodyAsString();
			}
			else if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
				return null;
			}
			else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to get namespace: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	public void setNamespacePrefix(String prefix, String name)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		EntityEnclosingMethod method = new PutMethod(Protocol.getNamespacePrefixLocation(repositoryURL, prefix));
		setDoAuthentication(method);
		method.setRequestEntity(new StringRequestEntity(name, "text/plain", "UTF-8"));

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!HttpClientUtil.is2xx(httpCode)) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to set namespace: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	public void removeNamespacePrefix(String prefix)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpMethod method = new DeleteMethod(Protocol.getNamespacePrefixLocation(repositoryURL, prefix));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!HttpClientUtil.is2xx(httpCode)) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to remove namespace: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	public void clearNamespaces()
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpMethod method = new DeleteMethod(Protocol.getNamespacesLocation(repositoryURL));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!HttpClientUtil.is2xx(httpCode)) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to clear namespaces: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	/*-------------------------*
	 * Repository/context size *
	 *-------------------------*/

	public long size(Resource... contexts)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		String[] encodedContexts = Protocol.encodeContexts(contexts);

		NameValuePair[] contextParams = new NameValuePair[encodedContexts.length];
		for (int i = 0; i < encodedContexts.length; i++) {
			contextParams[i] = new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContexts[i]);
		}

		HttpMethod method = new GetMethod(Protocol.getSizeLocation(repositoryURL));
		setDoAuthentication(method);
		method.setQueryString(contextParams);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_OK) {
				String response = method.getResponseBodyAsString();
				try {
					return Long.parseLong(response);
				}
				catch (NumberFormatException e) {
					throw new RepositoryException("Server responded with invalid size value: " + response);
				}
			}
			else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException(errInfo.toString());
			}
		}
		finally {
			releaseConnection(method);
		}
	}

	/*------------------*
	 * Response parsing *
	 *------------------*/

	protected void getTupleQueryResult(HttpMethod method, TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException
	{
		// Specify which formats we support using Accept headers
		Set<TupleQueryResultFormat> tqrFormats = TupleQueryResultParserRegistry.getInstance().getKeys();
		if (tqrFormats.isEmpty()) {
			throw new RepositoryException("No tuple query result parsers have been registered");
		}

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

				method.addRequestHeader(ACCEPT_PARAM_NAME, acceptParam);
			}
		}

		int httpCode = httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = getResponseMIMEType(method);
			try {
				TupleQueryResultFormat format = TupleQueryResultFormat.matchMIMEType(mimeType, tqrFormats);
				TupleQueryResultParser parser = QueryResultIO.createParser(format, getValueFactory());
				parser.setTupleQueryResultHandler(handler);
				parser.parse(method.getResponseBodyAsStream());
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (QueryResultParseException e) {
				throw new RepositoryException("Malformed query result from server", e);
			}
		}
		else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
			throw new UnauthorizedException();
		}
		else {
			ErrorInfo errInfo = getErrorInfo(method);

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
	}

	protected void getRDF(HttpMethod method, RDFHandler handler, boolean requireContext)
		throws IOException, RDFHandlerException, RepositoryException, MalformedQueryException,
		UnauthorizedException
	{
		// Specify which formats we support using Accept headers
		Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
		if (rdfFormats.isEmpty()) {
			throw new RepositoryException("No tuple RDF parsers have been registered");
		}

		for (RDFFormat format : rdfFormats) {
			// Determine a q-value that reflects the necessity of context
			// support and the user specified preference
			int qValue = 10;

			if (requireContext && !format.supportsContexts()) {
				// Prefer context-supporting formats over pure triple-formats
				qValue -= 5;
			}

			if (preferredRDFFormat != null && !preferredRDFFormat.equals(format)) {
				// Prefer specified format over other formats
				qValue -= 2;
			}

			if (!format.supportsNamespaces()) {
				// We like reusing namespace prefixes
				qValue -= 1;
			}

			for (String mimeType : format.getMIMETypes()) {
				String acceptParam = mimeType;

				if (qValue < 10) {
					acceptParam += ";q=0." + qValue;
				}

				method.addRequestHeader(ACCEPT_PARAM_NAME, acceptParam);
			}
		}

		int httpCode = httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = getResponseMIMEType(method);
			try {
				RDFFormat format = RDFFormat.matchMIMEType(mimeType, rdfFormats);
				RDFParser parser = Rio.createParser(format, getValueFactory());
				parser.setPreserveBNodeIDs(true);
				parser.setRDFHandler(handler);
				parser.parse(method.getResponseBodyAsStream(), method.getURI().getURI());
			}
			catch (UnsupportedRDFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (RDFParseException e) {
				throw new RepositoryException("Malformed query result from server", e);
			}
		}
		else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
			throw new UnauthorizedException();
		}
		else {
			ErrorInfo errInfo = getErrorInfo(method);

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
	}

	protected boolean getBoolean(HttpMethod method)
		throws IOException, RepositoryException, MalformedQueryException, UnauthorizedException
	{
		// Specify which formats we support using Accept headers
		Set<BooleanQueryResultFormat> booleanFormats = BooleanQueryResultParserRegistry.getInstance().getKeys();
		if (booleanFormats.isEmpty()) {
			throw new RepositoryException("No boolean query result parsers have been registered");
		}

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

				method.addRequestHeader(ACCEPT_PARAM_NAME, acceptParam);
			}
		}

		int httpCode = httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = getResponseMIMEType(method);
			try {
				BooleanQueryResultFormat format = BooleanQueryResultFormat.matchMIMEType(mimeType, booleanFormats);
				BooleanQueryResultParser parser = QueryResultIO.createParser(format);
				return parser.parse(method.getResponseBodyAsStream());
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new RepositoryException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (QueryResultParseException e) {
				throw new RepositoryException("Malformed query result from server", e);
			}
		}
		else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
			throw new UnauthorizedException();
		}
		else {
			ErrorInfo errInfo = getErrorInfo(method);

			// Throw appropriate exception
			if (errInfo.getErrorType() == ErrorType.MALFORMED_QUERY) {
				throw new MalformedQueryException(errInfo.getErrorMessage());
			}
			else if (errInfo.getErrorType() == ErrorType.UNSUPPORTED_QUERY_LANGUAGE) {
				throw new UnsupportedQueryLanguageException(errInfo.getErrorMessage());
			}
			else {
				throw new RepositoryException(method.getStatusText());
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
	protected String getResponseMIMEType(HttpMethod method)
		throws IOException
	{
		Header[] headers = method.getResponseHeaders("Content-Type");

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

	protected ErrorInfo getErrorInfo(HttpMethod method)
		throws RepositoryException
	{
		try {
			ErrorInfo errInfo = ErrorInfo.parse(method.getResponseBodyAsString());
			logger.warn("Server reports problem: {}", errInfo.getErrorMessage());
			return errInfo;
		}
		catch (IOException e) {
			logger.warn("Unable to retrieve error info from server");
			throw new RepositoryException("Unable to retrieve error info from server", e);
		}
	}

	protected final void setDoAuthentication(HttpMethod method) {
		if (authScope != null && httpClient.getState().getCredentials(authScope) != null) {
			method.setDoAuthentication(true);
		}
		else {
			method.setDoAuthentication(false);
		}
	}

	protected final void releaseConnection(HttpMethod method) {
		if (Thread.currentThread().isInterrupted()) {
			method.abort();
		}
		else {
			method.releaseConnection();
		}
	}
}
