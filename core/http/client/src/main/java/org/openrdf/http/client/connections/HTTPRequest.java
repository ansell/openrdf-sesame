/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.connections;

import static info.aduna.net.http.EntityHeaders.CONTENT_ENCODING;
import static info.aduna.net.http.EntityHeaders.CONTENT_TYPE;
import static info.aduna.net.http.GeneralHeaders.CACHE_CONTROL;
import static info.aduna.net.http.MimeTypes.FORM_MIME_TYPE;
import static info.aduna.net.http.RequestHeaders.ACCEPT;
import static info.aduna.net.http.RequestHeaders.ACCEPT_ENCODING;
import static info.aduna.net.http.RequestHeaders.IF_NONE_MATCH;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import info.aduna.io.IOUtil;
import info.aduna.lang.FileFormat;

import org.openrdf.http.client.helpers.BackgroundGraphResult;
import org.openrdf.http.client.helpers.BackgroundTupleResult;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.cas.CasParseException;
import org.openrdf.http.protocol.cas.ProxyFailure;
import org.openrdf.http.protocol.cas.ProxyGrantingTicketRegistry;
import org.openrdf.http.protocol.cas.ProxySuccess;
import org.openrdf.http.protocol.cas.ServiceResponse;
import org.openrdf.http.protocol.cas.ServiceResponseParser;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParserRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultParserRegistry;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.result.GraphResult;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;

/**
 * Serialises Java Objects over an HTTP connection.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class HTTPRequest {

	/**
	 * Keeps track of session cookies. The outer map binds the info to the local
	 * session. The inner map stores the server's session URL as it's key and the
	 * session ID as it's value.
	 */
	private static final WeakHashMap<Session, Map<String, String>> sessionMap = new WeakHashMap<Session, Map<String, String>>();

	private final Logger logger = LoggerFactory.getLogger(HTTPRequest.class);

	private final HTTPConnectionPool pool;

	private final HttpMethodBase method;

	private volatile boolean released;

	public HTTPRequest(HTTPConnectionPool pool, HttpMethodBase method) {
		this.pool = pool;
		this.method = method;
	}

	public void ifNoneMatch(String match) {
		method.addRequestHeader(IF_NONE_MATCH, match);
	}

	public void accept(Class<?> type)
		throws NoCompatibleMediaType
	{
		if (String.class.isAssignableFrom(type)) {
			acceptString();
		}
		else if (Model.class.isAssignableFrom(type)) {
			acceptRDF(false);
		}
		else {
			throw new NoCompatibleMediaType("No parsers are available for " + type);
		}
	}

	public void acceptString() {
		method.addRequestHeader(ACCEPT, "text/plain");
	}

	public void acceptLong() {
		acceptString();
	}

	public void acceptBoolean()
		throws NoCompatibleMediaType
	{
		if (pool.isPreferredBooleanQueryResultFormatUsed()) {
			FileFormat format = pool.getPreferredBooleanQueryResultFormat();
			method.addRequestHeader(ACCEPT, format.getDefaultMIMEType());
			return;
		}
		// Specify which formats we support using Accept headers
		Set<BooleanQueryResultFormat> booleanFormats = BooleanQueryResultParserRegistry.getInstance().getKeys();
		if (booleanFormats.isEmpty()) {
			throw new NoCompatibleMediaType("No boolean query result parsers have been registered");
		}

		for (BooleanQueryResultFormat format : booleanFormats) {
			// Determine a q-value that reflects the user specified preference
			int qValue = 10;

			TupleQueryResultFormat preferredBQRFormat = pool.getPreferredTupleQueryResultFormat();
			if (preferredBQRFormat != null && !preferredBQRFormat.equals(format)) {
				// Prefer specified format over other formats
				qValue -= 2;
			}

			for (String mimeType : format.getMIMETypes()) {
				String acceptParam = mimeType;

				if (qValue < 10) {
					acceptParam += ";q=0." + qValue;
				}

				method.addRequestHeader(ACCEPT, acceptParam);
			}
		}
	}

	public void acceptTupleQueryResult()
		throws NoCompatibleMediaType
	{
		if (pool.isPreferredTupleQueryResultFormatUsed()) {
			FileFormat format = pool.getPreferredTupleQueryResultFormat();
			method.addRequestHeader(ACCEPT, format.getDefaultMIMEType());
			return;
		}
		// Specify which formats we support using Accept headers
		Set<TupleQueryResultFormat> tqrFormats = TupleQueryResultParserRegistry.getInstance().getKeys();
		if (tqrFormats.isEmpty()) {
			throw new NoCompatibleMediaType("No tuple query result parsers have been registered");
		}

		for (TupleQueryResultFormat format : tqrFormats) {
			// Determine a q-value that reflects the user specified preference
			int qValue = 10;

			TupleQueryResultFormat preferredTQRFormat = pool.getPreferredTupleQueryResultFormat();
			if (preferredTQRFormat != null && !preferredTQRFormat.equals(format)) {
				// Prefer specified format over other formats
				qValue -= 2;
			}

			for (String mimeType : format.getMIMETypes()) {
				String acceptParam = mimeType;

				if (qValue < 10) {
					acceptParam += ";q=0." + qValue;
				}

				method.addRequestHeader(ACCEPT, acceptParam);
			}
		}
	}

	public void acceptGraphQueryResult()
		throws NoCompatibleMediaType
	{
		if (pool.isPreferredTupleQueryResultFormatUsed()) {
			FileFormat format = pool.getPreferredTupleQueryResultFormat();
			method.addRequestHeader(ACCEPT, format.getDefaultMIMEType());
			return;
		}
		acceptRDF(false);
	}

	public void acceptRDF(boolean requireContext)
		throws NoCompatibleMediaType
	{
		// Specify which formats we support using Accept headers
		Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
		if (rdfFormats.isEmpty()) {
			throw new NoCompatibleMediaType("No tuple RDF parsers have been registered");
		}

		for (RDFFormat format : rdfFormats) {
			// Determine a q-value that reflects the necessity of context
			// support and the user specified preference
			int qValue = 10;

			if (requireContext && !format.supportsContexts()) {
				// Prefer context-supporting formats over pure triple-formats
				qValue -= 5;
			}

			RDFFormat preferredRDFFormat = pool.getPreferredRDFFormat();
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

				method.addRequestHeader(ACCEPT, acceptParam);
			}
		}
	}

	public void send(Object instance) {
		if (instance instanceof String) {
			sendString((String)instance);
		}
		else if (instance instanceof Model) {
			sendModel((Model)instance);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	public void sendString(String plain) {
		try {
			((EntityEnclosingMethod)method).setRequestEntity(new StringRequestEntity(plain, "text/plain",
					"UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public void sendModel(final Model model) {
		final RDFFormat dataFormat = pool.getPreferredRDFFormat();
		final Charset charset = dataFormat.hasCharset() ? dataFormat.getCharset() : Charset.forName("UTF-8");
		final RDFWriterFactory factory = RDFWriterRegistry.getInstance().get(dataFormat);
		sendEntity(new RequestEntity() {

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
				RDFWriter rdf = factory.getWriter(writer);
				try {
					rdf.startRDF();
					for (Map.Entry<String, String> ns : model.getNamespaces().entrySet()) {
						rdf.handleNamespace(ns.getKey(), ns.getValue());
					}
					for (Statement st : model) {
						rdf.handleStatement(st);
					}
					rdf.endRDF();
				}
				catch (RDFHandlerException e) {
					if (e.getCause() instanceof IOException) {
						throw (IOException)e.getCause();
					}
					IOException ioe = new IOException(e.getMessage());
					ioe.initCause(e);
					throw ioe;
				}
				writer.flush();
			}
		});
	}

	public void sendForm(List<NameValuePair> queryParams) {
		sendForm(queryParams.toArray(new NameValuePair[queryParams.size()]));
	}

	public void sendForm(NameValuePair... queryParams) {
		method.setRequestHeader(CONTENT_TYPE, FORM_MIME_TYPE + "; charset=utf-8");
		((PostMethod)method).setRequestBody(queryParams);
	}

	public void sendQueryString(List<NameValuePair> params) {
		sendQueryString(params.toArray(new NameValuePair[params.size()]));
	}

	public void sendQueryString(NameValuePair... params) {
		String queryString = EncodingUtil.formUrlEncode(params, "UTF-8");
		if (queryString.length() < 1024) {
			method.setQueryString(queryString);
		}
		else {
			sendForm(params);
		}
	}

	public void sendEntity(RequestEntity requestEntity) {
		((EntityEnclosingMethod)method).setRequestEntity(requestEntity);
	}

	public void execute()
		throws IOException, HTTPException
	{
		method.setRequestHeader(ACCEPT_ENCODING, "gzip");

		setSessionCookie();

		int statusCode = pool.executeMethod(method);

		if (statusCode >= 400) {
			String body;
			if ("HEAD".equals(method.getName())) {
				body = method.getStatusLine().getReasonPhrase();
			}
			else {
				body = getResponseBodyAsString();
			}
			release();
			throw HTTPException.create(statusCode, body);
		}
	}

	private void setSessionCookie() {
		Session session = SessionManager.get();

		if (session == null) {
			// no session, no cookie
			return;
		}

		String sessionURL = Protocol.getSessionLocation(pool.getServerURL());
		String sessionID = null;

		Map<String,String> sessionHostMap = sessionMap.get(session);
		if (sessionHostMap != null) {
			sessionID = sessionHostMap.get(sessionURL);
		}

		if (sessionID == null) {
			// Try to authenticate
			String casServer = System.getProperty("org.openrdf.auth.cas.server");
			String pgt = ProxyGrantingTicketRegistry.getProxyGrantingTicket(session);

			if (casServer != null && pgt != null) {
				// Got a proxy granting ticket for a CAS server
				String proxyTicket = getProxyTicket(casServer, pgt, sessionURL);
				if (proxyTicket != null) {
					sessionID = startRemoteSession(sessionURL, proxyTicket, session);
					if (sessionID != null) {
						if (sessionHostMap == null) {
							sessionHostMap = new HashMap<String, String>();
							sessionMap.put(session, sessionHostMap);
						}
						sessionHostMap.put(sessionURL, sessionID);
					}
				}
			}
		}

		if (sessionID != null) {
			method.setRequestHeader("Cookie", Protocol.SESSION_COOKIE + "=" + sessionID);
		}
	}

	private String getProxyTicket(String casServer, String pgt, String targetServiceURL) {
		try {
			NameValuePair pgtParam = new NameValuePair("pgt", pgt);
			NameValuePair serviceParam = new NameValuePair("targetService", targetServiceURL);

			GetMethod proxyMethod = new GetMethod(casServer + "proxy");
			proxyMethod.setQueryString(new NameValuePair[] { pgtParam, serviceParam });

			int status = pool.httpClient.executeMethod(proxyMethod);

			if (status == HttpStatus.SC_OK) {
				// Do not use raw stream since that will ignore the response's
				// character encoding
				String casResponseStr = proxyMethod.getResponseBodyAsString();
				ServiceResponse casResponse = ServiceResponseParser.parse(casResponseStr);
				if (casResponse instanceof ProxySuccess) {
					return ((ProxySuccess)casResponse).getProxyTicket();
				}
				else if (casResponse instanceof ProxyFailure) {
					ProxyFailure pf = (ProxyFailure)casResponse;
					logger.warn("Failed to acquire proxy ticket: {} \"{}\"", pf.getCode(), pf.getMessage());
				}
				else {
					logger.warn("Unexpected response from CAS server: {}", casResponse);
				}
			}
		}
		catch (IOException e) {
			logger.warn("Failed to acquire proxy ticket", e);
		}
		catch (CasParseException e) {
			logger.warn("Failed to parse response from CAS server", e);
		}
		catch (SAXException e) {
			logger.warn("Failed to parse response from CAS server", e);
		}
		catch (ParserConfigurationException e) {
			logger.warn("Failed to parse response from CAS server", e);
		}

		return null;
	}

	private String startRemoteSession(String sessionURL, String proxyTicket, Session localSession) {
		try {
			NameValuePair ticketParam = new NameValuePair("ticket", proxyTicket);
			GetMethod sessionMethod = new GetMethod(sessionURL);
			sessionMethod.setQueryString(new NameValuePair[] { ticketParam });

			int status = pool.httpClient.executeMethod(sessionMethod);

			if (status == HttpStatus.SC_OK) {
				for (Header header : sessionMethod.getResponseHeaders("Set-Cookie")) {
					for (HeaderElement headerEl : header.getElements()) {
						if (Protocol.SESSION_COOKIE.equals(headerEl.getName())) {
							return headerEl.getValue();
						}
					}
				}
			}
		}
		catch (IOException e) {
			logger.warn("Failed to start session on " + sessionURL, e);
		}

		return null;
	}

	public boolean isNotModified() {
		return method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED;
	}

	public InputStream getResponseBodyAsStream()
		throws IOException
	{
		InputStream stream = method.getResponseBodyAsStream();

		boolean useGZip = false;

		for (Header encodingHeader : method.getResponseHeaders(CONTENT_ENCODING)) {
			for (HeaderElement el : encodingHeader.getElements()) {
				if (el.getName().equalsIgnoreCase("gzip")) {
					useGZip = true;
				}
				else {
					throw new IOException("Server replied with unsupported content encoding: " + el.getName());
				}
			}
		}

		if (useGZip) {
			stream = new GZIPInputStream(stream);
		}

		return stream;
	}

	public Reader getResponseBodyAsReader()
		throws IOException
	{
		String charset = method.getResponseCharSet();
		if (charset == null) {
			charset = "ISO-8859-1";
		}
		InputStream stream = getResponseBodyAsStream();
		return new InputStreamReader(stream, charset);
	}

	public String getResponseBodyAsString()
		throws IOException
	{
		return IOUtil.readString(getResponseBodyAsReader());
	}

	public <T> T read(Class<T> type)
		throws IOException, NumberFormatException, QueryResultParseException, RDFParseException,
		NoCompatibleMediaType
	{
		if (String.class.isAssignableFrom(type)) {
			return type.cast(readString());
		}
		else if (Model.class.isAssignableFrom(type)) {
			return type.cast(readModel());
		}
		else {
			throw new NoCompatibleMediaType("Cannot read " + type);
		}
	}

	public String readString()
		throws IOException
	{
		if (method.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
			return null;
		}
		return getResponseBodyAsString();
	}

	public long readLong()
		throws IOException, NumberFormatException
	{
		return Long.parseLong(getResponseBodyAsString());
	}

	public boolean readBoolean()
		throws IOException, QueryResultParseException, NoCompatibleMediaType
	{
		Boolean result = null;
		String mimeType = readContentType();
		try {
			BooleanQueryResultFormat format = BooleanQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(
					mimeType);
			result = QueryResultIO.parse(getResponseBodyAsStream(), format);
			return result;
		}
		catch (UnsupportedQueryResultFormatException e) {
			logger.warn(e.toString(), e);
			throw new NoCompatibleMediaType("Server responded with an unsupported file format: " + mimeType);
		}
		finally {
			if (result == null) {
				abort();
			}
		}
	}

	public TupleResult getTupleQueryResult()
		throws IOException, QueryResultParseException, NoCompatibleMediaType
	{
		BackgroundTupleResult result = null;
		String mimeType = readContentType();
		try {
			TupleQueryResultFormat format = TupleQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(
					mimeType);
			TupleQueryResultParser parser = QueryResultIO.createParser(format, pool.getValueFactory());
			InputStream in = getResponseBodyAsStream();
			result = new BackgroundTupleResult(parser, in, this);
			pool.executeTask(result);
			return result;
		}
		catch (UnsupportedQueryResultFormatException e) {
			logger.warn(e.toString(), e);
			throw new NoCompatibleMediaType("Server responded with an unsupported file format: " + mimeType);
		}
		finally {
			if (result == null) {
				abort();
			}
		}
	}

	public void readTupleQueryResult(TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, IOException, QueryResultParseException, NoCompatibleMediaType
	{
		String mimeType = readContentType();
		try {
			TupleQueryResultFormat format = TupleQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(
					mimeType);
			QueryResultIO.parse(getResponseBodyAsStream(), format, handler, pool.getValueFactory());
		}
		catch (UnsupportedQueryResultFormatException e) {
			logger.warn(e.toString(), e);
			throw new NoCompatibleMediaType("Server responded with an unsupported file format: " + mimeType);
		}
	}

	public Model readModel()
		throws IOException, RDFParseException, NoCompatibleMediaType
	{
		Model model = new LinkedHashModel();
		try {
			readRDF(new StatementCollector(model));
		}
		catch (RDFHandlerException e) {
			throw new AssertionError(e);
		}
		return model;
	}

	public GraphResult getGraphQueryResult()
		throws IOException, RDFParseException, NoCompatibleMediaType
	{
		BackgroundGraphResult result = null;
		String mimeType = readContentType();
		try {
			RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(mimeType);
			RDFParser parser = Rio.createParser(format, pool.getValueFactory());
			parser.setPreserveBNodeIDs(true);
			InputStream in = getResponseBodyAsStream();
			String base = method.getURI().getURI();
			result = new BackgroundGraphResult(parser, in, base, this);
			pool.executeTask(result);
			return result;
		}
		catch (UnsupportedRDFormatException e) {
			logger.warn(e.toString(), e);
			throw new NoCompatibleMediaType("Server responded with an unsupported file format: " + mimeType);
		}
		finally {
			if (result == null) {
				abort();
			}
		}
	}

	public void readRDF(RDFHandler handler)
		throws RDFHandlerException, IOException, RDFParseException, NoCompatibleMediaType
	{
		String mimeType = readContentType();
		try {
			RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(mimeType);
			RDFParser parser = Rio.createParser(format, pool.getValueFactory());
			parser.setPreserveBNodeIDs(true);
			parser.setRDFHandler(handler);
			parser.parse(getResponseBodyAsStream(), method.getURI().getURI());
		}
		catch (UnsupportedRDFormatException e) {
			logger.warn(e.toString(), e);
			throw new NoCompatibleMediaType("Server responded with an unsupported file format: " + mimeType);
		}
	}

	public void release() {
		if (released) {
			return;
		}

		released = true;

		if (Thread.currentThread().isInterrupted()) {
			method.abort();
		}
		else {
			method.releaseConnection();
		}
	}

	public void abort() {
		method.abort();
	}

	public String readQueryType() {
		return readHeader(Protocol.X_QUERY_TYPE);
	}

	public String readETag() {
		return readHeader("ETag");
	}

	public String readLocation() {
		return readHeader("Location");
	}

	public int readMaxAge() {
		Header[] headers = method.getResponseHeaders(CACHE_CONTROL);

		for (Header header : headers) {
			HeaderElement[] headerElements = header.getElements();

			for (HeaderElement headerEl : headerElements) {
				String name = headerEl.getName();
				if ("max-age".equals(name)) {
					try {
						return Integer.parseInt(headerEl.getValue());
					}
					catch (NumberFormatException e) {
						logger.warn(e.toString(), e);
						return 0;
					}
				}
			}
		}

		return 0;
	}

	/**
	 * Gets the MIME type specified in the response headers of the supplied
	 * method, if any. For example, if the response headers contain
	 * <tt>Content-Type: application/xml;charset=UTF-8</tt>, this method will
	 * return <tt>application/xml</tt> as the MIME type.
	 * 
	 * @return The response MIME type, or <tt>null</tt> if not available.
	 */
	private String readContentType() {
		String mimeType = readHeader("Content-Type");
		if (mimeType != null) {
			logger.debug("reponse MIME type is {}", mimeType);
			return mimeType;
		}
		return null;
	}

	private String readHeader(String headerName) {
		Header[] headers = method.getResponseHeaders(headerName);

		for (Header header : headers) {
			for (HeaderElement headerEl : header.getElements()) {
				String name = headerEl.getName();
				if (name != null) {
					return name;
				}
			}
		}

		return null;
	}
}
