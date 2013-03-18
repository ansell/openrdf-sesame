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
package org.openrdf.workbench.base;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;
import info.aduna.io.MavenUtil;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.BasicQueryWriterSettings;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.workbench.util.TupleResultBuilder;

public abstract class BaseServlet implements Servlet {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected static final String SERVER_USER = "server-user";

	protected static final String SERVER_PASSWORD = "server-password";

	protected static final String ACCEPT = "Accept";

	protected static final String APPLICATION_XML = "application/xml";

	protected static final String APPLICATION_SPARQL_RESULTS_XML = "application/sparql-results+xml";

	protected static final String TEXT_HTML = "text/html";

	protected static final String USER_AGENT = "User-Agent";

	protected static final String MSIE = "MSIE";

	protected static final String MOZILLA = "Mozilla";

	protected ServletConfig config;

	protected AppConfiguration appConfig;

	public ServletConfig getServletConfig() {
		return config;
	}

	public String getServletInfo() {
		return getClass().getSimpleName();
	}

	public void init(final ServletConfig config)
		throws ServletException
	{
		this.config = config;
		this.appConfig = new AppConfiguration("openrdf-workbench", "OpenRDF Sesame Workbench",
				AppVersion.parse(MavenUtil.loadVersion("org.openrdf.sesame", "sesame-http-workbench", "dev")));
		try {
			// Suppress loading of log configuration.
			this.appConfig.init(false);
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
	}

	public void destroy() {
	}

	public final void service(final ServletRequest req, final ServletResponse resp)
		throws ServletException, IOException
	{
		final HttpServletRequest hreq = (HttpServletRequest)req;
		final HttpServletResponse hresp = (HttpServletResponse)resp;
		service(hreq, hresp);
	}

	public void service(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		// default empty implementation
	}

	protected QueryResultFormat getTupleResultFormat(final HttpServletRequest req, final ServletResponse resp)
	{
		String header = req.getHeader(ACCEPT);

		if (header != null) {
			TupleQueryResultFormat tupleFormat = QueryResultIO.getParserFormatForFileName(header);
			if (tupleFormat != null) {
				return tupleFormat;
			}
		}

		return null;
	}

	protected QueryResultFormat getBooleanResultFormat(final HttpServletRequest req, final ServletResponse resp)
	{
		String header = req.getHeader(ACCEPT);
		if (header != null) {
			// Then try boolean format
			BooleanQueryResultFormat booleanFormat = QueryResultIO.getBooleanParserFormatForMIMEType(header);
			if (booleanFormat != null) {
				return booleanFormat;
			}
		}

		return null;
	}

	protected QueryResultWriter getResultWriter(final HttpServletRequest req, final ServletResponse resp,
			final OutputStream outputStream)
		throws UnsupportedQueryResultFormatException, IOException
	{
		QueryResultFormat resultFormat = getTupleResultFormat(req, resp);

		if (resultFormat == null) {
			resultFormat = getBooleanResultFormat(req, resp);
		}

		if (resultFormat == null) {
			// This is safe with the current SPARQL Results XML implementation that
			// is able to write out boolean results from the "Tuple" writer.
			resultFormat = TupleQueryResultFormat.SPARQL;
		}

		return QueryResultIO.createWriter(resultFormat, outputStream);
	}

	/**
	 * Gets a {@link TupleResultBuilder} based on the Accept header, and sets the
	 * result content type to the best available match for that, returning a
	 * builder that can be used to write out the results.
	 * 
	 * @param req
	 *        the current HTTP request
	 * @param resp
	 *        the current HTTP response
	 * @param outputStream
	 *        TODO
	 * @return a builder that can be used to write out the results
	 * @throws IOException
	 * @throws UnsupportedQueryResultFormatException
	 */
	protected TupleResultBuilder getTupleResultBuilder(HttpServletRequest req, HttpServletResponse resp,
			OutputStream outputStream)
		throws UnsupportedQueryResultFormatException, IOException
	{
		QueryResultWriter resultWriter = getResultWriter(req, resp, resp.getOutputStream());

		String contentType = resultWriter.getQueryResultFormat().getDefaultMIMEType();

		// HACK: In order to make XSLT stylesheet driven user interface work,
		// browser user agents must receive application/xml if they are going to
		// actually get application/sparql-results+xml
		// NOTE: This will test against both BooleanQueryResultsFormat and
		// TupleQueryResultsFormat
		if (contentType.equals(APPLICATION_SPARQL_RESULTS_XML)) {
			String uaHeader = req.getHeader(USER_AGENT);
			String acceptHeader = req.getHeader(ACCEPT);

			if (acceptHeader != null && acceptHeader.contains(APPLICATION_SPARQL_RESULTS_XML)) {
				// Do nothing, leave the contentType as
				// application/sparql-results+xml
			}
			// Switch back to application/xml for user agents who claim to be
			// Mozilla compatible
			else if (uaHeader != null && uaHeader.contains(MOZILLA)) {
				contentType = APPLICATION_XML;
			}
			// Switch back to application/xml for user agents who accept either
			// application/xml or text/html
			else if (acceptHeader != null
					&& (acceptHeader.contains(APPLICATION_XML) || acceptHeader.contains(TEXT_HTML)))
			{
				contentType = APPLICATION_XML;
			}
		}

		resp.setContentType(contentType);

		// Setup qname support for result writers who declare that they support it
		if (resultWriter.getSupportedSettings().contains(BasicQueryWriterSettings.ADD_SESAME_QNAME)) {
			resultWriter.getWriterConfig().set(BasicQueryWriterSettings.ADD_SESAME_QNAME, true);
		}

		// TODO: Make the following two settings configurable

		// Convert xsd:string back to plain literals where this behaviour is
		// supported
		if (resultWriter.getSupportedSettings().contains(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL)) {
			resultWriter.getWriterConfig().set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true);
		}

		// Convert rdf:langString back to language literals where this behaviour
		// is supported
		if (resultWriter.getSupportedSettings().contains(BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL)) {
			resultWriter.getWriterConfig().set(BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true);
		}

		// Explicitly support the xsd prefix for XMLSchema namespace as it is
		// required by XSLT scripts
		try {
			resultWriter.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		}
		catch (QueryResultHandlerException e) {
			throw new IOException(e);
		}

		return new TupleResultBuilder(resultWriter, ValueFactoryImpl.getInstance());
	}
}
