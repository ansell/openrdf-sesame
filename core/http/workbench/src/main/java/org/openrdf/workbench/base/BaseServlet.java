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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;
import info.aduna.io.MavenUtil;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class BaseServlet implements Servlet {

	protected static final String SERVER_USER = "server-user";

	protected static final String SERVER_PASSWORD = "server-password";

	protected static final String ACCEPT = "Accept";

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

	protected QueryResultWriter getResultWriter(final HttpServletRequest req, final ServletResponse resp)
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

		return QueryResultIO.createWriter(resultFormat, resp.getOutputStream());
	}

	/**
	 * Gets a {@link TupleResultBuilder} based on the Accept header, and sets the
	 * result content type to the best available match for that, returning a
	 * builder that can be used to write out the results.
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws UnsupportedQueryResultFormatException
	 */
	protected TupleResultBuilder getTupleResultBuilder(WorkbenchRequest req, HttpServletResponse resp)
		throws UnsupportedQueryResultFormatException, IOException
	{
		QueryResultWriter resultWriter = getResultWriter(req, resp);
		resp.setContentType(resultWriter.getQueryResultFormat().getDefaultMIMEType());
		return new TupleResultBuilder(resultWriter, ValueFactoryImpl.getInstance());
	}
}
