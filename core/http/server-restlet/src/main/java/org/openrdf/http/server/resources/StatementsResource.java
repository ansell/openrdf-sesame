/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.LIMIT;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OFFSET;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.error.ErrorType.MALFORMED_DATA;
import static org.restlet.data.Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.openrdf.cursor.Cursor;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.transaction.TransactionReader;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.http.server.ErrorInfoException;
import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.http.server.representations.ModelResultRepresentation;
import org.openrdf.http.server.resources.helpers.StatementResultResource;
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
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class StatementsResource extends StatementResultResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Resource subj;

	private final URI pred;

	private final Value obj;

	private final Resource[] contexts;

	private final boolean includeInferred;

	private final int offset;

	private final int limit;

	public StatementsResource(Context context, Request request, Response response)
		throws ResourceException
	{
		super(context, request, response);

		// Allow POST, PUT and DELETE
		this.setModifiable(true);

		Form params = getQuery();
		ValueFactory vf = getConnection().getValueFactory();

		subj = ServerUtil.parseResourceParam(params, SUBJECT_PARAM_NAME, vf);
		pred = ServerUtil.parseURIParam(params, PREDICATE_PARAM_NAME, vf);
		obj = ServerUtil.parseValueParam(params, OBJECT_PARAM_NAME, vf);
		contexts = ServerUtil.parseContextParam(params, CONTEXT_PARAM_NAME, vf);
		includeInferred = ServerUtil.parseBooleanParam(params, INCLUDE_INFERRED_PARAM_NAME, true);
		offset = ServerUtil.parseIntegerParam(params, OFFSET, 0);
		limit = ServerUtil.parseIntegerParam(params, LIMIT, 0);
	}

	protected final Representation getRepresentation(RDFWriterFactory factory, MediaType mediaType)
		throws ResourceException
	{
		try {
			ModelResult modelResult = getConnection().match(subj, pred, obj, includeInferred, contexts);

			if (offset > 0 || limit > -1) {
				Cursor<Statement> cursor = modelResult;

				if (offset > 0) {
					cursor = new OffsetCursor<Statement>(cursor, offset);
				}
				if (limit > -1) {
					cursor = new LimitCursor<Statement>(cursor, limit);
				}
				modelResult = new ModelResultImpl(cursor);
			}

			ModelResultRepresentation result = new ModelResultRepresentation(modelResult, factory, mediaType);
			result.setTrimNamespaces(true);
			return result;
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "statements";
	}

	@Override
	public void acceptRepresentation(Representation entity)
		throws ResourceException
	{
		String mimeType = entity.getMediaType().getName();

		if (Protocol.TXN_MIME_TYPE.equals(mimeType)) {
			execute(entity);
		}
		else {
			add(entity, false);
		}
	}

	@Override
	public void storeRepresentation(Representation entity)
		throws ResourceException
	{
		add(entity, true);
	}

	@Override
	public void removeRepresentations()
		throws ResourceException
	{
		try {
			getConnection().removeMatch(subj, pred, obj, contexts);
		}
		catch (StoreException e) {
			throw new ResourceException(SERVER_ERROR_INTERNAL, "Repository update error", e);
		}
	}

	/**
	 * Process a RDF transaction request
	 */
	private void execute(Representation entity)
		throws ResourceException
	{
		try {
			Reader in = entity.getReader();
			logger.debug("Processing transaction...");

			TransactionReader reader = new TransactionReader();
			Iterable<? extends TransactionOperation> txn = reader.parse(in);

			RepositoryConnection repositoryCon = getConnection();

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

				logger.debug("Transaction processed ");
			}
			finally {
				if (autoCommit && !repositoryCon.isAutoCommit()) {
					// restore auto-commit by rolling back
					logger.error("Rolling back transaction");
					repositoryCon.rollback();
				}
			}
		}
		catch (SAXParseException e) {
			throw new ErrorInfoException(MALFORMED_DATA, e.getMessage());
		}
		catch (SAXException e) {
			throw new ResourceException(e);
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	private void add(Representation entity, boolean replaceCurrent)
		throws ResourceException
	{
		String mimeType = entity.getMediaType().getName();

		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: "
					+ mimeType);
		}

		RepositoryConnection repositoryCon = getConnection();
		ValueFactory vf = repositoryCon.getValueFactory();

		Form params = getQuery();
		Resource[] contexts = ServerUtil.parseContextParam(params, CONTEXT_PARAM_NAME, vf);
		URI baseURI = ServerUtil.parseURIParam(params, BASEURI_PARAM_NAME, vf);

		if (baseURI == null) {
			baseURI = vf.createURI("foo:bar");
			logger.info("no base URI specified, using dummy '{}'", baseURI);
		}

		try {
			InputStream in = entity.getStream();

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
		catch (UnsupportedRDFormatException e) {
			throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
					"No RDF parser available for format " + rdfFormat.getName());
		}
		catch (RDFParseException e) {
			throw new ErrorInfoException(MALFORMED_DATA, e.getMessage());
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}
}
