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
package org.openrdf.http.server.repository.transaction;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.DEFAULT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INSERT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.NAMED_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.REMOVE_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.USING_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.USING_NAMED_GRAPH_PARAM_NAME;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.lang.FileFormat;
import info.aduna.lang.service.FileFormatServiceRegistry;
import info.aduna.webapp.views.EmptySuccessView;
import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.Protocol.Action;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.HTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.BooleanQueryResultView;
import org.openrdf.http.server.repository.GraphQueryResultView;
import org.openrdf.http.server.repository.QueryResultView;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.http.server.repository.TupleQueryResultView;
import org.openrdf.http.server.repository.statements.ExportStatementsView;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.AbstractRDFHandler;

/**
 * Handles requests for transaction creation on a repository.
 * 
 * @since 2.8.0
 * @author Jeen Broekstra
 */
public class TransactionController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public TransactionController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_POST, "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ModelAndView result;

		String reqMethod = request.getMethod();
		UUID transactionId = getTransactionID(request);
		logger.debug("transaction id: {}", transactionId);
		RepositoryConnection connection = ActiveTransactionRegistry.INSTANCE.getTransactionConnection(transactionId);

		if (connection == null) {
			logger.warn("could not find connection for transaction id {}", transactionId);
			throw new ClientHTTPException(SC_BAD_REQUEST,
					"unable to find registerd connection for transaction id '" + transactionId + "'");
		}

		try {
			if ("PUT".equals(reqMethod)) {
				// TODO filter for appropriate PUT operations
				logger.info("PUT txn operation");
				result = processTransactionOperation(connection, request, response);
				logger.info("PUT txn operation request finished.");
			}
			else if (METHOD_POST.equals(reqMethod)) {
				// TODO filter for appropriate POST operations
				logger.info("POST txn operation");
				result = processTransactionOperation(connection, request, response);
				logger.info("POST txn operation request finished.");
			}
			else if ("DELETE".equals(reqMethod)) {
				logger.info("DELETE transaction");
				try {
					connection.rollback();
				}
				finally {
					ActiveTransactionRegistry.INSTANCE.deregister(transactionId);
					connection.close();
				}

				result = new ModelAndView(EmptySuccessView.getInstance());
				logger.info("DELETE transaction request finished.");
			}
			else {
				throw new ClientHTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed: "
						+ reqMethod);
			}
		}
		finally {
			ActiveTransactionRegistry.INSTANCE.returnTransactionConnection(transactionId);
		}
		return result;
	}

	private UUID getTransactionID(HttpServletRequest request)
		throws ClientHTTPException
	{
		String pathInfoStr = request.getPathInfo();

		UUID txnID = null;

		if (pathInfoStr != null && !pathInfoStr.equals("/")) {
			String[] pathInfo = pathInfoStr.substring(1).split("/");
			// should be of the form: /<Repository>/transactions/<txnID>
			if (pathInfo.length == 3) {
				try {
					txnID = UUID.fromString(pathInfo[2]);
					logger.debug("txnID is '{}'", txnID);
				}
				catch (IllegalArgumentException e) {
					throw new ClientHTTPException(SC_BAD_REQUEST, "not a valid transaction id: " + pathInfo[2]);
				}
			}
			else {
				logger.warn("could not determine transaction id from path info {} ", pathInfoStr);
			}
		}

		return txnID;
	}

	private ModelAndView processTransactionOperation(RepositoryConnection conn, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, HTTPException
	{
		ProtocolUtil.logRequestParameters(request);
		Action action = Action.valueOf(request.getParameter(Protocol.ACTION_PARAM_NAME));

		Map<String, Object> model = new HashMap<String, Object>();

		String baseURI = request.getParameter(Protocol.BASEURI_PARAM_NAME);
		if (baseURI == null) {
			baseURI = "";
		}
		
		try {
			switch (action) {
				case ADD:
					conn.add(request.getInputStream(), baseURI,
							Rio.getParserFormatForMIMEType(request.getContentType()));
					break;
				case DELETE:
					RDFParser parser = Rio.createParser(Rio.getParserFormatForMIMEType(request.getContentType()),
							conn.getValueFactory());
					parser.setRDFHandler(new WildcardRDFRemover(conn));
					parser.getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
					parser.parse(request.getInputStream(), baseURI);
					break;
				case QUERY:
					return processQuery(conn, request, response);
				case GET:
					return getExportStatementsResult(conn, request, response);
				case SIZE:
					return getSize(conn, request, response);
				case UPDATE:
					return getSparqlUpdateResult(conn, request, response);
				case COMMIT:
					conn.commit();
					conn.close();
					ActiveTransactionRegistry.INSTANCE.deregister(getTransactionID(request));
					break;
				case ROLLBACK:
					try {
						conn.rollback();
					}
					finally {
						try {
							if (conn.isOpen()) {
								conn.close();
							}
						}
						finally {
							ActiveTransactionRegistry.INSTANCE.deregister(getTransactionID(request));
						}
					}
					break;
				default:
					logger.warn("transaction action '{}' not recognized", action);
					throw new ClientHTTPException("action not recognized: " + action);
			}

			model.put(SimpleResponseView.SC_KEY, HttpServletResponse.SC_OK);
			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (Exception e) {
			if (e instanceof ClientHTTPException) {
				throw (ClientHTTPException)e;
			}
			else {
				throw new ServerHTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Transaction handling error: " + e.getMessage(), e);
			}
		}
	}

	private ModelAndView getSize(RepositoryConnection conn, HttpServletRequest request,
			HttpServletResponse response)
		throws HTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		Map<String, Object> model = new HashMap<String, Object>();
		final boolean headersOnly = METHOD_HEAD.equals(request.getMethod());

		if (!headersOnly) {
			Repository repository = RepositoryInterceptor.getRepository(request);

			ValueFactory vf = repository.getValueFactory();
			Resource[] contexts = ProtocolUtil.parseContextParam(request, Protocol.CONTEXT_PARAM_NAME, vf);

			long size = -1;

			try {
				size = conn.size(contexts);
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
			}
			model.put(SimpleResponseView.CONTENT_KEY, String.valueOf(size));
		}

		return new ModelAndView(SimpleResponseView.getInstance(), model);

	}

	/**
	 * Get all statements and export them as RDF.
	 * 
	 * @return a model and view for exporting the statements.
	 */
	private ModelAndView getExportStatementsResult(RepositoryConnection conn, HttpServletRequest request,
			HttpServletResponse response)
		throws ClientHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		ValueFactory vf = conn.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		IRI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		boolean useInferencing = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		RDFWriterFactory rdfWriterFactory = ProtocolUtil.getAcceptableService(request, response,
				RDFWriterRegistry.getInstance());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ExportStatementsView.SUBJECT_KEY, subj);
		model.put(ExportStatementsView.PREDICATE_KEY, pred);
		model.put(ExportStatementsView.OBJECT_KEY, obj);
		model.put(ExportStatementsView.CONTEXTS_KEY, contexts);
		model.put(ExportStatementsView.USE_INFERENCING_KEY, Boolean.valueOf(useInferencing));
		model.put(ExportStatementsView.FACTORY_KEY, rdfWriterFactory);
		model.put(ExportStatementsView.HEADERS_ONLY, METHOD_HEAD.equals(request.getMethod()));
		model.put(ExportStatementsView.CONNECTION_KEY, conn);
		return new ModelAndView(ExportStatementsView.getInstance(), model);
	}

	private ModelAndView processQuery(RepositoryConnection conn, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, HTTPException
	{
		String queryStr = request.getParameter(QUERY_PARAM_NAME);

		Query query = getQuery(conn, queryStr, request, response);

		View view;
		Object queryResult;
		FileFormatServiceRegistry<? extends FileFormat, ?> registry;

		try {
			if (query instanceof TupleQuery) {
				TupleQuery tQuery = (TupleQuery)query;

				queryResult = tQuery.evaluate();
				registry = TupleQueryResultWriterRegistry.getInstance();
				view = TupleQueryResultView.getInstance();
			}
			else if (query instanceof GraphQuery) {
				GraphQuery gQuery = (GraphQuery)query;

				queryResult = gQuery.evaluate();
				registry = RDFWriterRegistry.getInstance();
				view = GraphQueryResultView.getInstance();
			}
			else if (query instanceof BooleanQuery) {
				BooleanQuery bQuery = (BooleanQuery)query;

				queryResult = bQuery.evaluate();
				registry = BooleanQueryResultWriterRegistry.getInstance();
				view = BooleanQueryResultView.getInstance();
			}
			else {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Unsupported query type: "
						+ query.getClass().getName());
			}
		}
		catch (QueryInterruptedException e) {
			logger.info("Query interrupted", e);
			throw new ServerHTTPException(SC_SERVICE_UNAVAILABLE, "Query evaluation took too long");
		}
		catch (QueryEvaluationException e) {
			logger.info("Query evaluation error", e);
			if (e.getCause() != null && e.getCause() instanceof HTTPException) {
				// custom signal from the backend, throw as HTTPException
				// directly (see SES-1016).
				throw (HTTPException)e.getCause();
			}
			else {
				throw new ServerHTTPException("Query evaluation error: " + e.getMessage());
			}
		}
		Object factory = ProtocolUtil.getAcceptableService(request, response, registry);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(QueryResultView.FILENAME_HINT_KEY, "query-result");
		model.put(QueryResultView.QUERY_RESULT_KEY, queryResult);
		model.put(QueryResultView.FACTORY_KEY, factory);
		model.put(QueryResultView.HEADERS_ONLY, false); // TODO needed for HEAD
																		// requests.

		return new ModelAndView(view, model);
	}

	private Query getQuery(RepositoryConnection repositoryCon, String queryStr, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientHTTPException
	{
		Query result = null;

		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		logger.debug("query language param = {}", queryLnStr);

		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr);

			if (queryLn == null) {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
			}
		}

		String baseURI = request.getParameter(Protocol.BASEURI_PARAM_NAME);

		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		String timeout = request.getParameter(Protocol.TIMEOUT_PARAM_NAME);
		int maxQueryTime = 0;
		if (timeout != null) {
			try {
				maxQueryTime = Integer.parseInt(timeout);
			}
			catch (NumberFormatException e) {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid timeout value: " + timeout);
			}
		}

		// build a dataset, if specified
		String[] defaultGraphURIs = request.getParameterValues(DEFAULT_GRAPH_PARAM_NAME);
		String[] namedGraphURIs = request.getParameterValues(NAMED_GRAPH_PARAM_NAME);

		SimpleDataset dataset = null;
		if (defaultGraphURIs != null || namedGraphURIs != null) {
			dataset = new SimpleDataset();

			if (defaultGraphURIs != null) {
				for (String defaultGraphURI : defaultGraphURIs) {
					try {
						IRI uri = null;
						if (!"null".equals(defaultGraphURI)) {
							uri = repositoryCon.getValueFactory().createIRI(defaultGraphURI);
						}
						dataset.addDefaultGraph(uri);
					}
					catch (IllegalArgumentException e) {
						throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for default graph: "
								+ defaultGraphURI);
					}
				}
			}

			if (namedGraphURIs != null) {
				for (String namedGraphURI : namedGraphURIs) {
					try {
						IRI uri = null;
						if (!"null".equals(namedGraphURI)) {
							uri = repositoryCon.getValueFactory().createIRI(namedGraphURI);
						}
						dataset.addNamedGraph(uri);
					}
					catch (IllegalArgumentException e) {
						throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for named graph: "
								+ namedGraphURI);
					}
				}
			}
		}

		try {
			result = repositoryCon.prepareQuery(queryLn, queryStr, baseURI);

			result.setIncludeInferred(includeInferred);

			if (maxQueryTime > 0) {
				result.setMaxQueryTime(maxQueryTime);
			}

			if (dataset != null) {
				result.setDataset(dataset);
			}

			// determine if any variable bindings have been set on this query.
			@SuppressWarnings("unchecked")
			Enumeration<String> parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {
				String parameterName = parameterNames.nextElement();

				if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length())
				{
					String bindingName = parameterName.substring(BINDING_PREFIX.length());
					Value bindingValue = ProtocolUtil.parseValueParam(request, parameterName,
							repositoryCon.getValueFactory());
					result.setBinding(bindingName, bindingValue);
				}
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.UNSUPPORTED_QUERY_LANGUAGE, queryLn.getName());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (MalformedQueryException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_QUERY, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR);
		}

		return result;
	}

	private ModelAndView getSparqlUpdateResult(RepositoryConnection conn, HttpServletRequest request,
			HttpServletResponse response)
		throws ServerHTTPException, ClientHTTPException, HTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		String sparqlUpdateString = request.getParameterValues(Protocol.UPDATE_PARAM_NAME)[0];
		logger.debug(sparqlUpdateString);
		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		logger.debug("query language param = {}", queryLnStr);

		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr);

			if (queryLn == null) {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
			}
		}

		String baseURI = request.getParameter(Protocol.BASEURI_PARAM_NAME);

		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		// build a dataset, if specified
		String[] defaultRemoveGraphURIs = request.getParameterValues(REMOVE_GRAPH_PARAM_NAME);
		String[] defaultInsertGraphURIs = request.getParameterValues(INSERT_GRAPH_PARAM_NAME);
		String[] defaultGraphURIs = request.getParameterValues(USING_GRAPH_PARAM_NAME);
		String[] namedGraphURIs = request.getParameterValues(USING_NAMED_GRAPH_PARAM_NAME);

		SimpleDataset dataset = new SimpleDataset();

		if (defaultRemoveGraphURIs != null) {
			for (String graphURI : defaultRemoveGraphURIs) {
				try {
					IRI uri = null;
					if (!"null".equals(graphURI)) {
						uri = conn.getValueFactory().createIRI(graphURI);
					}
					dataset.addDefaultRemoveGraph(uri);
				}
				catch (IllegalArgumentException e) {
					throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for default remove graph: "
							+ graphURI);
				}
			}
		}

		if (defaultInsertGraphURIs != null && defaultInsertGraphURIs.length > 0) {
			String graphURI = defaultInsertGraphURIs[0];
			try {
				IRI uri = null;
				if (!"null".equals(graphURI)) {
					uri = conn.getValueFactory().createIRI(graphURI);
				}
				dataset.setDefaultInsertGraph(uri);
			}
			catch (IllegalArgumentException e) {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for default insert graph: " + graphURI);
			}
		}

		if (defaultGraphURIs != null) {
			for (String defaultGraphURI : defaultGraphURIs) {
				try {
					IRI uri = null;
					if (!"null".equals(defaultGraphURI)) {
						uri = conn.getValueFactory().createIRI(defaultGraphURI);
					}
					dataset.addDefaultGraph(uri);
				}
				catch (IllegalArgumentException e) {
					throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for default graph: "
							+ defaultGraphURI);
				}
			}
		}

		if (namedGraphURIs != null) {
			for (String namedGraphURI : namedGraphURIs) {
				try {
					IRI uri = null;
					if (!"null".equals(namedGraphURI)) {
						uri = conn.getValueFactory().createIRI(namedGraphURI);
					}
					dataset.addNamedGraph(uri);
				}
				catch (IllegalArgumentException e) {
					throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for named graph: " + namedGraphURI);
				}
			}
		}

		try {
			Update update = conn.prepareUpdate(queryLn, sparqlUpdateString, baseURI);

			update.setIncludeInferred(includeInferred);

			if (dataset != null) {
				update.setDataset(dataset);
			}

			// determine if any variable bindings have been set on this update.
			@SuppressWarnings("unchecked")
			Enumeration<String> parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {
				String parameterName = parameterNames.nextElement();

				if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length())
				{
					String bindingName = parameterName.substring(BINDING_PREFIX.length());
					Value bindingValue = ProtocolUtil.parseValueParam(request, parameterName,
							conn.getValueFactory());
					update.setBinding(bindingName, bindingValue);
				}
			}

			update.execute();

			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (UpdateExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof HTTPException) {
				// custom signal from the backend, throw as HTTPException directly
				// (see SES-1016).
				throw (HTTPException)e.getCause();
			}
			else {
				throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
			}
		}
		catch (RepositoryException e) {
			if (e.getCause() != null && e.getCause() instanceof HTTPException) {
				// custom signal from the backend, throw as HTTPException directly
				// (see SES-1016).
				throw (HTTPException)e.getCause();
			}
			else {
				throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
			}
		}
		catch (MalformedQueryException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_QUERY, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
	}

	private static class WildcardRDFRemover extends AbstractRDFHandler {

		private final RepositoryConnection conn;

		public WildcardRDFRemover(RepositoryConnection conn) {
			super();
			this.conn = conn;
		}

		@Override
		public void handleStatement(Statement st)
			throws RDFHandlerException
		{
			Resource subject = SESAME.WILDCARD.equals(st.getSubject()) ? null : st.getSubject();
			IRI predicate = SESAME.WILDCARD.equals(st.getPredicate()) ? null : st.getPredicate();
			Value object = SESAME.WILDCARD.equals(st.getObject()) ? null : st.getObject();
			Resource context = st.getContext();
			try {
				if (context != null) {
					conn.remove(subject, predicate, object, st.getContext());
				}
				else {
					conn.remove(subject, predicate, object);
				}
			}
			catch (RepositoryException e) {
				throw new RDFHandlerException(e);
			}
		}

	}
}
