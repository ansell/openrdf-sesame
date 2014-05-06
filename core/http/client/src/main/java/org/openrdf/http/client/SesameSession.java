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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import info.aduna.io.IOUtil;

import org.openrdf.IsolationLevel;
import org.openrdf.OpenRDFException;
import org.openrdf.OpenRDFUtil;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.Protocol.Action;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.BasicParserSettings;

/**
 * An {@link SparqlSession} subclass which bundles special functionality for
 * Sesame remote repositories.
 * 
 * @author Andreas Schwarte
 */
public class SesameSession extends SparqlSession {

	private String serverURL;

	private String transactionURL;

	public SesameSession(HttpClient client, ExecutorService executor) {
		super(client, executor);

		// we want to preserve bnode ids to allow Sesame API methods to match
		// blank nodes.
		getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

		// Sesame client has preference for binary response formats, as these are
		// most performant
		setPreferredTupleQueryResultFormat(TupleQueryResultFormat.BINARY);
		setPreferredRDFFormat(RDFFormat.BINARY);
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

	public String getRepositoryURL() {
		return this.getQueryURL();
	}

	public void setRepository(String repositoryURL) {
		// Try to parse the server URL from the repository URL
		Pattern urlPattern = Pattern.compile("(.*)/" + Protocol.REPOSITORIES + "/[^/]*/?");
		Matcher matcher = urlPattern.matcher(repositoryURL);

		if (matcher.matches() && matcher.groupCount() == 1) {
			setServerURL(matcher.group(1));
		}

		setQueryURL(repositoryURL);
	}

	protected void checkRepositoryURL() {
		if (getRepositoryURL() == null) {
			throw new IllegalStateException("Repository URL has not been set");
		}
	}

	protected void checkServerURL() {
		if (serverURL == null) {
			throw new IllegalStateException("Server URL has not been set");
		}
	}

	@Override
	public String getUpdateURL() {
		return Protocol.getStatementsLocation(getQueryURL());
	}

	/*-----------------*
	 * Repository list *
	 *-----------------*/

	public TupleQueryResult getRepositoryList()
		throws IOException, RepositoryException, UnauthorizedException, QueryInterruptedException
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
		throws IOException, TupleQueryResultHandlerException, RepositoryException, UnauthorizedException,
		QueryInterruptedException
	{
		checkServerURL();

		HttpGet method = new HttpGet(Protocol.getRepositoriesLocation(serverURL));

		try {
			getTupleQueryResult(method, handler);
		}
		catch (MalformedQueryException e) {
			// This shouldn't happen as no queries are involved
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	/*------------------*
	 * Protocol version *
	 *------------------*/

	public String getServerProtocol()
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkServerURL();

		HttpGet method = new HttpGet(Protocol.getProtocolLocation(serverURL));

		try {
			return EntityUtils.toString(executeOK(method).getEntity());
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
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

		try {
			URIBuilder url = new URIBuilder(Protocol.getSizeLocation(getQueryURL()));
			for (int i = 0; i < encodedContexts.length; i++) {
				url.addParameter(Protocol.CONTEXT_PARAM_NAME, encodedContexts[i]);
			}

			HttpUriRequest method = new HttpGet(url.build());

			String response = EntityUtils.toString(executeOK(method).getEntity());
			try {
				return Long.parseLong(response);
			}
			catch (NumberFormatException e) {
				throw new RepositoryException("Server responded with invalid size value: " + response);
			}
		}
		catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	public void deleteRepository(String repositoryID)
		throws IOException, RepositoryException
	{

		HttpUriRequest method = new HttpDelete(Protocol.getRepositoryLocation(serverURL, repositoryID));

		try {
			executeNoContent(method);
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	/*---------------------------*
	 * Get/add/remove namespaces *
	 *---------------------------*/

	public TupleQueryResult getNamespaces()
		throws IOException, RepositoryException, UnauthorizedException, QueryInterruptedException
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
		throws IOException, TupleQueryResultHandlerException, RepositoryException, UnauthorizedException,
		QueryInterruptedException
	{
		checkRepositoryURL();

		HttpUriRequest method = new HttpGet(Protocol.getNamespacesLocation(getQueryURL()));

		try {
			getTupleQueryResult(method, handler);
		}
		catch (MalformedQueryException e) {
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	public String getNamespace(String prefix)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpUriRequest method = new HttpGet(Protocol.getNamespacePrefixLocation(getQueryURL(), prefix));

		try {
			HttpResponse response = execute(method);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
				return EntityUtils.toString(response.getEntity());
			}
			else {
				EntityUtils.consume(response.getEntity());
				return null;
			}
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	public void setNamespacePrefix(String prefix, String name)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpPut method = new HttpPut(Protocol.getNamespacePrefixLocation(getQueryURL(), prefix));
		method.setEntity(new StringEntity(name, ContentType.create("text/plain", "UTF-8")));

		try {
			executeNoContent(method);
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	public void removeNamespacePrefix(String prefix)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpUriRequest method = new HttpDelete(Protocol.getNamespacePrefixLocation(getQueryURL(), prefix));

		try {
			executeNoContent(method);
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	public void clearNamespaces()
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpUriRequest method = new HttpDelete(Protocol.getNamespacesLocation(getQueryURL()));

		try {
			executeNoContent(method);
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	/*-------------*
	 * Context IDs *
	 *-------------*/

	public TupleQueryResult getContextIDs()
		throws IOException, RepositoryException, UnauthorizedException, QueryInterruptedException
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
		throws IOException, TupleQueryResultHandlerException, RepositoryException, UnauthorizedException,
		QueryInterruptedException
	{
		checkRepositoryURL();

		HttpGet method = new HttpGet(Protocol.getContextsLocation(getQueryURL()));

		try {
			getTupleQueryResult(method, handler);
		}
		catch (MalformedQueryException e) {
			logger.warn("Server reported unexpected malfored query error", e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	/*---------------------------*
	 * Get/add/remove statements *
	 *---------------------------*/

	public void getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws IOException, RDFHandlerException, RepositoryException, UnauthorizedException,
		QueryInterruptedException
	{
		checkRepositoryURL();

		try {
			final boolean useTransaction = transactionURL != null;

			String baseLocation = useTransaction ? transactionURL
					: Protocol.getStatementsLocation(getQueryURL());
			URIBuilder url = new URIBuilder(baseLocation);

			if (subj != null) {
				url.setParameter(Protocol.SUBJECT_PARAM_NAME, Protocol.encodeValue(subj));
			}
			if (pred != null) {
				url.setParameter(Protocol.PREDICATE_PARAM_NAME, Protocol.encodeValue(pred));
			}
			if (obj != null) {
				url.setParameter(Protocol.OBJECT_PARAM_NAME, Protocol.encodeValue(obj));
			}
			for (String encodedContext : Protocol.encodeContexts(contexts)) {
				url.addParameter(Protocol.CONTEXT_PARAM_NAME, encodedContext);
			}
			url.setParameter(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred));
			if (useTransaction) {
				url.setParameter(Protocol.ACTION_PARAM_NAME, Action.GET.toString());
			}

			HttpUriRequest method = useTransaction ? new HttpPut(url.build()) : new HttpGet(url.build());

			try {
				getRDF(method, handler, true);
			}
			catch (MalformedQueryException e) {
				logger.warn("Server reported unexpected malfored query error", e);
				throw new RepositoryException(e.getMessage(), e);
			}
		}
		catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	public synchronized void beginTransaction(IsolationLevel isolationLevel)
		throws OpenRDFException, IOException, UnauthorizedException
	{
		checkRepositoryURL();

		if (transactionURL != null) {
			throw new IllegalStateException("Transaction URL is already set");
		}

		HttpPost method = new HttpPost(Protocol.getTransactionsLocation(getRepositoryURL()));

		// TODO add isolation level to payload

		HttpResponse response = execute(method);
		int code = response.getStatusLine().getStatusCode();

		try {
			if (code == HttpURLConnection.HTTP_CREATED) {
				transactionURL = response.getFirstHeader("Location").getValue();
				if (transactionURL == null) {
					throw new RepositoryException("no valid transaction ID received in server response.");
				}
			}
			else {
				throw new RepositoryException("unable to start transaction. HTTP error code " + code);
			}
		}
		finally {
			EntityUtils.consume(response.getEntity());
		}
	}

	public synchronized void commitTransaction()
		throws OpenRDFException, IOException, UnauthorizedException
	{
		checkRepositoryURL();

		if (transactionURL == null) {
			throw new IllegalStateException("Transaction URL has not been set");
		}

		HttpPut method = null;
		try {
			URIBuilder url = new URIBuilder(transactionURL);
			url.addParameter(Protocol.ACTION_PARAM_NAME, Action.COMMIT.toString());
			method = new HttpPut(url.build());
		}
		catch (URISyntaxException e) {
			logger.error("could not create URL for transaction commit", e);
			throw new RuntimeException(e);
		}

		final HttpResponse response = execute(method);
		try {
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpURLConnection.HTTP_OK) {
				// we're done.
				transactionURL = null;
			}
			else {
				throw new RepositoryException("unable to commit transaction. HTTP error code " + code);
			}
		}
		finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}

	}

	public synchronized void rollbackTransaction()
		throws OpenRDFException, IOException, UnauthorizedException
	{
		checkRepositoryURL();

		if (transactionURL == null) {
			throw new IllegalStateException("Transaction URL has not been set");
		}

		String requestURL = appendAction(transactionURL, Action.ROLLBACK);
		HttpPut method = new HttpPut(requestURL);

		final HttpResponse response = execute(method);
		try {
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpURLConnection.HTTP_OK) {
				// we're done.
				transactionURL = null;
			}
			else {
				throw new RepositoryException("unable to rollback transaction. HTTP error code " + code);
			}
		}
		finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	/**
	 * Appends the action as a parameter to the supplied url
	 * 
	 * @param url
	 *        a url on which to append the parameter. it is assumed the url has
	 *        no parameters.
	 * @param action
	 *        the action to add as a parameter
	 * @return the url parametrized with the supplied action
	 */
	private String appendAction(String url, Action action) {
		return url + "?" + Protocol.ACTION_PARAM_NAME + "=" + action.toString();
	}

	/**
	 * Sends a transaction list as serialized XML to the server.
	 * 
	 * @deprecated since 2.8.0
	 * @param txn
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws UnauthorizedException
	 */
	@Deprecated
	public void sendTransaction(final Iterable<? extends TransactionOperation> txn)
		throws IOException, RepositoryException, UnauthorizedException
	{
		checkRepositoryURL();

		HttpPost method = new HttpPost(Protocol.getStatementsLocation(getQueryURL()));

		// Create a RequestEntity for the transaction data
		method.setEntity(new AbstractHttpEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public Header getContentType() {
				return new BasicHeader("Content-Type", Protocol.TXN_MIME_TYPE);
			}

			public boolean isRepeatable() {
				return true;
			}

			public boolean isStreaming() {
				return true;
			}

			public InputStream getContent()
				throws IOException, IllegalStateException
			{
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				writeTo(buf);
				return new ByteArrayInputStream(buf.toByteArray());
			}

			public void writeTo(OutputStream out)
				throws IOException
			{
				TransactionWriter txnWriter = new TransactionWriter();
				txnWriter.serialize(txn, out);
			}
		});

		try {
			executeNoContent(method);
		}
		catch (RepositoryException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
	}

	public void addData(InputStream contents, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws UnauthorizedException, RDFParseException, RepositoryException, IOException
	{
		upload(contents, baseURI, dataFormat, false, true, Action.ADD, contexts);
	}

	public void removeData(InputStream contents, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws UnauthorizedException, RDFParseException, RepositoryException, IOException
	{
		upload(contents, baseURI, dataFormat, false, true, Action.DELETE, contexts);
	}

	public void upload(InputStream contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			boolean preserveNodeIds, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		upload(contents, baseURI, dataFormat, overwrite, preserveNodeIds, null, contexts);
	}

	protected void upload(InputStream contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			boolean preserveNodeIds, Action action, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		// Set Content-Length to -1 as we don't know it and we also don't want to
		// cache
		HttpEntity entity = new InputStreamEntity(contents, -1,
				ContentType.parse(dataFormat.getDefaultMIMEType()));
		upload(entity, baseURI, overwrite, preserveNodeIds, action, contexts);
	}

	public void upload(final Reader contents, String baseURI, final RDFFormat dataFormat, boolean overwrite,
			boolean preserveNodeIds, Resource... contexts)
		throws UnauthorizedException, RDFParseException, RepositoryException, IOException
	{
		upload(contents, baseURI, dataFormat, overwrite, preserveNodeIds, null, contexts);
	}

	@Override
	protected HttpUriRequest getQueryMethod(QueryLanguage ql, String query, String baseURI, Dataset dataset,
			boolean includeInferred, int maxQueryTime, Binding... bindings)
	{
		String requestURL = transactionURL != null ? appendAction(transactionURL, Action.QUERY) : getQueryURL();

		HttpEntityEnclosingRequest method = null;
		if (transactionURL == null) {
			method = new HttpPost(requestURL);
		}
		else {
			method = new HttpPut(requestURL);
		}

		method.setHeader("Content-Type", Protocol.FORM_MIME_TYPE + "; charset=utf-8");

		List<NameValuePair> queryParams = getQueryMethodParameters(ql, query, baseURI, dataset,
				includeInferred, maxQueryTime, bindings);

		method.setEntity(new UrlEncodedFormEntity(queryParams, UTF8));

		return (HttpUriRequest)method;
	}

	@Override
	protected HttpUriRequest getUpdateMethod(QueryLanguage ql, String update, String baseURI, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		String requestURL = transactionURL != null ? appendAction(transactionURL, Action.UPDATE)
				: getUpdateURL();

		HttpEntityEnclosingRequest method = null;
		// if (transactionURL == null) {
		method = new HttpPost(requestURL);
		// }
		// else {
		// method = new HttpPut(requestURL);
		// }

		method.setHeader("Content-Type", Protocol.FORM_MIME_TYPE + "; charset=utf-8");

		List<NameValuePair> queryParams = getUpdateMethodParameters(ql, update, baseURI, dataset,
				includeInferred, bindings);

		method.setEntity(new UrlEncodedFormEntity(queryParams, UTF8));

		return (HttpUriRequest)method;
	}

	protected void upload(final Reader contents, String baseURI, final RDFFormat dataFormat,
			boolean overwrite, boolean preserveNodeIds, Action action, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		final Charset charset = dataFormat.hasCharset() ? dataFormat.getCharset() : Charset.forName("UTF-8");

		HttpEntity entity = new AbstractHttpEntity() {

			private InputStream content;

			public long getContentLength() {
				return -1; // don't know
			}

			public Header getContentType() {
				return new BasicHeader("Content-Type", dataFormat.getDefaultMIMEType() + "; charset="
						+ charset.name());
			}

			public boolean isRepeatable() {
				return false;
			}

			public boolean isStreaming() {
				return true;
			}

			public synchronized InputStream getContent()
				throws IOException, IllegalStateException
			{
				if (content == null) {
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					writeTo(buf);
					content = new ByteArrayInputStream(buf.toByteArray());
				}
				return content;
			}

			public void writeTo(OutputStream out)
				throws IOException
			{
				try {
					OutputStreamWriter writer = new OutputStreamWriter(out, charset);
					IOUtil.transfer(contents, writer);
					writer.flush();
				}
				finally {
					contents.close();
				}
			}
		};

		upload(entity, baseURI, overwrite, preserveNodeIds, action, contexts);
	}

	protected void upload(HttpEntity reqEntity, String baseURI, boolean overwrite, boolean preserveNodeIds,
			Action action, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		checkRepositoryURL();

		boolean useTransaction = transactionURL != null;

		try {

			String baseLocation = useTransaction ? transactionURL
					: Protocol.getStatementsLocation(getQueryURL());
			URIBuilder url = new URIBuilder(baseLocation);

			// Set relevant query parameters
			for (String encodedContext : Protocol.encodeContexts(contexts)) {
				url.addParameter(Protocol.CONTEXT_PARAM_NAME, encodedContext);
			}
			if (baseURI != null && baseURI.trim().length() != 0) {
				String encodedBaseURI = Protocol.encodeValue(new URIImpl(baseURI));
				url.setParameter(Protocol.BASEURI_PARAM_NAME, encodedBaseURI);
			}
			if (preserveNodeIds) {
				url.setParameter(Protocol.PRESERVE_BNODE_ID_PARAM_NAME, "true");
			}

			if (useTransaction) {
				if (action == null) {
					throw new IllegalArgumentException("action can not be null on transaction operation");
				}
				url.setParameter(Protocol.ACTION_PARAM_NAME, action.toString());
			}

			// Select appropriate HTTP method
			HttpEntityEnclosingRequest method;
			if (overwrite || useTransaction) {
				method = new HttpPut(url.build());
			}
			else {
				method = new HttpPost(url.build());
			}

			// Set payload
			method.setEntity(reqEntity);

			// Send request
			try {
				executeNoContent((HttpUriRequest)method);
			}
			catch (RepositoryException e) {
				throw e;
			}
			catch (RDFParseException e) {
				throw e;
			}
			catch (OpenRDFException e) {
				throw new RepositoryException(e);
			}
		}
		catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void setUsernameAndPassword(String username, String password) {
		checkServerURL();
		setUsernameAndPasswordForUrl(username, password, getServerURL());
	}
}
