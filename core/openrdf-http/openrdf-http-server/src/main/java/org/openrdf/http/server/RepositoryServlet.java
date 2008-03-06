/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.ACCEPT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import info.aduna.collections.iterators.ConvertingIterator;
import info.aduna.io.IOUtil;
import info.aduna.iteration.CloseableIteration;
import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.transaction.TransactionReader;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryManager;
import org.openrdf.repository.config.RepositoryManagerConfigException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Servlet that exports information about available repositories.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryServlet extends GenericServlet {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -4058073101576755194L;

	/*---------*
	 * Methods *
	 *---------*/

	// Implements GenericServlet.service(...)
	public void service(ServletRequest request, ServletResponse response)
		throws IOException
	{
		String origThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(Protocol.REPOSITORIES);

		try {
			if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
				handleHTTPRequest((HttpServletRequest)request, (HttpServletResponse)response);
			}
		}
		finally {
			Thread.currentThread().setName(origThreadName);
		}
	}

	protected void handleHTTPRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		try {
			String pathInfoStr = request.getPathInfo();
			List<String> pathInfo = splitFilePath(pathInfoStr);

			if (pathInfo.size() == 0) {
				// No repository ID was specified
				handleRepositoryListRequest(request, response);
			}
			else {
				// First component of the path is the repository ID
				String repositoryID = pathInfo.get(0);

				String origThreadName = Thread.currentThread().getName();
				Thread.currentThread().setName(origThreadName + "/" + repositoryID);

				try {
					Repository repository = RepositoryManager.getDefaultInstance().getRepository(repositoryID);

					if (repository == null) {
						throw new ClientRequestException(SC_NOT_FOUND, "Unknown repository: " + repositoryID);
					}

					if (pathInfo.size() == 1) {
						handleRepositoryRequest(repository, request, response);
					}
					else {
						String subPath = pathInfo.get(1);

						if (subPath.equals(Protocol.STATEMENTS) && pathInfo.size() == 2) {
							handleStatementsRequest(repository, request, response);
						}
						else if (subPath.equals(Protocol.CONTEXTS) && pathInfo.size() == 2) {
							handleContextListRequest(repository, request, response);
						}
						else if (subPath.equals(Protocol.SIZE) && pathInfo.size() == 2) {
							handleSizeRequest(repository, request, response);
						}
						else if (subPath.equals(Protocol.NAMESPACES)) {
							List<String> params = pathInfo.subList(2, pathInfo.size());
							handleNamespaceRequest(repository, params, request, response);
						}
						else {
							throw new ClientRequestException(SC_NOT_FOUND, "Invalid path: " + request.getPathInfo());
						}
					}
				}
				finally {
					Thread.currentThread().setName(origThreadName);
				}
			}
		}
		catch (ClientRequestException e) {
			if (e.getCause() == null) {
				logger.info(e.getMessage());
			}
			else {
				logger.info(e.getMessage(), e.getCause());
			}

			response.sendError(e.getStatusCode(), e.getMessage());
		}
		catch (RepositoryManagerConfigException e) {
			logger.error("Repository configuration error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Repository configuration error: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Failed to handle repository request", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private void handleRepositoryListRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		if ("GET".equals(request.getMethod())) {
			logger.info("GET repository list");
			exportRepositoryList(request, response);
		}
		else {
			response.setHeader("Allow", "GET");
			throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + request.getMethod());
		}
	}

	private void exportRepositoryList(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		// Export the repository list as a set of 5-tuples ["uri", "id", "title",
		// "readable", "writable"]
		TupleQueryResultWriterFactory qrWriterFactory = getAcceptableQueryResultWriterFactory(request, response);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		try {
			OutputStream out = response.getOutputStream();
			TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(qrFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=repositories."
					+ qrFormat.getFileExtension());

			// Determine the repository's URI
			StringBuffer requestURL = request.getRequestURL();
			if (requestURL.charAt(requestURL.length() - 1) != '/') {
				requestURL.append('/');
			}
			String namespace = requestURL.toString();

			List<String> columnNames = Arrays.asList("uri", "id", "title", "readable", "writable");
			qrWriter.startQueryResult(columnNames, true, false);

			ValueFactory vf = new ValueFactoryImpl();

			for (RepositoryConfig repConfig : RepositoryManager.getDefaultInstance().getConfig().getRepositoryConfigs())
			{
				URI uri = vf.createURI(namespace, repConfig.getID());
				Literal id = vf.createLiteral(repConfig.getID());
				Literal title = vf.createLiteral(repConfig.getTitle());
				Literal readable = vf.createLiteral(repConfig.isWorldReadable());
				Literal writable = vf.createLiteral(repConfig.isWorldWritable());

				BindingSet bindingSet = new ListBindingSet(columnNames, uri, id, title, readable, writable);
				qrWriter.handleSolution(bindingSet);
			}

			qrWriter.endQueryResult();
			out.close();
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
	}

	private void handleRepositoryRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		String reqMethod = request.getMethod();

		if ("GET".equals(reqMethod)) {
			logger.info("GET query result");
		}
		else if ("POST".equals(reqMethod)) {
			String contentType = request.getContentType();

			if (Protocol.FORM_MIME_TYPE.equals(contentType)) {
				logger.info("POST query");
			}
			else {
				throw new ClientRequestException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported content type: "
						+ contentType);
			}
		}
		else {
			response.setHeader("Allow", "GET, POST");
			throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + reqMethod);
		}

		logRequestParameters(request);

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		if (queryStr != null) {
			evaluateQuery(repository, queryStr, request, response);
		}
		else {
			throw new ClientRequestException(SC_BAD_REQUEST, "Missing parameter: " + QUERY_PARAM_NAME);
		}
	}

	private void evaluateQuery(Repository repository, String queryStr, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		logRequestParameters(request);

		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr.toUpperCase());

			if (queryLn == null) {
				throw new ClientRequestException(SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
			}
		}

		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		try {
			RepositoryConnection con = repository.getConnection();

			try {
				Query query = con.prepareQuery(queryLn, queryStr);
				query.setIncludeInferred(includeInferred);

				// determine if any variable bindings have been set on this query.
				Enumeration parameterNames = request.getParameterNames();

				while (parameterNames.hasMoreElements()) {
					String parameterName = (String)parameterNames.nextElement();

					if (parameterName.startsWith(BINDING_PREFIX)
							&& parameterName.length() > BINDING_PREFIX.length())
					{
						String bindingName = parameterName.substring(BINDING_PREFIX.length());
						Value bindingValue = parseValueParam(request, parameterName, repository.getValueFactory());
						query.addBinding(bindingName, bindingValue);
					}
				}

				if (query instanceof TupleQuery) {
					TupleQuery tupleQuery = (TupleQuery)query;

					TupleQueryResultWriterFactory qrWriterFactory = getAcceptableQueryResultWriterFactory(request,
							response);
					TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

					OutputStream out = response.getOutputStream();
					TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);

					response.setStatus(SC_OK);
					response.setContentType(qrFormat.getMIMEType());
					response.setHeader("Content-Disposition", "attachment; filename=queryresult."
							+ qrFormat.getFileExtension());

					try {
						tupleQuery.evaluate(qrWriter);
					}
					finally {
						out.close();
					}
				}
				else if (query instanceof GraphQuery) {
					GraphQuery graphQuery = (GraphQuery)query;

					RDFWriterFactory rdfWriterFactory = getAcceptableRDFWriterFactory(request, response);
					RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

					OutputStream out = response.getOutputStream();
					RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);

					response.setStatus(SC_OK);
					response.setContentType(rdfFormat.getMIMEType());
					response.setHeader("Content-Disposition", "attachment; filename=queryresult."
							+ rdfFormat.getFileExtension());

					try {
						graphQuery.evaluate(rdfWriter);
					}
					finally {
						out.close();
					}
				}
				else {
					logger.error("Unknown query type: {}", queryStr);
					response.sendError(SC_INTERNAL_SERVER_ERROR, "Unknown query type");
				}
			}
			finally {
				con.close();
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query language: " + queryLn);
		}
		catch (MalformedQueryException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Malformed query: " + e.getMessage());
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_NOT_ACCEPTABLE, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientRequestException(SC_NOT_ACCEPTABLE, "Unsupported RDF format: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		catch (QueryEvaluationException e) {
			logger.error("Query evaluation error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Query evaluation error: " + e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void handleStatementsRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		String reqMethod = request.getMethod();

		if ("GET".equals(reqMethod)) {
			logger.info("GET statements");
			exportStatements(repository, request, response);
		}
		else if ("POST".equals(reqMethod)) {
			String contentType = request.getContentType();

			if (Protocol.TXN_MIME_TYPE.equals(contentType)) {
				logger.info("POST transaction to repository");
				// An RDF transaction document is being posted to the server
				handleTransaction(repository, request, response);
			}
			else {
				logger.info("POST data to repository");
				// An RDF document is being posted to the server
				addData(repository, false, request, response);
			}
		}
		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT data in repository");
			addData(repository, true, request, response);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE data from repository");
			deleteData(repository, request, response);
		}
		else {
			response.setHeader("Allow", "GET, POST, PUT, DELETE");
			throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + reqMethod);
		}
	}

	private void exportStatements(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		boolean useInferencing = parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		RDFWriterFactory rdfWriterFactory = getAcceptableRDFWriterFactory(request, response);
		RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

		try {
			OutputStream out = response.getOutputStream();
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(rdfFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=statements."
					+ rdfFormat.getFileExtension());

			RepositoryConnection con = repository.getConnection();
			try {
				con.exportStatements(subj, pred, obj, useInferencing, rdfWriter, contexts);
			}
			finally {
				con.close();
			}

			out.close();
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientRequestException(SC_NOT_ACCEPTABLE, "No RDF writer available for format: "
					+ rdfFormat.getName());
		}
		catch (RDFHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to export statements: " + e.getMessage());
		}
	}

	private void handleTransaction(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		InputStream in = request.getInputStream();
		try {
			logger.info("Starting transaction...");
			TransactionReader reader = new TransactionReader();
			Iterable<? extends TransactionOperation> txn = reader.parse(in);

			// FIXME: remove cast to SailRepository
			SailConnection con = ((SailRepository)repository).getSail().getConnection();

			try {
				for (TransactionOperation op : txn) {
					logger.debug("Executing operation: {}", op.getClass().getName());
					op.execute(con);
				}

				con.commit();
			}
			finally {
				con.close();
			}
			logger.info("Transaction finished.");

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (SAXParseException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Malformed transaction data: " + e.getMessage());
		}
		catch (SAXException e) {
			logger.error("Unable to parse transaction data", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to parse transaction data: " + e.getMessage());
		}
		catch (IOException e) {
			logger.info("Failed to read data", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to read data: " + e.getMessage());
		}
		catch (SailException e) {
			logger.info("Sail update error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to update data: " + e.getMessage());
		}
	}

	private void addData(Repository repository, boolean replaceCurrent, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		logRequestParameters(request);

		String mimeType = request.getContentType();
		if (mimeType.indexOf(";") > 0) {
			// A character encoding part is specified in the content-type field.
			// Get the mime-type substring to enable correct determination of the
			// RDF format.
			mimeType = mimeType.substring(0, mimeType.indexOf(";"));
		}

		RDFFormat rdfFormat = RDFFormat.forMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new ClientRequestException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported content type: "
					+ request.getContentType());
		}

		ValueFactory vf = repository.getValueFactory();

		Resource[] contexts = parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		URI baseURI = parseURIParam(request, BASEURI_PARAM_NAME, vf);

		if (baseURI == null) {
			baseURI = vf.createURI("foo:bar");
			logger.info("no base URI specified, using dummy '{}'", baseURI);
		}

		InputStream in = request.getInputStream();
		try {
			RepositoryConnection con = repository.getConnection();

			try {
				con.setAutoCommit(false);

				if (replaceCurrent) {
					con.clear(contexts);
				}

				con.add(in, baseURI.toString(), rdfFormat, contexts);
				con.commit();
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (IOException e) {
			logger.info("Failed to read data", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to read data");
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientRequestException(SC_UNSUPPORTED_MEDIA_TYPE, "No RDF parser available for format "
					+ rdfFormat.getName());
		}
		catch (RDFParseException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Parse error at line " + e.getLineNumber() + ": "
					+ e.getMessage());
		}
		catch (RepositoryException e) {
			logger.info("Repository update error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to update data: " + e.getMessage());
		}
	}

	private void deleteData(Repository repository, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = parseContextParam(request, CONTEXT_PARAM_NAME, vf);

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.remove(subj, pred, obj, contexts);
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (RepositoryException e) {
			logger.error("Repository update error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to delete data: " + e.getMessage());
		}
	}

	private void handleContextListRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		if ("GET".equals(request.getMethod())) {
			logger.info("GET contexts for repository");
			exportContextList(repository, request, response);
		}
		else {
			response.setHeader("Allow", "GET");
			throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + request.getMethod());
		}
	}

	private void exportContextList(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		// Export the list of context identifiers as a set of 1-tuples
		TupleQueryResultWriterFactory qrWriterFactory = getAcceptableQueryResultWriterFactory(request, response);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		try {
			OutputStream out = response.getOutputStream();
			TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(qrFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=repository-contexts."
					+ qrFormat.getFileExtension());

			List<String> columnNames = Arrays.asList("contextID");
			qrWriter.startQueryResult(columnNames, true, false);

			RepositoryConnection con = repository.getConnection();
			try {
				CloseableIteration<? extends Resource, RepositoryException> contextIter = con.getContextIDs();

				try {
					while (contextIter.hasNext()) {
						BindingSet bindingSet = new ListBindingSet(columnNames, contextIter.next());
						qrWriter.handleSolution(bindingSet);
					}
				}
				finally {
					contextIter.close();
				}
			}
			finally {
				con.close();
			}

			qrWriter.endQueryResult();
			out.close();
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to export context list: " + e.getMessage());
		}
	}

	private void handleSizeRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		if ("GET".equals(request.getMethod())) {
			logger.info("GET size of repository");
			exportSize(repository, request, response);
		}
		else {
			response.setHeader("Allow", "GET");
			throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + request.getMethod());
		}
	}

	private void exportSize(Repository repository, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();
		Resource[] contexts = parseContextParam(request, CONTEXT_PARAM_NAME, vf);

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				long size = con.size(contexts);

				response.setStatus(SC_OK);
				response.setContentType("text/plain");

				PrintWriter writer = response.getWriter();
				writer.print(size);
				writer.close();
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to export repository size: " + e.getMessage());
		}
	}

	private void handleNamespaceRequest(Repository repository, List<String> params,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		String reqMethod = request.getMethod();

		if (params.size() == 0) {
			if ("GET".equals(reqMethod)) {
				logger.info("GET namespace list");
				exportNamespaceList(repository, request, response);
			}
			else if ("DELETE".equals(reqMethod)) {
				logger.info("DELETE namespaces");
				clearNamespaces(repository, response);
			}
			else {
				response.setHeader("Allow", "GET, DELETE");
				throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + reqMethod);
			}
		}
		else if (params.size() == 1) {
			String prefix = params.get(0);
			logger.info("GET namespace for prefix {}" + prefix);

			if ("GET".equals(reqMethod)) {
				exportNamespace(repository, prefix, response);
			}
			else if ("PUT".equals(reqMethod)) {
				logger.info("PUT prefix {}", prefix);
				setNamespace(repository, prefix, request, response);
			}
			else if ("DELETE".equals(reqMethod)) {
				logger.info("DELETE prefix {}", prefix);
				removeNamespace(repository, prefix, response);
			}
			else {
				response.setHeader("Allow", "GET, PUT, DELETE");
				throw new ClientRequestException(SC_METHOD_NOT_ALLOWED, "Method not allowed: " + reqMethod);
			}
		}
		else {
			throw new ClientRequestException(SC_NOT_FOUND, "Invalid path: " + request.getPathInfo());
		}
	}

	private void exportNamespaceList(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		// Export the list of namespaces and their prefixes as a set of
		// 2-tuples ["prefix", "name"]
		TupleQueryResultWriterFactory qrWriterFactory = getAcceptableQueryResultWriterFactory(request, response);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		try {
			OutputStream out = response.getOutputStream();
			TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(qrFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=repository-namespaces."
					+ qrFormat.getFileExtension());

			List<String> columnNames = Arrays.asList("prefix", "namespace");
			qrWriter.startQueryResult(columnNames, true, false);

			RepositoryConnection con = repository.getConnection();
			try {
				CloseableIteration<? extends Namespace, RepositoryException> iter = con.getNamespaces();

				try {
					while (iter.hasNext()) {
						Namespace ns = iter.next();

						Literal prefix = new LiteralImpl(ns.getPrefix());
						Literal namespace = new LiteralImpl(ns.getName());

						BindingSet bindingSet = new ListBindingSet(columnNames, prefix, namespace);
						qrWriter.handleSolution(bindingSet);
					}
				}
				finally {
					iter.close();
				}
			}
			finally {
				con.close();
			}

			qrWriter.endQueryResult();
			out.close();
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to export namespace list: " + e.getMessage());
		}
	}

	// Exports the namespace for the specified prefix
	private void exportNamespace(Repository repository, String prefix, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		try {
			RepositoryConnection con = repository.getConnection();

			try {
				String namespace = con.getNamespace(prefix);

				if (namespace != null) {
					response.setStatus(SC_OK);
					// FIXME: Specify character encoding as text/plain default to
					// US-ASCII?
					response.setContentType("text/plain");
					PrintWriter writer = response.getWriter();
					writer.print(namespace);
					writer.close();
				}
				else {
					throw new ClientRequestException(SC_NOT_FOUND, "Undefined prefix: " + prefix);
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to export namespace: " + e.getMessage());
		}
	}

	private void clearNamespaces(Repository repository, HttpServletResponse response)
		throws IOException
	{
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				// TODO: use clearNamespaces() when available
				CloseableIteration<? extends Namespace, RepositoryException> nsIter = con.getNamespaces();
				while (nsIter.hasNext()) {
					con.removeNamespace(nsIter.next().getPrefix());
				}
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(SC_NO_CONTENT);
			response.getWriter().close();
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to remove namespaces: " + e.getMessage());
		}
	}

	private void setNamespace(Repository repository, String prefix, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		String namespace = IOUtil.readString(request.getReader());

		namespace = namespace.trim();

		if (namespace.length() == 0) {
			throw new ClientRequestException(SC_BAD_REQUEST, "No namespace name found in request body");
		}
		// FIXME: perform some sanity checks on the namespace string

		logger.info("namespace={}", namespace);

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.setNamespace(prefix, namespace);

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(SC_NO_CONTENT);
				response.getWriter().close();
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to set namespace prefix: " + e.getMessage());
		}
	}

	private void removeNamespace(Repository repository, String prefix, HttpServletResponse response)
		throws IOException
	{
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.removeNamespace(prefix);

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(SC_NO_CONTENT);
				response.getWriter().close();
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to remove namespace prefix: " + e.getMessage());
		}
	}

	/**
	 * Splits a file path into separate components using slashes as component
	 * separators.
	 * 
	 * @param filePath
	 *        A file path, e.g. "foo/bar/baz".
	 * @return A list of Strings containing the components of the parsed path in
	 *         order of appearance.
	 */
	private List<String> splitFilePath(String filePath) {
		List<String> result = new ArrayList<String>(4);

		if (filePath != null) {
			StringTokenizer st = new StringTokenizer(filePath, "/");
			while (st.hasMoreTokens()) {
				result.add(st.nextToken());
			}
		}

		return result;
	}

	/**
	 * Determines an appropriate RDF format based on the 'Accept' parameter or
	 * -header specified in the supplied request and returns a RDF writer factory
	 * for this format. The parameter value, if present, overrules the header
	 * value. If the header value is used, a "Vary: Accept" header will be added
	 * to the response as required by RFC 2616.
	 * 
	 * @param request
	 *        The request.
	 * @param response
	 *        The response.
	 * @return An RDFWriterFactory for an RDF file format that can be handled by
	 *         the client.
	 * @throws ClientRequestException
	 *         If no acceptable format could be determined a "NOT ACCEPTABLE"
	 *         exception is thrown.
	 */
	private RDFWriterFactory getAcceptableRDFWriterFactory(HttpServletRequest request,
			HttpServletResponse response)
		throws ClientRequestException
	{
		// Accept-parameter takes precedence over request headers
		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			logAcceptableFormats(request);

			Iterator<String> mimeTypeIter = new ConvertingIterator<RDFWriterFactory, String>(
					Rio.getRDFWriterRegistry().getAll().iterator())
			{

				@Override
				protected String convert(RDFWriterFactory factory)
				{
					return factory.getRDFFormat().getMIMEType();
				}
			};

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypeIter, request);

			response.setHeader("Vary", Protocol.ACCEPT_PARAM_NAME);
		}

		if (mimeType != null) {
			for (RDFWriterFactory factory : Rio.getRDFWriterRegistry().getAll()) {
				if (factory.getRDFFormat().hasMIMEType(mimeType)) {
					return factory;
				}
			}
		}

		// No acceptable format was found, send 406 as required by RFC 2616
		throw new ClientRequestException(SC_NOT_ACCEPTABLE, "No acceptable RDF format found.");
	}

	/**
	 * Determines an appropriate tuple query result format based on the 'Accept'
	 * parameter or -header specified in the supplied request. The parameter
	 * value, if present, takes presedence over the header value. If the header
	 * value is used, a "Vary: Accept" header will be added to the response as
	 * required by RFC 2616.
	 * 
	 * @param request
	 *        The request.
	 * @param response
	 *        The response.
	 * @return A TupleQueryResultFormat object representing an query result
	 *         format that can be handled by the client, or <tt>null</tt> if no
	 *         such format could be determined.
	 */
	private TupleQueryResultWriterFactory getAcceptableQueryResultWriterFactory(HttpServletRequest request,
			HttpServletResponse response)
		throws ClientRequestException
	{
		// Accept-parameter takes precedence over request headers
		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			logAcceptableFormats(request);

			Iterator<String> mimeTypeIter = new ConvertingIterator<TupleQueryResultWriterFactory, String>(
					QueryResultUtil.getTupleQueryResultWriterRegistry().getAll().iterator())
			{

				@Override
				protected String convert(TupleQueryResultWriterFactory factory)
				{
					return factory.getTupleQueryResultFormat().getMIMEType();
				}
			};

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypeIter, request);

			response.setHeader("Vary", Protocol.ACCEPT_PARAM_NAME);
		}

		if (mimeType != null) {
			for (TupleQueryResultWriterFactory factory : QueryResultUtil.getTupleQueryResultWriterRegistry().getAll())
			{
				if (factory.getTupleQueryResultFormat().hasMIMEType(mimeType)) {
					return factory;
				}
			}
		}

		// No acceptable format was found, send 406 as required by RFC 2616
		throw new ClientRequestException(SC_NOT_ACCEPTABLE, "No acceptable query result format found");
	}

	private Value parseValueParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeValue(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	private Resource parseResourceParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeResource(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	private URI parseURIParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeURI(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	private Resource[] parseContextParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String[] paramValues = request.getParameterValues(paramName);
		try {
			return Protocol.decodeContexts(paramValues, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ e.getMessage());
		}
	}

	private boolean parseBooleanParam(HttpServletRequest request, String paramName, boolean defaultValue) {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			return defaultValue;
		}
		else {
			return Boolean.parseBoolean(paramName);
		}
	}

	/**
	 * Logs all request parameters of the supplied request.
	 */
	private void logRequestParameters(HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			@SuppressWarnings("unchecked")
			Enumeration<String> paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String name = paramNames.nextElement();
				for (String value : request.getParameterValues(name)) {
					logger.info("{}=\"{}\"", name, value);
				}
			}
		}
	}

	private void logAcceptableFormats(HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			StringBuilder acceptable = new StringBuilder(64);

			@SuppressWarnings("unchecked")
			Enumeration<String> acceptHeaders = request.getHeaders(ACCEPT_PARAM_NAME);

			while (acceptHeaders.hasMoreElements()) {
				acceptable.append(acceptHeaders.nextElement());

				if (acceptHeaders.hasMoreElements()) {
					acceptable.append(',');
				}
			}

			logger.debug("Acceptable formats: " + acceptable);
		}
	}
}