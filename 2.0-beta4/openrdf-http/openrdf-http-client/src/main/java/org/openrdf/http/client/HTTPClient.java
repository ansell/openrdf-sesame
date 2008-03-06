/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;
import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.OpenRDFUtil;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultParserFactory;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
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

	private ValueFactory _valueFactory;

	private String _serverURL;

	private String _repositoryURL;

	private HttpClient _httpClient;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPClient() {
		_valueFactory = new ValueFactoryImpl();

		// TODO: check configuration options for HttpClient
		_httpClient = new HttpClient();
	}

	/*-----------------*
	 * Get/set methods *
	 *-----------------*/

	public void setValueFactory(ValueFactory valueFactory) {
		_valueFactory = valueFactory;
	}

	public ValueFactory getValueFactory() {
		return _valueFactory;
	}

	public void setServerURL(String serverURL) {
		if (serverURL == null) {
			throw new IllegalArgumentException("serverURL must not be null");
		}

		_serverURL = serverURL;
	}

	public String getServerURL() {
		return _serverURL;
	}

	protected void _checkServerURL() {
		if (_serverURL == null) {
			throw new IllegalStateException("Server URL has not been set");
		}
	}

	public void setRepositoryURL(final String repositoryURL) {
		if (repositoryURL == null) {
			throw new IllegalArgumentException("repositoryURL must not be null");
		}

		Pattern urlPattern = Pattern.compile("(.*)/" + Protocol.REPOSITORIES + "/[^/]*/?");
		Matcher matcher = urlPattern.matcher(repositoryURL);

		if (matcher.matches() && matcher.groupCount() == 1) {
			setServerURL(matcher.group(1));
			_repositoryURL = repositoryURL;
		}
		else {
			throw new IllegalArgumentException("Invalid repository URL: " + repositoryURL);
		}
	}

	public String getRepositoryURL() {
		return _repositoryURL;
	}

	protected void _checkRepositoryURL() {
		if (_repositoryURL == null) {
			throw new IllegalStateException("Repository URL has not been set");
		}
	}

	public void setRepositoryID(String repositoryID) {
		_checkServerURL();
		_repositoryURL = Protocol.getRepositoryLocation(_serverURL, repositoryID);
	}

	/*------------------*
	 * Protocol version *
	 *------------------*/

	public String getServerProtocol()
		throws IOException
	{
		_checkServerURL();

		GetMethod getMethod = new GetMethod(Protocol.getProtocolLocation(_serverURL));

		int httpCode = _httpClient.executeMethod(getMethod);

		try {
			if (httpCode == HttpURLConnection.HTTP_OK) {
				return getMethod.getResponseBodyAsString();
			}
			else {
				throw new IOException("Failed to get server protocol: " + getMethod.getStatusText());
			}
		}
		finally {
			getMethod.releaseConnection();
		}
	}

	/*-----------------*
	 * Repository list *
	 *-----------------*/

	public TupleQueryResult getRepositoryList()
		throws IOException
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
		throws IOException, TupleQueryResultHandlerException
	{
		_checkServerURL();

		GetMethod method = new GetMethod(Protocol.getRepositoriesLocation(_serverURL));
		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*------------------*
	 * Query evaluation *
	 *------------------*/

	public TupleQueryResult sendTupleQuery(QueryLanguage ql, String query, boolean includeInferred,
			Binding... bindings)
		throws IOException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			sendTupleQuery(ql, query, includeInferred, builder, bindings);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendTupleQuery(QueryLanguage ql, String query, boolean includeInferred,
			TupleQueryResultHandler handler, Binding... bindings)
		throws IOException, TupleQueryResultHandlerException
	{
		GetMethod method = new GetMethod(getRepositoryURL());
		NameValuePair[] queryString = new NameValuePair[3 + bindings.length];

		queryString[0] = new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.toString());
		queryString[1] = new NameValuePair(Protocol.QUERY_PARAM_NAME, query);
		queryString[2] = new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME,
				Boolean.toString(includeInferred));

		for (int i = 0; i < bindings.length; i++) {
			queryString[i + 3] = new NameValuePair(Protocol.BINDING_PREFIX + bindings[i].getName(),
					Protocol.encodeValue(bindings[i].getValue()));

		}

		method.setQueryString(queryString);

		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	public GraphQueryResult sendGraphQuery(QueryLanguage ql, String query, boolean includeInferred,
			Binding... bindings)
		throws IOException
	{
		try {
			StatementCollector collector = new StatementCollector();
			sendGraphQuery(ql, query, includeInferred, collector, bindings);
			return new GraphQueryResultImpl(collector.getNamespaces(),
					new CloseableIteratorIteration<Statement, QueryEvaluationException>(
							collector.getStatements().iterator()));
		}
		catch (RDFHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendGraphQuery(QueryLanguage ql, String query, boolean includeInferred, RDFHandler handler,
			Binding... bindings)
		throws IOException, RDFHandlerException
	{
		GetMethod method = new GetMethod(getRepositoryURL());

		NameValuePair[] queryString = new NameValuePair[3 + bindings.length];

		queryString[0] = new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.toString());
		queryString[1] = new NameValuePair(Protocol.QUERY_PARAM_NAME, query);
		queryString[2] = new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME,
				Boolean.toString(includeInferred));

		for (int i = 0; i < bindings.length; i++) {
			queryString[i + 3] = new NameValuePair(Protocol.BINDING_PREFIX + bindings[i].getName(),
					Protocol.encodeValue(bindings[i].getValue()));
		}

		method.setQueryString(queryString);

		try {
			_getRDF(method, handler, false);
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*---------------------------*
	 * Get/add/remove statements *
	 *---------------------------*/

	public void getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws IOException, RDFHandlerException
	{
		_getStatements(subj, pred, obj, includeInferred, handler, contexts);
	}

	private void _getStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws IOException, RDFHandlerException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		_checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getStatementsLocation(getRepositoryURL()));

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
			_getRDF(method, handler, contexts.length > 0);
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void sendTransaction(final Iterable<? extends TransactionOperation> txn)
		throws IOException
	{
		_checkRepositoryURL();

		PostMethod method = new PostMethod(Protocol.getStatementsLocation(getRepositoryURL()));

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
				TransactionWriter taWriter = new TransactionWriter();
				taWriter.serialize(txn, out);
			}
		});

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Transaction failed: " + method.getStatusText() + " (" + httpCode + ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void upload(final Reader contents, String baseURI, final RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws IOException, RDFParseException
	{
		final Charset charset = dataFormat.hasCharset() ? dataFormat.getCharset() : Charset.forName("UTF-8");

		RequestEntity entity = new RequestEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public String getContentType() {
				return dataFormat.getMIMEType() + "; charset=" + charset.name();
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

		_upload(entity, baseURI, overwrite, contexts);
	}

	public void upload(InputStream contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws IOException, RDFParseException
	{
		// content-length -1 as we don't know and don't want to cache
		RequestEntity entity = new InputStreamRequestEntity(contents, -1, dataFormat.getMIMEType());
		_upload(entity, baseURI, overwrite, contexts);
	}

	private void _upload(RequestEntity reqEntity, String baseURI, boolean overwrite, Resource... contexts)
		throws IOException, RDFParseException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		_checkRepositoryURL();

		String uploadURL = Protocol.getStatementsLocation(getRepositoryURL());

		// Select appropriate HTTP method
		EntityEnclosingMethod method;
		if (overwrite) {
			method = new PutMethod(uploadURL);
		}
		else {
			method = new PostMethod(uploadURL);
		}

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
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode == 400) {
				throw new RDFParseException(method.getStatusText());
			}
			else if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Failed to upload: " + method.getStatusText() + " (" + httpCode + ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*-------------*
	 * Context IDs *
	 *-------------*/

	public TupleQueryResult getContextIDs()
		throws IOException
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
		throws IOException, TupleQueryResultHandlerException
	{
		_checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getContextsLocation(getRepositoryURL()));
		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*---------------------------*
	 * Get/add/remove namespaces *
	 *---------------------------*/

	public TupleQueryResult getNamespaces()
		throws IOException
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
		throws IOException, TupleQueryResultHandlerException
	{
		_checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getNamespacesLocation(_repositoryURL));
		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	public String getNamespace(String prefix)
		throws IOException
	{
		_checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getNamespacePrefixLocation(_repositoryURL, prefix));

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_OK) {
				return method.getResponseBodyAsString();
			}
			else if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
				return null;
			}
			else {
				throw new IOException("Failed to get namespace: " + method.getStatusText() + " (" + httpCode
						+ ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void setNamespacePrefix(String prefix, String name)
		throws IOException
	{
		_checkRepositoryURL();

		PutMethod method = new PutMethod(Protocol.getNamespacePrefixLocation(_repositoryURL, prefix));
		RequestEntity namespace = new StringRequestEntity(name, "text/plain", "UTF-8");
		method.setRequestEntity(namespace);

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Failed to set namespace: " + method.getStatusText() + " (" + httpCode
						+ ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void removeNamespacePrefix(String prefix)
		throws IOException
	{
		_checkRepositoryURL();

		DeleteMethod method = new DeleteMethod(Protocol.getNamespacePrefixLocation(_repositoryURL, prefix));

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Failed to remove namespace: " + method.getStatusText() + " (" + httpCode
						+ ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*-------------------------*
	 * Repository/context size *
	 *-------------------------*/

	public long size(Resource... contexts)
		throws IOException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		_checkRepositoryURL();

		String[] encodedContexts = Protocol.encodeContexts(contexts);

		NameValuePair[] contextParams = new NameValuePair[encodedContexts.length];
		for (int index = 0; index < encodedContexts.length; index++) {
			contextParams[index] = new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContexts[index]);
		}

		GetMethod getMethod = new GetMethod(Protocol.getSizeLocation(_repositoryURL));
		getMethod.setQueryString(contextParams);

		int httpCode = _httpClient.executeMethod(getMethod);

		try {
			if (httpCode == HttpURLConnection.HTTP_OK) {
				String response = getMethod.getResponseBodyAsString();
				try {
					return Long.parseLong(response);
				}
				catch (NumberFormatException e) {
					throw new IOException("Server responded with invalid size value: " + response);
				}
			}
			else {
				throw new IOException("Failed to get size: " + getMethod.getStatusText());
			}
		}
		finally {
			getMethod.releaseConnection();
		}
	}

	/*------------------*
	 * Response parsing *
	 *------------------*/

	private void _getTupleQueryResult(HttpMethod method, TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException
	{
		// TODO: add format preferrence through 'q' attribute?
		for (TupleQueryResultParserFactory factory : QueryResultUtil.getTupleQueryResultParserRegistry().getAll())
		{
			method.addRequestHeader(ACCEPT_PARAM_NAME, factory.getTupleQueryResultFormat().getMIMEType());
		}

		int httpCode = _httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = _getResponseMIMEType(method);
			try {
				TupleQueryResultFormat format = TupleQueryResultFormat.forMIMEType(mimeType);
				TupleQueryResultParser parser = QueryResultUtil.createParser(format, getValueFactory());

				parser.setTupleQueryResultHandler(handler);
				parser.parse(method.getResponseBodyAsStream());
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new IOException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (TupleQueryResultParseException e) {
				IOException ioe = new IOException("Malformed query result from server");
				ioe.initCause(e);
				throw ioe;
			}
		}
		else {
			throw new IOException("Failed to get query result from server: " + method.getStatusText());
		}
	}

	private void _getRDF(HttpMethod method, RDFHandler handler, boolean contextSerialization)
		throws IOException, RDFHandlerException
	{
		if (contextSerialization && Rio.getRDFParserRegistry().get(RDFFormat.TRIX) != null) {
			// we need context serialization, use a format that supports contexts
			method.addRequestHeader(ACCEPT_PARAM_NAME, RDFFormat.TRIX.getMIMEType());
		}
		else {
			// TODO: add format preferrence through 'q' attribute?
			for (RDFParserFactory factory : Rio.getRDFParserRegistry().getAll()) {
				method.addRequestHeader(ACCEPT_PARAM_NAME, factory.getRDFFormat().getMIMEType());
			}
		}

		int httpCode = _httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = _getResponseMIMEType(method);
			try {
				RDFFormat format = RDFFormat.forMIMEType(mimeType);
				RDFParser parser = Rio.createParser(format, getValueFactory());
				parser.setPreserveBNodeIDs(true);
				parser.setRDFHandler(handler);
				parser.parse(method.getResponseBodyAsStream(), method.getURI().getURI());
			}
			catch (UnsupportedRDFormatException e) {
				throw new IOException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (RDFParseException e) {
				IOException ioe = new IOException("Malformed query result from server");
				ioe.initCause(e);
				throw ioe;
			}
		}
		else {
			throw new IOException("Failed to get RDF from server: " + method.getStatusText());
		}
	}

	/*-----------------------------------------------*
	 * Utility methods related to Commons HttpClient *
	 *-----------------------------------------------*/

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
	private String _getResponseMIMEType(HttpMethod method)
		throws IOException
	{
		Header[] headers = method.getResponseHeaders("Content-Type");

		for (Header header : headers) {
			HeaderElement[] headerElements = header.getElements();

			for (HeaderElement headerEl : headerElements) {
				String mimeType = headerEl.getName();
				if (mimeType != null) {
					return mimeType;
				}
			}
		}

		return null;
	}

	private void _releaseConnection(HttpMethod method) {
		try {
			// Read the entire response body to enable the reuse of the connection
			InputStream responseStream = method.getResponseBodyAsStream();
			if (responseStream != null) {
				while (responseStream.read() >= 0) {
					// do nothing
				}
			}

			method.releaseConnection();
		}
		catch (IOException e) {
			logger.warn("I/O error upon releasing connection", e);
		}
	}
}
