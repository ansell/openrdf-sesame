/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static org.openrdf.http.protocol.Protocol.BINDINGS_QUERY;
import static org.openrdf.http.protocol.Protocol.BOOLEAN_QUERY;
import static org.openrdf.http.protocol.Protocol.GRAPH_QUERY;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.X_QUERY_TYPE;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import info.aduna.lang.FileFormat;

import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Model;
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
		}
		else if (model instanceof RepositoryResult) {
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
		else if (model instanceof Model) {
			render((Model)model, req, resp);
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
		if (map.isEmpty()) {
			return null;
		}
		return map.values().iterator().next();
	}

	private void render(RepositoryResult<Statement> gqr, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		try {
			try {
				RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
				RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
				setContentType(resp, factory.getRDFFormat());
				if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod())))
					return;

				ServletOutputStream out = resp.getOutputStream();
				try {
					RDFWriter rdfHandler = factory.getWriter(out);
					rdfHandler.startRDF();

					RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(req);
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
				catch (RDFHandlerException e) {
					throw new IOException("Serialization error: " + e.getMessage());
				}
				finally {
					out.close();
				}
			}
			finally {
				gqr.close();
			}
		}
		catch (StoreException e) {
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
	}

	private void render(TupleQueryResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		try {
			try {
				TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
				TupleQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
				setContentType(resp, factory.getTupleQueryResultFormat());
				resp.setHeader(X_QUERY_TYPE, BINDINGS_QUERY);
				if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod())))
					return;

				ServletOutputStream out = resp.getOutputStream();
				try {
					QueryResultUtil.report(model, factory.getWriter(out));
				}
				catch (TupleQueryResultHandlerException e) {
					throw new IOException("Serialization error: " + e.getMessage());
				}
				finally {
					out.close();
				}
			}
			finally {
				model.close();
			}
		}
		catch (StoreException e) {
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
	}

	private void render(GraphQueryResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		try {
			try {
				RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
				RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
				setContentType(resp, factory.getRDFFormat());
				resp.setHeader(X_QUERY_TYPE, GRAPH_QUERY);
				if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod())))
					return;

				ServletOutputStream out = resp.getOutputStream();
				try {
					QueryResultUtil.report(model, factory.getWriter(out));
				}
				catch (RDFHandlerException e) {
					throw new IOException("Serialization error: " + e.getMessage());
				}
				finally {
					out.close();
				}
			}
			finally {
				model.close();
			}
		}
		catch (StoreException e) {
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
	}

	private void render(BooleanQueryResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException
	{
		BooleanQueryResultWriterRegistry registry = BooleanQueryResultWriterRegistry.getInstance();
		BooleanQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
		setContentType(resp, factory.getBooleanQueryResultFormat());
		resp.setHeader(X_QUERY_TYPE, BOOLEAN_QUERY);
		if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod())))
			return;

		ServletOutputStream out = resp.getOutputStream();
		try {
			factory.getWriter(out).write(model.next());
		}
		finally {
			out.close();
		}
	}

	private void render(Model model, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ClientHTTPException
	{
		RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
		RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
		setContentType(resp, factory.getRDFFormat());
		if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod())))
			return;

		ServletOutputStream out = resp.getOutputStream();
		try {
			RDFWriter rdfHandler = factory.getWriter(out);
			rdfHandler.startRDF();

			for (Map.Entry<String, String> ns : model.getNamespaces().entrySet()) {
				rdfHandler.handleNamespace(ns.getKey(), ns.getValue());
			}

			for (Statement st : model) {
				rdfHandler.handleStatement(st);
			}

			rdfHandler.endRDF();
		}
		catch (RDFHandlerException e) {
			throw new IOException("Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}

	private void render(Reader model, HttpServletRequest req, HttpServletResponse resp)
		throws IOException
	{
		try {
			resp.setContentType("text/plain");
			if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod())))
				return;

			ServletOutputStream writer = resp.getOutputStream();
			try {
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
		finally {
			model.close();
		}
	}

	private void setContentType(HttpServletResponse resp, FileFormat format) {
		String contentType = format.getDefaultMIMEType();
		if (format.hasCharset()) {
			Charset charset = format.getCharset();
			contentType += "; charset=" + charset.name();
		}
		resp.setHeader("Content-Type", contentType);
		if (format.getDefaultFileExtension() != null) {
			String contentDisposition = "attachment; filename=result" + "." + format.getDefaultFileExtension();
			resp.setHeader("Content-Disposition", contentDisposition);
		}
	}

	protected void logEndOfRequest(HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			String queryStr = request.getParameter(QUERY_PARAM_NAME);
			int qryCode = String.valueOf(queryStr).hashCode();
			logger.info("Request for query {} is finished", qryCode);
		}
	}
}