/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static org.openrdf.http.protocol.Protocol.ACCEPT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
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
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Servlet that exports information about available repositories.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryServlet extends GenericServlet {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

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
		String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("repositories");

		try {
			if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
				_service((HttpServletRequest)request, (HttpServletResponse)response);
			}
		}
		finally {
			Thread.currentThread().setName(oldName);
		}
	}

	protected void _service(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		try {
			String pathInfoStr = request.getPathInfo();

			List<String> pathInfo = _splitFilePath(pathInfoStr);

			if (pathInfo.size() == 0) {
				// No repository ID was specified
				_handleRepositoryListRequest(request, response);
			}
			else {
				// First component of the path is the repository ID
				String repositoryID = pathInfo.get(0);

				String oldName = Thread.currentThread().getName();
				Thread.currentThread().setName("repositories/" + repositoryID);
				try {
					Repository repository = RepositoryManager.getDefaultInstance().getRepository(repositoryID);

					if (repository == null) {
						logger.info("UNKNOWN REPOSITORY: {}", repositoryID);
						response.sendError(HttpServletResponse.SC_NOT_FOUND, "unknown repository");
					}
					else {
						if (pathInfo.size() == 1) {
							_handleRepositoryRequest(repository, request, response);
						}
						else {
							String subPath = pathInfo.get(1);

							if (subPath.equals(Protocol.STATEMENTS) && pathInfo.size() == 2) {
								_handleStatementsRequest(repository, request, response);
							}
							else if (subPath.equals(Protocol.CONTEXTS) && pathInfo.size() == 2) {
								_handleContextListRequest(repository, request, response);
							}
							else if (subPath.equals(Protocol.SIZE) && pathInfo.size() == 2) {
								_handleSizeRequest(repository, request, response);
							}
							else if (subPath.equals(Protocol.NAMESPACES)) {
								List<String> params = pathInfo.subList(2, pathInfo.size());
								_handleNamespaceRequest(repository, params, request, response);
							}
							else {
								logger.info("INVALID PATH: {}", request.getPathInfo());
								response.sendError(HttpServletResponse.SC_NOT_FOUND);
							}
						}
					}
				}
				finally {
					Thread.currentThread().setName(oldName);
				}
			}
		}
		catch (RepositoryManagerConfigException e) {
			logger.error("REPOSITORY CONFIGURATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Repository configuration error: "
					+ e.getMessage());
		}
		catch (Exception e) {
			logger.error("FAILED TO HANDLE REPOSITORY REQUEST", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void _handleRepositoryListRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		if ("GET".equals(request.getMethod())) {
			logger.info("GET repository list");
			_exportRepositoryList(request, response);
		}
		else {
			logger.info("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _exportRepositoryList(HttpServletRequest request, HttpServletResponse response)
		throws IOException, UnsupportedQueryResultFormatException
	{
		// Export the repository list as a set of 4-tuples ["uri", "title",
		// "readable", "writable"]
		TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
		if (qrFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			logger.info("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return;
		}

		try {
			TupleQueryResultWriter qrWriter = QueryResultUtil.createWriter(qrFormat);

			OutputStream out = response.getOutputStream();
			qrWriter.setOutputStream(out);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType(qrFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=repositories."
					+ qrFormat.getFileExtension());

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
			logger.info("UNSUPPORTED QUERY RESULT FORMAT: " + e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.info("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
	}

	private void _handleRepositoryRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
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
				logger.info("UNSUPPORTED MEDIA TYPE: {}", contentType);
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				return;
			}
		}
		else {
			logger.info("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET, POST");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		_logRequestParameters(request);

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		if (queryStr != null) {
			_evaluateQuery(repository, queryStr, request, response);
		}
		else {
			logger.info("BAD REQUEST", "Missing parameter: " + QUERY_PARAM_NAME);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: " + QUERY_PARAM_NAME);
		}
	}

	private void _evaluateQuery(Repository repository, String queryStr, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		_logRequestParameters(request);

		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr.toUpperCase());

			if (queryLn == null) {
				logger.info("UNKNOWN QUERY LANGUAGE");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
				return;
			}
		}

		// determine if inferred triples should be included in query evaluation
		String inferredParam = request.getParameter(Protocol.INCLUDE_INFERRED_PARAM_NAME);
		boolean includeInferred = Boolean.parseBoolean(inferredParam);

		try {
			RepositoryConnection con = repository.getConnection();

			try {
				Query query = con.prepareQuery(queryLn, queryStr);
				query.setIncludeInferred(includeInferred);

				// FIXME: handle external bindings?

				if (query instanceof TupleQuery) {
					TupleQuery tupleQuery = (TupleQuery)query;

					TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
					if (qrFormat == null) {
						logger.info("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
						response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
								"No acceptable query result format found.");
						return;
					}

					TupleQueryResultWriter qrWriter = QueryResultUtil.createWriter(qrFormat);

					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType(qrFormat.getMIMEType());
					response.setHeader("Content-Disposition", "attachment; filename=queryresult."
							+ qrFormat.getFileExtension());

					OutputStream out = response.getOutputStream();
					try {
						qrWriter.setOutputStream(out);
						tupleQuery.evaluate(qrWriter);
					}
					finally {
						out.close();
					}
				}
				else if (query instanceof GraphQuery) {
					GraphQuery graphQuery = (GraphQuery)query;

					RDFFormat rdfFormat = _getAcceptableRDFFormat(request);
					if (rdfFormat == null) {
						logger.info("NO ACCEPTABLE RDF FORMAT FOUND");
						response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable RDF format found.");
						return;
					}

					RDFWriter rdfWriter = Rio.createWriter(rdfFormat);

					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType(rdfFormat.getMIMEType());
					response.setHeader("Content-Disposition", "attachment; filename=queryresult."
							+ rdfFormat.getFileExtension());

					OutputStream out = response.getOutputStream();
					try {
						rdfWriter.setOutputStream(out);
						graphQuery.evaluate(rdfWriter);
					}
					finally {
						out.close();
					}
				}
				else {
					logger.error("UNKNOWN QUERY TYPE: {}", queryStr);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown query type");
				}
			}
			finally {
				con.close();
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			logger.info("UNSUPPORTED QUERY LANGUAGE", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query language: " + queryLn);
		}
		catch (MalformedQueryException e) {
			logger.info("MALFORMED QUERY", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed query: " + e.getMessage());
		}
		catch (UnsupportedQueryResultFormatException e) {
			logger.info("UNSUPPORTED QUERY RESULT FORMAT", e);
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			logger.info("UNSUPPORTED RDF FORMAT", e);
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Unsupported RDF format: "
					+ e.getMessage());
		}
		catch (RDFHandlerException e) {
			logger.info("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.info("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (QueryEvaluationException e) {
			logger.info("QUERY EVALUATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Query evaluation error: "
					+ e.getMessage());
		}
		catch (RepositoryException e) {
			logger.info("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void _handleStatementsRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		String reqMethod = request.getMethod();

		if ("GET".equals(reqMethod)) {
			logger.info("GET statements");
			_exportStatements(repository, request, response);
		}
		else if ("POST".equals(reqMethod)) {
			String contentType = request.getContentType();

			if (Protocol.TXN_MIME_TYPE.equals(contentType)) {
				logger.info("POST transaction to repository");
				// An RDF transaction document is being posted to the server
				_handleTransaction(repository, request, response);
			}
			else {
				logger.info("POST data to repository");
				// An RDF document is being posted to the server
				_addData(repository, false, request, response);
			}
		}
		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT data in repository");
			_addData(repository, true, request, response);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE data from repository");
			_deleteData(repository, request, response);
		}
		else {
			logger.info("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET, POST, PUT, DELETE");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _exportStatements(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		_logRequestParameters(request);

		String subjectStr = request.getParameter(SUBJECT_PARAM_NAME);
		String predicateStr = request.getParameter(PREDICATE_PARAM_NAME);
		String objectStr = request.getParameter(OBJECT_PARAM_NAME);
		String[] contextParam = request.getParameterValues(CONTEXT_PARAM_NAME);
		String inferredParam = request.getParameter(INCLUDE_INFERRED_PARAM_NAME);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = null;
		URI pred = null;
		Value obj = null;
		Resource[] contexts = null;

		try {
			subj = Protocol.decodeResource(subjectStr, vf);
			pred = Protocol.decodeURI(predicateStr, vf);
			obj = Protocol.decodeValue(objectStr, vf);
			if (contextParam != null && contextParam.length > 0) {
				contexts = new Resource[contextParam.length];
				for (int i = 0; i < contextParam.length; i++) {
					contexts[i] = Protocol.decodeContext(contextParam[i], vf);
				}
			}
		}
		catch (IllegalArgumentException e) {
			logger.info("INVALID VALUE: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid value: " + e.getMessage());
			return;
		}

		boolean useInferencing = Boolean.parseBoolean(inferredParam);

		RDFFormat rdfFormat = _getAcceptableRDFFormat(request);
		if (rdfFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			logger.info("NO ACCEPTABLE RDF FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable RDF format found.");
			return;
		}

		try {
			RDFWriter rdfWriter = Rio.createWriter(rdfFormat);

			OutputStream out = response.getOutputStream();
			rdfWriter.setOutputStream(out);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType(rdfFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=statements."
					+ rdfFormat.getFileExtension());

			RepositoryConnection con = repository.getConnection();
			try {
				if (contextParam == null) {
					// No context was specified
					con.exportStatements(subj, pred, obj, useInferencing, rdfWriter);
				}
				else {
					con.exportStatements(subj, pred, obj, useInferencing, rdfWriter, contexts);
				}
			}
			finally {
				con.close();
			}

			out.close();
		}
		catch (UnsupportedRDFormatException e) {
			logger.info("NO RDF WRITER AVAILABLE FOR FORMAT: " + rdfFormat.getName(), e);
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No RDF writer available for format: "
					+ rdfFormat.getName());
			return;
		}
		catch (RDFHandlerException e) {
			logger.info("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("SAIL ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sail error: " + e.getMessage());
		}
	}

	private void _handleTransaction(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
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
					logger.debug("Executing operation: "+op.getClass().getName());
					op.execute(con);
				}

				con.commit();
			}
			finally {
				con.close();
			}
			logger.info("Transaction finished.");

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (SAXParseException e) {
			logger.info("MALFORMED TRANSACTION DATA");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed transaction data: "
					+ e.getMessage());
		}
		catch (SAXException e) {
			logger.warn("UNABLE TO PARSE TRANSACTION DATA", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to parse transaction data");
		}
		catch (IOException e) {
			logger.info("FAILED TO READ DATA", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read data");
		}
		catch (SailException e) {
			logger.info("SAIL UPDATE ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update data: "
					+ e.getMessage());
		}
	}

	private void _addData(Repository repository, boolean replaceCurrent, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		_logRequestParameters(request);

		RDFFormat rdfFormat = RDFFormat.forMIMEType(request.getContentType());
		if (rdfFormat == null) {
			logger.info("UNSUPPORTED MEDIA TYPE {}", request.getContentType());
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		String[] contextParam = request.getParameterValues(CONTEXT_PARAM_NAME);
		String baseURIParam = request.getParameter(BASEURI_PARAM_NAME);

		if (baseURIParam == null) {
			baseURIParam = "<foo:bar>";
			logger.info("No base URI specified, using dummy '{}'", baseURIParam);
		}

		ValueFactory vf = repository.getValueFactory();

		Resource[] contexts = null;
		URI baseURI;

		try {
			if (contextParam != null && contextParam.length > 0) {
				contexts = new Resource[contextParam.length];
				for (int i = 0; i < contextParam.length; i++) {
					contexts[i] = Protocol.decodeContext(contextParam[i], vf);
				}
			}
			baseURI = NTriplesUtil.parseURI(baseURIParam, vf);
		}
		catch (IllegalArgumentException e) {
			logger.info("INVALID VALUE: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid value: " + e.getMessage());
			return;
		}

		InputStream in = request.getInputStream();
		try {
			RepositoryConnection con = repository.getConnection();

			try {
				con.setAutoCommit(false);

				if (replaceCurrent) {
					if (contextParam == null) {
						// No context was specified, clear the entire repository
						con.clear();
					}
					else {
						// Only replace the data in the specified context
						con.clear(contexts);
					}
				}

				if (contexts == null) {
					con.add(in, baseURI.toString(), rdfFormat);
				}
				else {
					con.add(in, baseURI.toString(), rdfFormat, contexts);
				}
				con.commit();
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (IOException e) {
			logger.info("FAILED TO READ DATA", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read data");
		}
		catch (UnsupportedRDFormatException e) {
			logger.info("NO RDF PARSER AVAILABLE FOR FORMAT: {}", rdfFormat.getName(), e);
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"No RDF parser available for format " + rdfFormat.getName());
		}
		catch (RDFParseException e) {
			logger.info("PARSE ERROR");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parse error at line " + e.getLineNumber()
					+ ": " + e.getMessage());
		}
		catch (RepositoryException e) {
			logger.info("REPOSITORY UPDATE ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to update data: "
					+ e.getMessage());
		}
	}

	private void _deleteData(Repository repository, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		_logRequestParameters(request);

		String subjectStr = request.getParameter(SUBJECT_PARAM_NAME);
		String predicateStr = request.getParameter(PREDICATE_PARAM_NAME);
		String objectStr = request.getParameter(OBJECT_PARAM_NAME);
		String[] contextParam = request.getParameterValues(CONTEXT_PARAM_NAME);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = null;
		URI pred = null;
		Value obj = null;
		Resource[] contexts = null;

		try {
			subj = Protocol.decodeResource(subjectStr, vf);
			pred = Protocol.decodeURI(predicateStr, vf);
			obj = Protocol.decodeValue(objectStr, vf);
			if (contextParam != null && contextParam.length > 0) {
				contexts = new Resource[contextParam.length];
				for (int i = 0; i < contextParam.length; i++) {
					contexts[i] = Protocol.decodeContext(contextParam[i], vf);
				}
			}
		}
		catch (IllegalArgumentException e) {
			logger.info("INVALID VALUE: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid value: " + e.getMessage());
			return;
		}

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				if (contexts == null) {
					con.remove(subj, pred, obj);
				}
				else {
					con.remove(subj, pred, obj, contexts);
				}
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (RepositoryException e) {
			logger.info("REPOSITORY UPDATE ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to delete data: "
					+ e.getMessage());
		}
	}

	private void _handleContextListRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		if ("GET".equals(request.getMethod())) {
			logger.info("GET contexts for repository");
			_exportContextList(repository, request, response);
		}
		else {
			logger.info("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _exportContextList(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		// Export the list of context identifiers as a set of 1-tuples
		TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
		if (qrFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			logger.info("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable query result format found.");
			return;
		}

		try {
			TupleQueryResultWriter qrWriter = QueryResultUtil.createWriter(qrFormat);

			OutputStream out = response.getOutputStream();
			qrWriter.setOutputStream(out);

			response.setStatus(HttpServletResponse.SC_OK);
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
			logger.info("UNSUPPORTED QUERY RESULT FORMAT: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.info("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Repository error: "
					+ e.getMessage());
		}
	}

	private void _handleSizeRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		if ("GET".equals(request.getMethod())) {
			logger.info("GET size of repository");
			_exportSize(repository, request, response);
		}
		else {
			logger.info("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _exportSize(Repository repository, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		_logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();
		
		String[] contextParam = request.getParameterValues(Protocol.CONTEXT_PARAM_NAME);

		Resource[] contexts = null;

		try {
			if (contextParam != null && contextParam.length > 0) {
				contexts = new Resource[contextParam.length];
				for (int i = 0; i < contextParam.length; i++) {
					contexts[i] = Protocol.decodeContext(contextParam[i], vf);
				}
			}
		}
		catch (IllegalArgumentException e) {
			logger.info("INVALID VALUE: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid value: " + e.getMessage());
			return;
		}

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				long size = contexts == null ? con.size() : con.size(contexts);

				response.setStatus(HttpServletResponse.SC_OK);
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
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Repository error: "
					+ e.getMessage());
		}
	}

	private void _handleNamespaceRequest(Repository repository, List<String> params,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		if (params.size() == 0) {
			if ("GET".equals(request.getMethod())) {
				logger.info("GET namespace list");
				_exportNamespaceList(repository, request, response);
			}
			else if ("DELETE".equals(request.getMethod())) {
				logger.info("DELETE namespaces");
				_clearNamespaces(repository, response);
			}
			else {
				logger.info("METHOD NOT ALLOWED");
				response.setHeader("Allow", "GET, DELETE");
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}
		else if (params.size() == 1) {
			String prefix = params.get(0);
			logger.info("GET namespace for prefix {}" + prefix);

			if ("GET".equals(request.getMethod())) {
				_exportNamespace(repository, prefix, response);
			}
			else if ("PUT".equals(request.getMethod())) {
				logger.info("PUT prefix {}", prefix);
				_setNamespace(repository, prefix, request, response);
			}
			else if ("DELETE".equals(request.getMethod())) {
				logger.info("DELETE prefix {}", prefix);
				_removeNamespace(repository, prefix, response);
			}
			else {
				logger.info("METHOD NOT ALLOWED");
				response.setHeader("Allow", "GET, PUT, DELETE");
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}
		else {
			logger.info("INVALID PATH: " + request.getPathInfo());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void _exportNamespaceList(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		// Export the list of namespaces and their prefixes as a set of
		// 2-tuples ["prefix", "name"]
		TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
		if (qrFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			logger.info("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable query result format found.");
			return;
		}

		try {
			TupleQueryResultWriter qrWriter = QueryResultUtil.createWriter(qrFormat);

			OutputStream out = response.getOutputStream();
			qrWriter.setOutputStream(out);

			response.setStatus(HttpServletResponse.SC_OK);
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
			logger.info("UNSUPPORTED QUERY RESULT FORMAT: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.info("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to export namespace list: "
					+ e.getMessage());
		}
	}

	// Exports the namespace for the specified prefix
	private void _exportNamespace(Repository repository, String prefix, HttpServletResponse response)
		throws IOException
	{
		try {
			RepositoryConnection con = repository.getConnection();

			try {
				String namespace = con.getNamespace(prefix);

				if (namespace != null) {
					response.setStatus(HttpServletResponse.SC_OK);
					// FIXME: Specify character encoding as text/plain default to
					// US-ASCII?
					response.setContentType("text/plain");
					PrintWriter writer = response.getWriter();
					writer.print(namespace);
					writer.close();
				}
				else {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to export namespace: "
					+ e.getMessage());
		}
	}

	private void _clearNamespaces(Repository repository, HttpServletResponse response)
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
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getWriter().close();
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to remove namespaces: "
					+ e.getMessage());
		}
	}

	private void _setNamespace(Repository repository, String prefix, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		String namespace = IOUtil.readString(request.getReader());

		namespace = namespace.trim();

		if (namespace.length() == 0) {
			logger.info("NAMESPACE NAME MISSING");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No namespace name found in request body");
			return;
		}
		// TODO: perform some sanity checks on the namespace string

		logger.info("namespace={}", namespace);

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.setNamespace(prefix, namespace);

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				response.getWriter().close();
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to set namespace prefix: "
					+ e.getMessage());
		}
	}

	private void _removeNamespace(Repository repository, String prefix, HttpServletResponse response)
		throws IOException
	{
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.removeNamespace(prefix);

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				response.getWriter().close();
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			logger.error("REPOSITORY ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Unable to remove namespace prefix: " + e.getMessage());
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
	private List<String> _splitFilePath(String filePath) {
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
	 * -header specified in the supplied request. The parameter value, if
	 * present, takes presedence over the header value.
	 * 
	 * @param request
	 *        The request.
	 * @return An RDFFormat object representing an RDF file format that can be
	 *         handled by the client, or <tt>null</tt> if no such format could
	 *         be determined.
	 */
	private RDFFormat _getAcceptableRDFFormat(HttpServletRequest request) {
		logAcceptableFormats(request);

		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			// FIXME: base this on the RDF writers that are actually available
			Iterator<String> mimeTypeIter = new ConvertingIterator<RDFFormat, String>(
					RDFFormat.values().iterator())
			{

				@Override
				protected String convert(RDFFormat sourceObject)
				{
					return sourceObject.getMIMEType();
				}
			};

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypeIter, request);
		}

		if (mimeType != null) {
			return RDFFormat.forMIMEType(mimeType);
		}

		return null;
	}

	/**
	 * Determines an appropriate query result file format based on the 'Accept'
	 * parameter or -header specified in the supplied request. The parameter
	 * value, if present, takes presedence over the header value.
	 * 
	 * @param request
	 *        The request.
	 * @return A TupleQueryResultFormat object representing an query result
	 *         format that can be handled by the client, or <tt>null</tt> if no
	 *         such format could be determined.
	 */
	private TupleQueryResultFormat _getAcceptableQueryResultFormat(HttpServletRequest request) {
		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);
		logAcceptableFormats(request);

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			// FIXME: base this on the writers that are actually available
			Iterator<String> mimeTypeIter = new ConvertingIterator<TupleQueryResultFormat, String>(
					TupleQueryResultFormat.values().iterator())
			{

				@Override
				protected String convert(TupleQueryResultFormat sourceObject)
				{
					return sourceObject.getMIMEType();
				}
			};

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypeIter, request);
		}

		if (mimeType != null) {
			return TupleQueryResultFormat.forMIMEType(mimeType);
		}

		return null;
	}

	/**
	 * Logs all request parameters of the supplied request.
	 */
	private void _logRequestParameters(HttpServletRequest request) {
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
