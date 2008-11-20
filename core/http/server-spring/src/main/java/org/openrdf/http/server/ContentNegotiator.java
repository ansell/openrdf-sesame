/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import info.aduna.lang.FileFormat;

import org.openrdf.http.server.exceptions.ClientHTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.store.StoreException;

class ContentNegotiator implements RequestToViewNameTranslator, ViewResolver, View {

	static final String BEAN_NAME = DispatcherServlet.REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public String getViewName(HttpServletRequest request)
		throws Exception
	{
		return BEAN_NAME;
	}

	public View resolveViewName(String viewName, Locale locale)
		throws Exception
	{
		return this;
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map map, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		Object model = getModel(map);
		if (model == null) {
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else if (model instanceof RepositoryResult) {
			render((RepositoryResult)model, req, resp);
		}
		else if (model instanceof TupleQueryResult) {
			render((TupleQueryResult)model, req, resp);
		}
		else if (model instanceof GraphQueryResult) {
			render((GraphQueryResult)model, req, resp);
		}
		else if (model instanceof BooleanQueryResult) {
			render((BooleanQueryResult)model, req, resp);
		}
		else if (model instanceof Reader) {
			render((Reader)model, req, resp);
		}
		else {
			throw new AssertionError(model.getClass());
		}
		logEndOfRequest(req);
	}

	@SuppressWarnings("unchecked")
	private Object getModel(Map map) {
		if (map.isEmpty())
			return null;
		return map.values().iterator().next();
	}

	private void render(RepositoryResult<Statement> gqr, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
		RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
		setContentType(resp, factory.getRDFFormat());
		ServletOutputStream out = resp.getOutputStream();
		try {
			RDFWriter rdfHandler = factory.getWriter(out);
			try {
				rdfHandler.startRDF();

				RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(req);
				for (Namespace ns : repositoryCon.getNamespaces().asList()) {
					String prefix = ns.getPrefix();
					String namespace = ns.getName();
					rdfHandler.handleNamespace(prefix, namespace);
				}

				while (gqr.hasNext()) {
					Statement st = gqr.next();
					rdfHandler.handleStatement(st);
				}

				rdfHandler.endRDF();
			}
			finally {
				gqr.close();
			}
		}
		catch (StoreException e) {
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			throw new IOException("Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}

	private void render(TupleQueryResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
		TupleQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
		setContentType(resp, factory.getTupleQueryResultFormat());
		ServletOutputStream out = resp.getOutputStream();
		try {
			QueryResultUtil.report(model, factory.getWriter(out));
		}
		catch (StoreException e) {
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			throw new IOException("Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}

	private void render(GraphQueryResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
		RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
		setContentType(resp, factory.getRDFFormat());
		ServletOutputStream out = resp.getOutputStream();
		try {
			QueryResultUtil.report(model, factory.getWriter(out));
		}
		catch (StoreException e) {
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			throw new IOException("Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}

	private void render(BooleanQueryResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		BooleanQueryResultWriterRegistry registry = BooleanQueryResultWriterRegistry.getInstance();
		BooleanQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
		setContentType(resp, factory.getBooleanQueryResultFormat());
		ServletOutputStream out = resp.getOutputStream();
		try {
			factory.getWriter(out).write(model.getResult());
		}
		finally {
			out.close();
		}
	}

	private void render(Reader model, HttpServletRequest req, HttpServletResponse resp)
		throws IOException
	{
		ServletOutputStream writer = resp.getOutputStream();
		try {
			resp.setContentType("text/plain");
			int read;
			char[] buf = new char[1024];
			while ((read = model.read(buf)) >= 0) {
				writer.print(new String(buf, 0, read));
			}
		}
		finally {
			writer.close();
		}
	}

	private void setContentType(HttpServletResponse resp, FileFormat format) {
		String contentType = format.getDefaultMIMEType();
		if (format.hasCharset()) {
			Charset charset = format.getCharset();
			contentType += "; charset=" + charset.name();
		}
		String contentDisposition = "attachment; filename=result";
		if (format.getDefaultFileExtension() != null) {
			contentDisposition += "." + format.getDefaultFileExtension();
		}
		resp.setHeader("Content-Type", contentType);
		resp.setHeader("Content-Disposition", contentDisposition);
	}

	protected void logEndOfRequest(HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			String queryStr = request.getParameter(QUERY_PARAM_NAME);
			int qryCode = String.valueOf(queryStr).hashCode();
			logger.info("Request for query {} is finished", qryCode);
		}
	}
}