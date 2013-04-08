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

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.HttpMethod;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.DeleteMethod;
//import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.PutMethod;
//import org.apache.commons.httpclient.methods.RequestEntity;
//import org.apache.commons.httpclient.methods.StringRequestEntity;

import info.aduna.io.IOUtil;

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
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;



/**
 * An {@link HTTPClient} subclass which bundles special functionality 
 * for Sesame remote repositories.
 *
 * @author Andreas Schwarte
 */
public class SesameHTTPClient extends HTTPClient {


	private String serverURL;
	
	

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
	
	public void setRepository(String serverURL, String repositoryID) {
		setServerURL(serverURL);
		setQueryURL(Protocol.getRepositoryLocation(serverURL, repositoryID));
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
		if (getQueryURL() == null) {
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
			else if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
				// trying to contact a non-Sesame server?
				throw new RepositoryException("Failed to get server protocol; no such resource on this server");
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

		HttpMethod method = new GetMethod(Protocol.getSizeLocation(getQueryURL()));
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

	public void deleteRepository(String repositoryID) throws HttpException, IOException, RepositoryException {
		
		HttpMethod method = new DeleteMethod(Protocol.getRepositoryLocation(serverURL, repositoryID));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_NO_CONTENT) {
				return;
			}
			else if (httpCode == HttpURLConnection.HTTP_FORBIDDEN) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new UnauthorizedException(errInfo.getErrorMessage());
			}
			else {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to delete repository: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
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

		HttpMethod method = new GetMethod(Protocol.getNamespacesLocation(getQueryURL()));
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

		HttpMethod method = new GetMethod(Protocol.getNamespacePrefixLocation(getQueryURL(), prefix));
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

		EntityEnclosingMethod method = new PutMethod(Protocol.getNamespacePrefixLocation(getQueryURL(), prefix));
		setDoAuthentication(method);
		method.setRequestEntity(new StringRequestEntity(name, "text/plain", "UTF-8"));

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!is2xx(httpCode)) {
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

		HttpMethod method = new DeleteMethod(Protocol.getNamespacePrefixLocation(getQueryURL(), prefix));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!is2xx(httpCode)) {
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

		HttpMethod method = new DeleteMethod(Protocol.getNamespacesLocation(getQueryURL()));
		setDoAuthentication(method);

		try {
			int httpCode = httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnauthorizedException();
			}
			else if (!is2xx(httpCode)) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Failed to clear namespaces: " + errInfo + " (" + httpCode + ")");
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

		GetMethod method = new GetMethod(Protocol.getContextsLocation(getQueryURL()));
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
	 * Get/add/remove statements *
	 *---------------------------*/

	public void getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws IOException, RDFHandlerException, RepositoryException, UnauthorizedException,
		QueryInterruptedException
	{
		checkRepositoryURL();

		GetMethod method = new GetMethod(Protocol.getStatementsLocation(getQueryURL()));
		setDoAuthentication(method);

		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		if (subj != null) {
			params.add(new BasicNameValuePair(Protocol.SUBJECT_PARAM_NAME, Protocol.encodeValue(subj)));
		}
		if (pred != null) {
			params.add(new BasicNameValuePair(Protocol.PREDICATE_PARAM_NAME, Protocol.encodeValue(pred)));
		}
		if (obj != null) {
			params.add(new BasicNameValuePair(Protocol.OBJECT_PARAM_NAME, Protocol.encodeValue(obj)));
		}
		for (String encodedContext : Protocol.encodeContexts(contexts)) {
			params.add(new BasicNameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		params.add(new BasicNameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred)));

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

		HttpPost method = new HttpPost(Protocol.getStatementsLocation(getQueryURL()));
		setDoAuthentication(method);

		// Create a RequestEntity for the transaction data
		method.setEntity(new RequestEntity() {

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
			else if (!is2xx(httpCode)) {
				ErrorInfo errInfo = getErrorInfo(method);
				throw new RepositoryException("Transaction failed: " + errInfo + " (" + httpCode + ")");
			}
		}
		finally {
			releaseConnection(method);
		}
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

	protected void upload(RequestEntity reqEntity, String baseURI, boolean overwrite, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException, UnauthorizedException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		checkRepositoryURL();

		String uploadURL = Protocol.getStatementsLocation(getQueryURL());

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
			params.add(new BasicNameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		if (baseURI != null && baseURI.trim().length() != 0) {
			String encodedBaseURI = Protocol.encodeValue(new URIImpl(baseURI));
			params.add(new BasicNameValuePair(Protocol.BASEURI_PARAM_NAME, encodedBaseURI));
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
			else if (!is2xx(httpCode)) {
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
}
