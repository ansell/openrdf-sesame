/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.xml.sax.SAXException;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.transaction.TransactionReader;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.http.server.exceptions.ClientHTTPException;
import org.openrdf.http.server.exceptions.ServerHTTPException;
import org.openrdf.http.server.helpers.HttpServerUtil;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.store.StoreException;

/**
 * Handles requests for manipulating the statements in a repository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
@Controller
public class StatementController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Get all statements and export them as RDF.
	 * 
	 * @return a model and view for exporting the statements.
	 */
	@ModelAttribute
	@RequestMapping(value = "/repositories/*/statements", method = RequestMethod.GET)
	public RepositoryResult<Statement> export(HttpServletRequest request)
		throws StoreException, ClientHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		boolean useInferencing = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		return repositoryCon.getStatements(subj, pred, obj, useInferencing, contexts);
	}

	@ModelAttribute
	@RequestMapping(value = "/repositories/*/statements", method = RequestMethod.POST)
	public void post(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());

		if (Protocol.TXN_MIME_TYPE.equals(mimeType)) {
			logger.info("POST transaction to repository");
			execute(request);
		}
		else {
			logger.info("POST data to repository");
			add(request, false);
		}
	}

	/**
	 * Upload data to the repository.
	 */
	@ModelAttribute
	@RequestMapping(value = "/repositories/*/statements", method = RequestMethod.PUT)
	public void put(HttpServletRequest request)
		throws IOException, ClientHTTPException, StoreException, RDFParseException
	{
			add(request, true);
	}

	/**
	 * Process several actions as a transaction.
	 */
	private void execute(HttpServletRequest request)
		throws IOException, ClientHTTPException, ServerHTTPException, SAXException, StoreException
	{
		InputStream in = request.getInputStream();
		logger.debug("Processing transaction...");

		TransactionReader reader = new TransactionReader();
		Iterable<? extends TransactionOperation> txn = reader.parse(in);

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		boolean wasAutoCommit = repositoryCon.isAutoCommit();
		repositoryCon.setAutoCommit(false);

		for (TransactionOperation op : txn) {
			op.execute(repositoryCon);
		}

		repositoryCon.setAutoCommit(wasAutoCommit);

		logger.debug("Transaction processed ");
	}

	private void add(HttpServletRequest request, boolean replaceCurrent)
		throws IOException, ClientHTTPException, StoreException, RDFParseException
	{
		ProtocolUtil.logRequestParameters(request);

		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());

		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: " + mimeType);
		}

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		URI baseURI = ProtocolUtil.parseURIParam(request, BASEURI_PARAM_NAME, vf);

		if (baseURI == null) {
			baseURI = vf.createURI("foo:bar");
			logger.info("no base URI specified, using dummy '{}'", baseURI);
		}

		InputStream in = request.getInputStream();
		boolean wasAutoCommit = repositoryCon.isAutoCommit();
		repositoryCon.setAutoCommit(false);

		if (replaceCurrent) {
			repositoryCon.clear(contexts);
		}
		repositoryCon.add(in, baseURI.toString(), rdfFormat, contexts);

		repositoryCon.setAutoCommit(wasAutoCommit);
	}

	/**
	 * Delete data from the repository.
	 */
	@ModelAttribute
	@RequestMapping(value = "/repositories/*/statements", method = RequestMethod.DELETE)
	public void remove(HttpServletRequest request)
		throws ClientHTTPException, StoreException
	{
		ProtocolUtil.logRequestParameters(request);

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);

		repositoryCon.removeMatch(subj, pred, obj, contexts);
	}
}
