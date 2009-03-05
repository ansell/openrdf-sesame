/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

import info.aduna.io.IOUtil;
import info.aduna.io.file.FileFormat;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.result.BooleanResult;
import org.openrdf.result.ContextResult;
import org.openrdf.result.GraphResult;
import org.openrdf.result.ModelResult;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.store.StoreException;

class ContentNegotiator implements RequestToViewNameTranslator, ViewResolver, View {

	/*-------------------------------------------*
	 * RequestToViewNameTranslator functionality *
	 *-------------------------------------------*/

	static final String BEAN_NAME = DispatcherServlet.REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME;

	public String getViewName(HttpServletRequest request)
		throws Exception
	{
		// All requests without an explicit view name are are processed by this
		// class
		return BEAN_NAME;
	}

	/*----------------------------*
	 * ViewResolver functionality *
	 *----------------------------*/

	public View resolveViewName(String viewName, Locale locale)
		throws Exception
	{
		// This class represents all relevant views
		return this;
	}

	/*--------------------*
	 * View functionality *
	 *--------------------*/

	private static final int SMALL = 10;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String getContentType() {
		// don't know the content type upfront
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map map, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		Object model = null;
		if (!map.isEmpty()) {
			model = map.values().iterator().next();
		}

		if (model == null) {
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		else if (model instanceof TupleResult) {
			render((TupleResult)model, req, resp);
		}
		else if (model instanceof ContextResult) {
			render((ContextResult)model, req, resp);
		}
		else if (model instanceof NamespaceResult) {
			render((NamespaceResult)model, req, resp);
		}
		else if (model instanceof GraphResult) {
			// subtype of ModelResult, must be checked before ModelResult
			render((GraphResult)model, req, resp);
		}
		else if (model instanceof ModelResult) {
			render((ModelResult)model, req, resp);
		}
		else if (model instanceof Model) {
			render((Model)model, req, resp);
		}
		else if (model instanceof BooleanResult) {
			render((BooleanResult)model, req, resp);
		}
		else if (model instanceof Reader) {
			render((Reader)model, req, resp);
		}
		else {
			throw new AssertionError(model.getClass());
		}

		logEndOfRequest(req);
	}

	private void render(TupleResult result, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		try {
			TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
			TupleQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);

			setContentType(resp, factory.getTupleQueryResultFormat());

			resp.setHeader(X_QUERY_TYPE, BINDINGS_QUERY);

			if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod()))) {
				return;
			}

			ServletOutputStream out = resp.getOutputStream();
			try {
				TupleQueryResultHandler handler = factory.getWriter(out);
				QueryResultUtil.report(result, handler);
			}
			catch (TupleQueryResultHandlerException e) {
				throw new IOException("Serialization error: " + e.getMessage());
			}
			finally {
				out.close();
			}
		}
		finally {
			result.close();
		}
	}

	private void render(ContextResult result, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		List<String> columnNames = Arrays.asList("contextID");
		List<BindingSet> contexts = new ArrayList<BindingSet>();

		try {
			while (result.hasNext()) {
				BindingSet bindingSet = new ListBindingSet(columnNames, result.next());
				contexts.add(bindingSet);
			}
		}
		finally {
			result.close();
		}

		render(new TupleResultImpl(columnNames, contexts), req, resp);
	}

	private void render(NamespaceResult result, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		List<String> columnNames = Arrays.asList(Protocol.PREFIX, Protocol.NAMESPACE);
		List<BindingSet> namespaces = new ArrayList<BindingSet>();

		try {
			while (result.hasNext()) {
				Namespace ns = result.next();

				Literal prefix = new LiteralImpl(ns.getPrefix());
				Literal namespace = new LiteralImpl(ns.getName());

				BindingSet bindingSet = new ListBindingSet(columnNames, prefix, namespace);
				namespaces.add(bindingSet);
			}
		}
		finally {
			result.close();
		}

		render(new TupleResultImpl(columnNames, namespaces), req, resp);
	}

	private void render(GraphResult result, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		resp.setHeader(X_QUERY_TYPE, GRAPH_QUERY);
		render(result, false, req, resp);
	}

	private void render(ModelResult result, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		render(result, true, req, resp);
	}

	/**
	 * Exports the model to an RDF format, optionally trimming the namespace
	 * declarations for small results.
	 */
	private void render(ModelResult result, boolean trimNamespaces, HttpServletRequest req,
			HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		try {
			RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
			RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);
			RDFFormat rdfFormat = factory.getRDFFormat();

			setContentType(resp, rdfFormat);

			if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod()))) {
				return;
			}

			ServletOutputStream out = resp.getOutputStream();
			try {
				RDFWriter writer = factory.getWriter(out);
				writer.setBaseURI(req.getRequestURL().toString());
				writer.startRDF();

				Set<String> firstNamespaces = null;
				List<Statement> firstStatements = new ArrayList<Statement>(SMALL);

				// Only try to trim namespace if the RDF format supports namespaces
				// in the first place
				trimNamespaces &= rdfFormat.supportsNamespaces();

				if (trimNamespaces) {
					// Gather the first few statements
					for (int i = 0; result.hasNext() && i < SMALL; i++) {
						firstStatements.add(result.next());
					}

					// Only trim namespaces if the set is small enough
					trimNamespaces = firstStatements.size() < SMALL;

					if (trimNamespaces) {
						// Gather the namespaces from the first few statements
						firstNamespaces = new HashSet<String>(SMALL);

						for (Statement st : firstStatements) {
							addNamespace(st.getSubject(), firstNamespaces);
							addNamespace(st.getPredicate(), firstNamespaces);
							addNamespace(st.getObject(), firstNamespaces);
							addNamespace(st.getContext(), firstNamespaces);
						}
					}
				}

				// Report namespace prefixes
				for (Map.Entry<String, String> ns : result.getNamespaces().entrySet()) {
					String prefix = ns.getKey();
					String namespace = ns.getValue();
					if (trimNamespaces == false || firstNamespaces.contains(namespace)) {
						writer.handleNamespace(prefix, namespace);
					}
				}

				// Report staements
				for (Statement st : firstStatements) {
					writer.handleStatement(st);
				}

				while (result.hasNext()) {
					Statement st = result.next();
					writer.handleStatement(st);
				}

				writer.endRDF();
			}
			catch (RDFHandlerException e) {
				throw new IOException("Serialization error: " + e.getMessage());
			}
			finally {
				out.close();
			}
		}
		finally {
			result.close();
		}
	}

	private void addNamespace(Value value, Set<String> namespaces) {
		if (value instanceof URI) {
			URI uri = (URI)value;
			namespaces.add(uri.getNamespace());
		}
	}

	private void render(Model model, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ClientHTTPException
	{
		RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
		RDFWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);

		setContentType(resp, factory.getRDFFormat());

		if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod()))) {
			return;
		}

		ServletOutputStream out = resp.getOutputStream();
		try {
			RDFWriter rdfHandler = factory.getWriter(out);
			rdfHandler.setBaseURI(req.getRequestURL().toString());
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

	private void render(BooleanResult model, HttpServletRequest req, HttpServletResponse resp)
		throws ClientHTTPException, IOException, StoreException
	{
		BooleanQueryResultWriterRegistry registry = BooleanQueryResultWriterRegistry.getInstance();
		BooleanQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(req, resp, registry);

		setContentType(resp, factory.getBooleanQueryResultFormat());
		resp.setHeader(X_QUERY_TYPE, BOOLEAN_QUERY);

		if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod()))) {
			return;
		}

		ServletOutputStream out = resp.getOutputStream();
		try {
			factory.getWriter(out).write(model.next());
		}
		finally {
			out.close();
		}
	}

	private void render(Reader reader, HttpServletRequest req, HttpServletResponse resp)
		throws IOException
	{
		try {
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF-8");

			if (RequestMethod.HEAD.equals(RequestMethod.valueOf(req.getMethod()))) {
				return;
			}

			Writer writer = resp.getWriter();
			try {
				IOUtil.transfer(reader, writer);
			}
			finally {
				writer.close();
			}
		}
		finally {
			reader.close();
		}
	}

	private void logEndOfRequest(HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			String queryStr = request.getParameter(QUERY_PARAM_NAME);
			if (queryStr != null) {
				int qryCode = String.valueOf(queryStr).hashCode();
				logger.info("Request for query {} is finished", qryCode);
			}
		}
	}

	private void setContentType(HttpServletResponse resp, FileFormat format) {
		resp.setContentType(format.getDefaultMIMEType());

		if (format.hasCharset()) {
			resp.setCharacterEncoding(format.getCharset().name());
		}

		if (format.getDefaultFileExtension() != null) {
			resp.setHeader("Content-Disposition", "inline; filename=result." + format.getDefaultFileExtension());
		}
	}
}