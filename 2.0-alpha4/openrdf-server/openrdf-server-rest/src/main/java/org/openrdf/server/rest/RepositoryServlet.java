/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.server.rest;

import static org.openrdf.protocol.rest.Protocol.ACCEPT_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.NAMESPACE_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.NAMESPACE_PREFIX_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.NULL_CONTEXT_PARAM_VALUE;
import static org.openrdf.protocol.rest.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.QUERY_PARAM_NAME;
import static org.openrdf.protocol.rest.Protocol.SUBJECT_PARAM_NAME;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.protocol.rest.Protocol;
import org.openrdf.protocol.rest.ProtocolUtil;
import org.openrdf.protocol.transaction.TransactionReader;
import org.openrdf.protocol.transaction.operations.OperationList;
import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.QueryParserUtil;
import org.openrdf.querylanguage.UnsupportedQueryLanguageException;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.Query;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResultFormat;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultUtil;
import org.openrdf.queryresult.TupleQueryResultWriter;
import org.openrdf.queryresult.UnsupportedQueryResultFormatException;
import org.openrdf.queryresult.impl.ListSolution;
import org.openrdf.repository.Connection;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryManager;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryManagerConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.util.http.HTTPUtil;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.log.ThreadLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Servlet that exports information about available repositories.
 */
public class RepositoryServlet extends GenericServlet {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -4058073101576755194L;

	// MIME types of supported RDF formats
	// FIXME: build this array based on what's available in Rio
	private static String[] RDF_MIME_TYPES = new String[] {
			RDFFormat.RDFXML.getMIMEType(),
			RDFFormat.TURTLE.getMIMEType(),
			RDFFormat.NTRIPLES.getMIMEType(),
			RDFFormat.TRIX.getMIMEType(),
			RDFFormat.N3.getMIMEType() };

	// MIME types of supported query result formats
	// FIXME: build this array based on what's available in
	// TupleQueryResultFormat
	private static String[] QUERY_RESULT_MIME_TYPES = new String[] {
			TupleQueryResultFormat.SPARQL.getMIMEType(),
			TupleQueryResultFormat.BINARY.getMIMEType(),
			TupleQueryResultFormat.JSON.getMIMEType() };

	/*---------*
	 * Methods *
	 *---------*/

	// Implements GenericServlet.service(...)
	public void service(ServletRequest request, ServletResponse response)
		throws IOException
	{
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			_service((HttpServletRequest)request, (HttpServletResponse)response);
		}
	}

	protected void _service(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		try {
			ThreadLog.log("=== handling repository request ===");

			List<String> pathInfo = null;

			String pathInfoStr = request.getPathInfo();
			if (pathInfoStr != null) {
				pathInfo = _splitFilePath(pathInfoStr);
			}

			ThreadLog.log(request.getMethod() + " " + ((pathInfoStr == null) ? "/" : pathInfoStr));

			if (pathInfo == null || pathInfo.size() == 0) {
				// No repository ID was specified
				_handleRepositoryListRequest(request, response);
			}
			else {
				// First component of the path is the repository ID
				String repositoryID = pathInfo.get(0);

				Repository repository = RepositoryManager.getDefaultServer().getRepository(repositoryID);

				if (repository == null) {
					ThreadLog.log("UNKNOWN REPOSITORY: " + repositoryID);
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "unknown repository");
					return;
				}

				if (pathInfo.size() == 1) {
					_handleRepositoryRequest(repository, request, response);
				}
				else if (pathInfo.size() == 2) {
					String subPath = pathInfo.get(1);

					if (subPath.equals("contexts")) {
						_handleContextListRequest(repository, request, response);
					}
					else if (subPath.equals("namespaces")) {
						_handleNamespaceListRequest(repository, request, response);
					}
					else {
						ThreadLog.log("INVALID PATH: " + request.getPathInfo());
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
				}
				else {
					ThreadLog.log("INVALID PATH: " + request.getPathInfo());
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}

			ThreadLog.log("=== repository request handled ===");
		}
		catch (RepositoryManagerConfigException e) {
			ThreadLog.error("REPOSITORY CONFIGURATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Repository configuration error: "
					+ e.getMessage());
		}
		catch (Exception e) {
			ThreadLog.error("FAILED TO HANDLE REPOSITORY REQUEST", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void _handleRepositoryListRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		if ("GET".equals(request.getMethod())) {
			try {
				_exportRepositoryList(request, response);
			}
			catch (UnsupportedQueryResultFormatException e) {
				ThreadLog.log("UNSUPPORTED QUERY RESULT FORMAT: " + e.getMessage());
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query result format: "
						+ e.getMessage());
			}
		}
		else {
			ThreadLog.log("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _handleContextListRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		if ("GET".equals(request.getMethod())) {
			try {
				_exportContextList(repository, request, response);
			}
			catch (UnsupportedQueryResultFormatException e) {
				ThreadLog.log("UNSUPPORTED QUERY RESULT FORMAT: " + e.getMessage());
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query result format: "
						+ e.getMessage());
			}
		}
		else {
			ThreadLog.log("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _handleNamespaceListRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		if ("GET".equals(request.getMethod())) {
			try {
				_exportNamespaceList(repository, request, response);
			}
			catch (UnsupportedQueryResultFormatException e) {
				ThreadLog.log("UNSUPPORTED QUERY RESULT FORMAT: " + e.getMessage());
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query result format: "
						+ e.getMessage());
			}
		}
		else if ("POST".equals(request.getMethod())) {
			_changeNamespacePrefix(repository, request, response);
		}
		else {
			ThreadLog.log("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET, POST");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	private void _handleRepositoryRequest(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		String reqMethod = request.getMethod();

		if ("GET".equals(reqMethod)) {
			String queryStr = request.getParameter(QUERY_PARAM_NAME);
			if (queryStr != null) {
				ThreadLog.log(QUERY_PARAM_NAME + "=" + queryStr);
				_evaluateQuery(repository, queryStr, request, response);
			}
			else {
				_exportStatements(repository, request, response);
			}
		}
		else if ("POST".equals(reqMethod)) {
			if (request.getContentType().equals("application/x-rdftransaction")) {
				// An RDF transaction document is being posted to the server
				_handleTransaction(repository, request, response);
			}
			else if (_getRDFContentType(request) != null) {
				// An RDF document is being posted to the server
				_addData(repository, false, request, response);
			}
			else {
				String queryStr = request.getParameter(QUERY_PARAM_NAME);
				if (queryStr != null) {
					ThreadLog.log(QUERY_PARAM_NAME + "=" + queryStr);
					_evaluateQuery(repository, queryStr, request, response);
				}
				else {
					ThreadLog.log("BAD REQUEST", "Missing parameter: " + QUERY_PARAM_NAME);
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: "
							+ QUERY_PARAM_NAME);
				}
			}
		}
		else if (repository.isWritable()) {
			if ("PUT".equals(reqMethod)) {
				_addData(repository, true, request, response);
			}
			else if ("DELETE".equals(reqMethod)) {
				_deleteData(repository, request, response);
			}
			else {
				ThreadLog.log("METHOD NOT ALLOWED");
				response.setHeader("Allow", "GET, POST, PUT, DELETE");
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}
		else {
			ThreadLog.log("METHOD NOT ALLOWED");
			response.setHeader("Allow", "GET, POST");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "repository is not writeable");
		}
	}

	private void _handleTransaction(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		// TODO set the correct baseuri for transactions
		String baseURI = "";

		// application/x-rdftransaction
		InputStream in = request.getInputStream();
		try {
			TransactionReader reader = new TransactionReader();
			OperationList ops = reader.parseTransactionFromSerialization(in);

			SailConnection con = repository.getSail().getConnection();

			try {
				ops.executeOperationsOnTransaction(con);
				con.commit();
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (SAXParseException e) {
			ThreadLog.log("MALFORMED TRANSACTION DATA");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed transaction data: "
					+ e.getMessage());
		}
		catch (SAXException e) {
			ThreadLog.warning("UNABLE TO PARSE TRANSACTION DATA", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to parse transaction data");
		}
		catch (IOException e) {
			ThreadLog.log("FAILED TO READ DATA", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read data");
		}
		catch (SailException e) {
			ThreadLog.log("SAIL UPDATE ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update data: "
					+ e.getMessage());
		}

	}

	private void _exportRepositoryList(HttpServletRequest request, HttpServletResponse response)
		throws IOException, UnsupportedQueryResultFormatException
	{
		// Export the list of context identifiers as a set of 1-tuples
		TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
		if (qrFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			ThreadLog.log("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return;
		}

		OutputStream out = response.getOutputStream();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(qrFormat.getMIMEType());
		TupleQueryResultWriter qrWriter = TupleQueryResultUtil.createWriter(qrFormat, out);

		try {
			StringBuffer requestURL = request.getRequestURL();
			if (requestURL.charAt(requestURL.length() - 1) != '/') {
				requestURL.append('/');
			}
			String namespace = requestURL.toString();

			List<String> columnNames = Arrays.asList("uri", "title", "readable", "writeable");
			qrWriter.startQueryResult(columnNames, true, false);

			ValueFactory vf = new ValueFactoryImpl();

			for (RepositoryConfig repConfig : RepositoryManager.getDefaultServer().getServerConfig().getRepositoryConfigs())
			{
				URI uri = vf.createURI(namespace, repConfig.getID());
				Literal title = vf.createLiteral(repConfig.getTitle());
				Literal readable = vf.createLiteral(repConfig.isWorldReadable());
				Literal writeable = vf.createLiteral(repConfig.isWorldWriteable());

				Solution solution = new ListSolution(columnNames, uri, title, readable, writeable);
				qrWriter.handleSolution(solution);
			}

			qrWriter.endQueryResult();
			out.close();
		}
		catch (TupleQueryResultHandlerException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
	}

	private void _exportContextList(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, UnsupportedQueryResultFormatException
	{
		// Export the list of context identifiers as a set of 1-tuples
		TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
		if (qrFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			ThreadLog.log("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable query result format found.");
			return;
		}

		OutputStream out = response.getOutputStream();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(qrFormat.getMIMEType());
		TupleQueryResultWriter qrWriter = TupleQueryResultUtil.createWriter(qrFormat, out);

		try {
			List<String> columnNames = Arrays.asList("contextID");
			qrWriter.startQueryResult(columnNames, true, false);

			Connection con = repository.getConnection();
			CloseableIterator<? extends Resource> contextIter = con.getContextIDs();

			try {
				while (contextIter.hasNext()) {
					Solution solution = new ListSolution(columnNames, contextIter.next());
					qrWriter.handleSolution(solution);
				}
			}
			finally {
				contextIter.close();
				con.close();
			}

			qrWriter.endQueryResult();
			out.close();
		}
		catch (TupleQueryResultHandlerException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (SailException e) {
			ThreadLog.error("SAIL ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sail error: " + e.getMessage());
		}
	}

	private void _exportNamespaceList(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, UnsupportedQueryResultFormatException
	{
		// Export the list of namespaces and their prefixes as a set of
		// 2-tuples ["prefix", "name"]
		TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
		if (qrFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			ThreadLog.log("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable query result format found.");
			return;
		}

		OutputStream out = response.getOutputStream();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(qrFormat.getMIMEType());
		TupleQueryResultWriter qrWriter = TupleQueryResultUtil.createWriter(qrFormat, out);

		try {
			List<String> columnNames = Arrays.asList("prefix", "namespace");
			qrWriter.startQueryResult(columnNames, true, false);

			Connection con = repository.getConnection();

			CloseableIterator<? extends Namespace> iter = con.getNamespaces();

			try {
				while (iter.hasNext()) {
					Namespace ns = iter.next();

					Literal prefix = new LiteralImpl(ns.getPrefix());
					Literal namespace = new LiteralImpl(ns.getName());

					Solution solution = new ListSolution(columnNames, prefix, namespace);
					qrWriter.handleSolution(solution);
				}
			}
			finally {
				iter.close();
				con.close();
			}

			qrWriter.endQueryResult();
			out.close();
		}
		catch (TupleQueryResultHandlerException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (SailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void _changeNamespacePrefix(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		String namespace = request.getParameter(NAMESPACE_PARAM_NAME);
		String prefix = request.getParameter(NAMESPACE_PREFIX_PARAM_NAME);

		if (namespace == null) {
			ThreadLog.log("MISSING PARAMETER: " + NAMESPACE_PARAM_NAME);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: " + NAMESPACE_PARAM_NAME);
			return;
		}
		if (prefix == null) {
			ThreadLog.log("MISSING PARAMETER: " + NAMESPACE_PREFIX_PARAM_NAME);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: "
					+ NAMESPACE_PREFIX_PARAM_NAME);
			return;
		}

		ThreadLog.log(NAMESPACE_PARAM_NAME + "=" + namespace);
		ThreadLog.log(NAMESPACE_PREFIX_PARAM_NAME + "=" + prefix);

		try {
			Connection con = repository.getConnection();
			try {
				con.setNamespace(namespace, prefix);
			}
			finally {
				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getWriter().close();
		}
		catch (SailException e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			PrintWriter writer = response.getWriter();
			writer.println("Unable to change namespace prefix: ");
			writer.println(e.getMessage());
			writer.close();
		}
	}

	private void _exportStatements(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		String subjectStr = request.getParameter(SUBJECT_PARAM_NAME);
		if (subjectStr != null) {
			ThreadLog.log(SUBJECT_PARAM_NAME + "=" + subjectStr);
		}

		String predicateStr = request.getParameter(PREDICATE_PARAM_NAME);
		if (predicateStr != null) {
			ThreadLog.log(PREDICATE_PARAM_NAME + "=" + predicateStr);
		}

		String objectStr = request.getParameter(OBJECT_PARAM_NAME);
		if (objectStr != null) {
			ThreadLog.log(OBJECT_PARAM_NAME + "=" + objectStr);
		}

		String useInferencingStr = request.getParameter(INCLUDE_INFERRED_PARAM_NAME);

		boolean queryNullContext = false;

		String contextStr = null;
		Resource context = null;
		String contextParam = request.getParameter(CONTEXT_PARAM_NAME);
		if (NULL_CONTEXT_PARAM_VALUE.equals(contextParam)) {
			queryNullContext = true;
		}
		else {
			try {
				context = (Resource)ProtocolUtil.decodeParameterValue(contextParam, repository.getValueFactory());
			}
			catch (IllegalArgumentException e) {
				ThreadLog.log("INVALID CONTEXT IDENTIFIER: " + contextStr);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid context identifier: "
						+ contextStr);
				return;
			}
		}

		RDFFormat rdfFormat = _getAcceptableRDFFormat(request);
		if (rdfFormat == null) {
			// No acceptable format was found, send 406 as required by RFC 2616
			ThreadLog.log("NO ACCEPTABLE RDF FORMAT FOUND");
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable RDF format found.");
			return;
		}

		RDFWriter rdfWriter;
		try {
			rdfWriter = Rio.createWriter(rdfFormat);
		}
		catch (UnsupportedRDFormatException e) {
			ThreadLog.log("NO RDF WRITER AVAILABLE FOR FORMAT: " + rdfFormat.getName(), e);
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No RDF writer available for format: "
					+ rdfFormat.getName());
			return;
		}

		try {
			OutputStream out = response.getOutputStream();
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType(rdfFormat.getMIMEType());

			rdfWriter.setOutputStream(out);

			Connection con = repository.getConnection();
			try {
				if (subjectStr == null && predicateStr == null && objectStr == null) {
					if (context == null && !queryNullContext) {
						// No context was specified, export all statements
						con.export(rdfWriter);
					}
					else {
						// Export statements for specified context
						con.exportContext(context, rdfWriter);
					}
				}
				else {
					Resource subj = null;
					if (subjectStr != null) {
						subj = (Resource)ProtocolUtil.decodeParameterValue(subjectStr, repository.getValueFactory());
					}
					URI pred = null;
					if (predicateStr != null) {
						pred = (URI)ProtocolUtil.decodeParameterValue(predicateStr, repository.getValueFactory());
					}
					Value obj = null;
					if (objectStr != null) {
						obj = ProtocolUtil.decodeParameterValue(objectStr, repository.getValueFactory());
					}
					boolean useInferencing = false;
					if (useInferencingStr != null) {
						useInferencing = useInferencingStr.equalsIgnoreCase("true");

					}

					if (context == null && !queryNullContext) {
						con.exportStatements(subj, pred, obj, useInferencing, rdfWriter);
					}
					else {
						con.exportStatements(subj, pred, obj, context, useInferencing, rdfWriter);
					}
				}
			}
			finally {
				con.close();
			}

			out.close();
		}
		catch (RDFHandlerException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (SailException e1) {
			ThreadLog.error("SAIL ERROR", e1);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sail error: " + e1.getMessage());
			return;
		}
	}

	private void _evaluateQuery(Repository repository, String queryStr, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);

		// determine if inferred triples should be included in query evaluation
		String inferredParam = request.getParameter(Protocol.INCLUDE_INFERRED_PARAM_NAME);
		boolean includeInferred = true;
		if (inferredParam != null) {
			includeInferred = Boolean.getBoolean(inferredParam);
		}

		if (queryLnStr != null) {
			ThreadLog.log(QUERY_LANGUAGE_PARAM_NAME + "=" + queryLnStr);

			queryLn = QueryLanguage.valueOf(queryLnStr.toUpperCase());

			if (queryLn == null) {
				ThreadLog.log("UNKNOWN QUERY LANGUAGE");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
				return;
			}
		}

		try {
			Query query = QueryParserUtil.parseQuery(queryLn, queryStr);

			OutputStream out = response.getOutputStream();

			if (query instanceof TupleQuery) {
				TupleQueryResultFormat qrFormat = _getAcceptableQueryResultFormat(request);
				if (qrFormat == null) {
					// No acceptable format was found, send 406 as required by
					// RFC 2616
					ThreadLog.log("NO ACCEPTABLE QUERY RESULT FORMAT FOUND");
					response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
							"No acceptable query result format found.");
					return;
				}

				TupleQueryResultWriter qrWriter = TupleQueryResultUtil.createWriter(qrFormat, out);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(qrFormat.getMIMEType());

				Connection con = repository.getConnection();
				try {
					con.evaluate((TupleQuery)query, includeInferred, qrWriter);
				}
				finally {
					con.close();
				}
			}
			else if (query instanceof GraphQuery) {
				RDFFormat rdfFormat = _getAcceptableRDFFormat(request);
				if (rdfFormat == null) {
					// No acceptable format was found, send 406 as required by
					// RFC 2616
					ThreadLog.log("NO ACCEPTABLE RDF FORMAT FOUND");
					response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No acceptable RDF format found.");
					return;
				}

				RDFWriter rdfWriter;
				try {
					rdfWriter = Rio.createWriter(rdfFormat, out);
				}
				catch (UnsupportedRDFormatException e) {
					ThreadLog.log("NO RDF WRITER AVAILABLE FOR FORMAT: " + rdfFormat.getName(), e);
					response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
							"No RDF writer available for format: " + rdfFormat.getName());
					return;
				}

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(rdfFormat.getMIMEType());
				Connection con = repository.getConnection();
				try {
					con.evaluate((GraphQuery)query, includeInferred, rdfWriter);
				}
				finally {
					con.close();
				}
			}

			out.close();
		}
		catch (MalformedQueryException e) {
			ThreadLog.log("MALFORMED QUERY");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed query: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (UnsupportedQueryLanguageException e) {
			// FIXME: send appropriate error
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (UnsupportedQueryResultFormatException e) {
			// FIXME: send appropriate error
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
		catch (SailException e) {
			ThreadLog.log("SERIALIZATION ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: "
					+ e.getMessage());
		}
	}

	private void _addData(Repository repository, boolean replaceCurrent, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException
	{
		String contextStr = request.getParameter(CONTEXT_PARAM_NAME);
		if (contextStr != null) {
			ThreadLog.log(CONTEXT_PARAM_NAME + "=" + contextStr);
		}

		Resource context = null;
		try {
			context = (Resource)ProtocolUtil.decodeParameterValue(contextStr, repository.getValueFactory());
		}
		catch (IllegalArgumentException e) {
			ThreadLog.log("INVALID CONTEXT IDENTIFIER: " + contextStr);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid context identifier: " + contextStr);
			return;
		}

		String baseURIStr = request.getParameter(BASEURI_PARAM_NAME);
		if (baseURIStr != null) {
			ThreadLog.log(BASEURI_PARAM_NAME + "=" + baseURIStr);
		}
		else {
			baseURIStr = "foo:bar";
			ThreadLog.log("No base URI specified, using dummy '" + baseURIStr + "'");
		}

		RDFFormat rdfFormat = _getRDFContentType(request);
		if (rdfFormat == null) {
			ThreadLog.log("No content-type specified for data, assuming RDF/XML");
			rdfFormat = RDFFormat.RDFXML;
		}

		InputStream in = request.getInputStream();
		try {
			Connection con = repository.getConnection();
			con.setAutoCommit(false);

			try {
				if (replaceCurrent) {
					// remove current data from repository
					if (contextStr == null) {
						// No context was specified, clear the entire repository
						con.clear();
					}
					else {
						// Only replace the data in the specified context
						con.clearContext(context);
					}
				}

				URI baseURI = (URI)ProtocolUtil.decodeParameterValue(baseURIStr, repository.getValueFactory());

				con.add(in, baseURI.toString(), rdfFormat, context);

				con.commit();

				// Indicate success with a 204 NO CONTENT response
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				response.getOutputStream().close();
			}
			finally {
				// Roll the transation back in case the commit wasn't reached
				if (con.isOpen()) {
					con.rollback();
				}

				con.close();
			}
		}
		catch (IOException e) {
			ThreadLog.log("FAILED TO READ DATA", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read data");
		}
		catch (UnsupportedRDFormatException e) {
			ThreadLog.log("NO RDF PARSER AVAILABLE FOR FORMAT: " + rdfFormat.getName(), e);
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"No RDF parser available for format " + rdfFormat.getName());
		}
		catch (RDFParseException e) {
			ThreadLog.log("PARSE ERROR");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parse error at line " + e.getLineNumber()
					+ ": " + e.getMessage());
		}
		catch (SailException e) {
			ThreadLog.log("SAIL UPDATE ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to update data: "
					+ e.getMessage());
		}
	}

	private void _deleteData(Repository repository, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String contextStr = request.getParameter(CONTEXT_PARAM_NAME);
		if (contextStr != null) {
			ThreadLog.log(CONTEXT_PARAM_NAME + "=" + contextStr);
		}

		Resource context = null;
		try {
			context = (Resource)ProtocolUtil.decodeParameterValue(contextStr, repository.getValueFactory());
		}
		catch (IllegalArgumentException e) {
			ThreadLog.log("INVALID CONTEXT IDENTIFIER: " + contextStr);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid context identifier: " + contextStr);
			return;
		}

		try {
			Connection con = repository.getConnection();
			try {
				if (contextStr == null) {
					// No context was specified, clear the entire repository
					con.clear();
				}
				else {
					// Clear the specified context
					con.clearContext(context);
				}
			}
			finally {
				// Roll the transation back in case the commit wasn't reached
				if (con.isOpen()) {
					con.rollback();
				}

				con.close();
			}

			// Indicate success with a 204 NO CONTENT response
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getOutputStream().close();
		}
		catch (SailException e) {
			ThreadLog.log("SAIL UPDATE ERROR", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to delete data: "
					+ e.getMessage());
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
	 * Determines the RDF file format for the content based on the content type
	 * specified in the 'Content-Type' header in the supplied request.
	 * 
	 * @param request
	 *        The request.
	 * @return An RDFFormat object representing the specified RDF file format, or
	 *         <tt>null</tt> if no content type was specified or if it could
	 *         not be resolved to a known RDF file format.
	 */
	private RDFFormat _getRDFContentType(HttpServletRequest request) {
		String contentType = request.getContentType();

		if (contentType != null) {
			return RDFFormat.forMIMEType(contentType);
		}

		return null;
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
		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);

		if (mimeType == null) {
			mimeType = HTTPUtil.selectPreferredMIMEType(RDF_MIME_TYPES, request);
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

		if (mimeType == null) {
			mimeType = HTTPUtil.selectPreferredMIMEType(QUERY_RESULT_MIME_TYPES, request);
		}

		if (mimeType != null) {
			return TupleQueryResultFormat.forMIMEType(mimeType);
		}

		return null;
	}
}
