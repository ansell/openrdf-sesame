/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.statements;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import info.aduna.webapp.util.HttpServerUtil;
import info.aduna.webapp.views.EmptySuccessView;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.protocol.transaction.TransactionReader;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Handles requests for manipulating the statements in a repository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class StatementsController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public StatementsController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_POST, "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ModelAndView result;

		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String reqMethod = request.getMethod();

		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET statements");
			result = getExportStatementsResult(repository, repositoryCon, request, response);
		}
		else if (METHOD_POST.equals(reqMethod)) {
			String mimeType = HttpServerUtil.getMIMEType(request.getContentType());

			if (Protocol.TXN_MIME_TYPE.equals(mimeType)) {
				logger.info("POST transaction to repository");
				result = getTransactionResultResult(repository, repositoryCon, request, response);
			}
			else if (request.getParameterMap().containsKey(Protocol.UPDATE_PARAM_NAME)) {
				logger.info("POST SPARQL update request to repository");
				result = getSparqlUpdateResult(repository, repositoryCon, request, response);
			}
			else {
				logger.info("POST data to repository");
				result = getAddDataResult(repository, repositoryCon, request, response, false);
			}
		}
		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT data in repository");
			result = getAddDataResult(repository, repositoryCon, request, response, true);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE data from repository");
			result = getDeleteDataResult(repository, repositoryCon, request, response);
		}
		else {
			throw new ClientHTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed: "
					+ reqMethod);
		}

		return result;
	}

	/**
	 * @param repository
	 * @param repositoryCon
	 * @param request
	 * @param response
	 * @return
	 * @throws ServerHTTPException
	 * @throws ClientHTTPException
	 */
	private ModelAndView getSparqlUpdateResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response) throws ServerHTTPException, ClientHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		String sparqlUpdateString = request.getParameterValues(Protocol.UPDATE_PARAM_NAME)[0];



		try {
			
			Update update = repositoryCon.prepareUpdate(QueryLanguage.SPARQL, sparqlUpdateString);
			
			// determine if any variable bindings have been set on this update.
			@SuppressWarnings("unchecked")
			Enumeration<String> parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {
				String parameterName = parameterNames.nextElement();

				if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length())
				{
					String bindingName = parameterName.substring(BINDING_PREFIX.length());
					Value bindingValue = ProtocolUtil.parseValueParam(request, parameterName,
							repository.getValueFactory());
					update.setBinding(bindingName, bindingValue);
				}
			}
			
			update.execute();
			
			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (UpdateExecutionException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_QUERY, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
	}

	/**
	 * Get all statements and export them as RDF.
	 * 
	 * @return a model and view for exporting the statements.
	 */
	private ModelAndView getExportStatementsResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws ClientHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
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

		return new ModelAndView(ExportStatementsView.getInstance(), model);
	}

	/**
	 * Process several actions as a transaction.
	 */
	private ModelAndView getTransactionResultResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		InputStream in = request.getInputStream();
		try {
			logger.debug("Processing transaction...");

			TransactionReader reader = new TransactionReader();
			Iterable<? extends TransactionOperation> txn = reader.parse(in);

			boolean wasAutoCommit = repositoryCon.isAutoCommit();
			repositoryCon.setAutoCommit(false);

			for (TransactionOperation op : txn) {
				op.execute(repositoryCon);
			}

			repositoryCon.setAutoCommit(wasAutoCommit);

			logger.debug("Transaction processed ");

			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (SAXParseException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (SAXException e) {
			throw new ServerHTTPException("Failed to parse transaction data: " + e.getMessage(), e);
		}
		catch (IOException e) {
			throw new ServerHTTPException("Failed to read data: " + e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
	}

	/**
	 * Upload data to the repository.
	 */
	private ModelAndView getAddDataResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response, boolean replaceCurrent)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());

		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: " + mimeType);
		}

		ValueFactory vf = repository.getValueFactory();

		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		URI baseURI = ProtocolUtil.parseURIParam(request, BASEURI_PARAM_NAME, vf);

		if (baseURI == null) {
			baseURI = vf.createURI("foo:bar");
			logger.info("no base URI specified, using dummy '{}'", baseURI);
		}

		InputStream in = request.getInputStream();
		try {
			boolean wasAutoCommit = repositoryCon.isAutoCommit();
			repositoryCon.setAutoCommit(false);

			if (replaceCurrent) {
				repositoryCon.clear(contexts);
			}
			repositoryCon.add(in, baseURI.toString(), rdfFormat, contexts);

			repositoryCon.setAutoCommit(wasAutoCommit);

			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "No RDF parser available for format "
					+ rdfFormat.getName());
		}
		catch (RDFParseException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (IOException e) {
			throw new ServerHTTPException("Failed to read data: " + e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
	}

	/**
	 * Delete data from the repository.
	 */
	private ModelAndView getDeleteDataResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws ClientHTTPException, ServerHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);

		try {
			repositoryCon.remove(subj, pred, obj, contexts);

			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
	}
}
