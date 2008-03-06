/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.statements;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import info.aduna.webapp.views.EmptySuccessView;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.base.ExceptionInfoBase;
import org.openrdf.http.protocol.base.ParseInfoBase;
import org.openrdf.http.protocol.error.ExceptionInfo;
import org.openrdf.http.protocol.error.ParseInfo;
import org.openrdf.http.protocol.transaction.TransactionReader;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.InternalServerException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Handles requests for manipulating the statements in a repository.
 * 
 * @author Herko ter Horst
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
		ModelAndView result = null;

		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String reqMethod = request.getMethod();
		if ("GET".equals(reqMethod)) {
			logger.info("GET statements");
			result = getExportStatementsResult(repository, repositoryCon, request, response);
		}
		else if ("POST".equals(reqMethod)) {
			String contentType = request.getContentType();

			if (Protocol.TXN_MIME_TYPE.equals(contentType)) {
				logger.info("POST transaction to repository");
				result = getTransactionResultResult(repository, repositoryCon, request, response);
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

		return result;
	}

	/**
	 * Get all statements and export them as RDF.
	 * 
	 * @param repository
	 * @param repositoryCon
	 * @param request
	 * @param response
	 * @return a model and view for exporting the statements.
	 * @throws ClientRequestException
	 */
	private ModelAndView getExportStatementsResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws ClientRequestException
	{
		ProtocolUtil.logRequestParameters(request);
		return new ModelAndView(ExportStatementsView.getInstance());
	}

	/**
	 * Process several actions as a transaction.
	 * 
	 * @param repository
	 * @param repositoryCon
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ClientRequestException
	 * @throws InternalServerException
	 */
	private ModelAndView getTransactionResultResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException, InternalServerException
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
		}
		catch (SAXParseException e) {
			ExceptionInfo exInfo = new ExceptionInfoBase(e);
			ParseInfo parseInfo = new ParseInfoBase(null, e.getLineNumber(), e.getColumnNumber(), null);
			exInfo.setParseInfo(parseInfo);
			throw new ClientRequestException(SC_BAD_REQUEST, "Malformed transaction data: " + e.getMessage(), e, exInfo);
		}
		catch (SAXException e) {
			throw new InternalServerException("Failed to parse transaction data: " + e.getMessage(), e);
		}
		catch (IOException e) {
			throw new InternalServerException("Failed to read data: " + e.getMessage());
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository update error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}

	/**
	 * Upload data to the repository.
	 * 
	 * @param repository
	 * @param repositoryCon
	 * @param request
	 * @param response
	 * @param replaceCurrent
	 * @return
	 * @throws IOException
	 * @throws ClientRequestException
	 * @throws InternalServerException
	 */
	private ModelAndView getAddDataResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response, boolean replaceCurrent)
		throws IOException, ClientRequestException, InternalServerException
	{
		ProtocolUtil.logRequestParameters(request);

		String mimeType = request.getContentType();
		if (mimeType.indexOf(";") > 0) {
			// A character encoding part is specified in the content-type field.
			// Get the mime-type substring to enable correct determination of the
			// RDF format.
			mimeType = mimeType.substring(0, mimeType.indexOf(";"));
		}
 
		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new ClientRequestException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported content type: "
					+ request.getContentType());
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
		}
		catch (IOException e) {
			throw new InternalServerException("Failed to read data: " + e.getMessage(), e);
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientRequestException(SC_UNSUPPORTED_MEDIA_TYPE, "No RDF parser available for format "
					+ rdfFormat.getName());
		}
		catch (RDFParseException e) {
			ExceptionInfo exInfo = new ExceptionInfoBase(e);
			ParseInfo parseInfo = new ParseInfoBase(null, e.getLineNumber(), e.getColumnNumber(), null);
			exInfo.setParseInfo(parseInfo);
			throw new ClientRequestException(SC_BAD_REQUEST, "Parse error at line " + e.getLineNumber() + ": "
					+ e.getMessage(), e, exInfo);
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository update error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}

	/**
	 * Delete data from the repository.
	 * 
	 * @param repository
	 * @param repositoryCon
	 * @param request
	 * @param response
	 * @return
	 * @throws ClientRequestException
	 * @throws InternalServerException
	 */
	private ModelAndView getDeleteDataResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws ClientRequestException, InternalServerException
	{
		ProtocolUtil.logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);

		try {
			repositoryCon.remove(subj, pred, obj, contexts);
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository update error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}
}
