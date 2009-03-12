/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.REPO_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.ServerHTTPException;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.transaction.TransactionReader;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.helpers.RDFRequest;
import org.openrdf.http.server.interceptors.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.cursors.LimitCursor;
import org.openrdf.query.algebra.evaluation.cursors.OffsetCursor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ModelResult;
import org.openrdf.result.impl.ModelResultImpl;
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

	@ModelAttribute
	@RequestMapping(method = HEAD, value = { REPO_PATH + "/statements", CONN_PATH + "/statements" })
	public ModelResult head(HttpServletRequest request)
		throws StoreException, ClientHTTPException, IOException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		// check if the syntax of the request
		RDFRequest req = new RDFRequest(vf, request);
		req.getSubject();
		req.getPredicate();
		req.getObject();
		req.getContext();
		req.isIncludeInferred();
		req.getOffset();
		req.getLimit();

		Cursor<Statement> nothing = EmptyCursor.getInstance();
		return new ModelResultImpl(nothing);
	}

	/**
	 * Get all statements and export them as RDF.
	 * 
	 * @return a model and view for exporting the statements.
	 */
	@ModelAttribute
	@RequestMapping(method = GET, value = { REPO_PATH + "/statements", CONN_PATH + "/statements" })
	public ModelResult get(HttpServletRequest request)
		throws StoreException, ClientHTTPException, IOException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		RDFRequest req = new RDFRequest(vf, request);
		Resource subj = req.getSubject();
		URI pred = req.getPredicate();
		Value obj = req.getObject();
		Resource[] contexts = req.getContext();
		boolean useInferencing = req.isIncludeInferred();
		int offset = req.getOffset();
		int limit = req.getLimit();

		Cursor<Statement> cursor = repositoryCon.match(subj, pred, obj, useInferencing, contexts);
		if (offset > 0) {
			cursor = new OffsetCursor<Statement>(cursor, offset);
		}
		if (limit > -1) {
			cursor = new LimitCursor<Statement>(cursor, limit);
		}
		return new ModelResultImpl(cursor);
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = { REPO_PATH + "/statements", CONN_PATH + "/statements" })
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
	@RequestMapping(method = PUT, value = { REPO_PATH + "/statements", CONN_PATH + "/statements" })
	public void put(HttpServletRequest request)
		throws IOException, ClientHTTPException, StoreException, RDFParseException
	{
		add(request, true);
	}

	/**
	 * Delete data from the repository.
	 */
	@ModelAttribute
	@RequestMapping(method = DELETE, value = { REPO_PATH + "/statements", CONN_PATH + "/statements" })
	public void delete(HttpServletRequest request)
		throws ClientHTTPException, StoreException, IOException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		RDFRequest req = new RDFRequest(vf, request);
		Resource subj = req.getSubject();
		URI pred = req.getPredicate();
		Value obj = req.getObject();
		Resource[] contexts = req.getContext();

		repositoryCon.removeMatch(subj, pred, obj, contexts);
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

		boolean autoCommit = repositoryCon.isAutoCommit();
		if (autoCommit) {
			repositoryCon.begin();
		}

		try {
			for (TransactionOperation op : txn) {
				op.execute(repositoryCon);
			}

			if (autoCommit) {
				repositoryCon.commit();
			}
		}
		finally {
			if (autoCommit && !repositoryCon.isAutoCommit()) {
				// restore auto-commit by rolling back
				repositoryCon.rollback();
			}
		}

		logger.debug("Transaction processed ");
	}

	private void add(HttpServletRequest request, boolean replaceCurrent)
		throws IOException, ClientHTTPException, StoreException, RDFParseException
	{
		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());

		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
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

		boolean autoCommit = repositoryCon.isAutoCommit();
		if (autoCommit) {
			repositoryCon.begin();
		}

		try {
			if (replaceCurrent) {
				repositoryCon.clear(contexts);
			}
			repositoryCon.add(in, baseURI.toString(), rdfFormat, contexts);

			if (autoCommit) {
				repositoryCon.commit();
			}
		}
		finally {
			if (autoCommit && !repositoryCon.isAutoCommit()) {
				// restore auto-commit by rolling back'
				repositoryCon.rollback();
			}
		}
	}
}
