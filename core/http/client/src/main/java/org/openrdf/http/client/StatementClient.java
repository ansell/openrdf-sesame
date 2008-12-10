/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;

import info.aduna.io.IOUtil;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.FutureGraphQueryResult;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.MalformedData;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Cursor;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.impl.EmptyCursor;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class StatementClient {

	private HTTPConnectionPool statements;

	private Integer limit;

	private String match;

	private String eTag;

	private int maxAge;

	public StatementClient(HTTPConnectionPool statements) {
		this.statements = statements;
	}

	/*---------------------------*
	 * Get/add/remove statements *
	 *---------------------------*/

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public String getETag() {
		return eTag;
	}

	public void ifNoneMatch(String eTag) {
		match = eTag;
	}

	public GraphQueryResult get(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		final HTTPConnection method = statements.get();
		if (match != null) {
			method.ifNoneMatch(match);
		}
		method.sendQueryString(getParams(subj, pred, obj, includeInferred, contexts));
		Callable<GraphQueryResult> task = new Callable<GraphQueryResult>() {

			public GraphQueryResult call()
				throws StoreException
			{
				try {
					method.acceptRDF(true);
					if (execute(method) && !method.isNotModified()) {
						return method.getGraphQueryResult();
					}
					else if (method.isNotModified()) {
						return null;
					} else {
						Map<String, String> ns = Collections.emptyMap();
						Cursor<Statement> cursor = EmptyCursor.emptyCursor();
						return new GraphQueryResultImpl(ns, cursor);
					}
				}
				catch (NoCompatibleMediaType e) {
					throw new StoreException(e);
				}
				catch (IOException e) {
					throw new StoreException(e);
				}
				catch (RDFParseException e) {
					throw new StoreException(e);
				}
			}
		};
		if (match == null)
			return new FutureGraphQueryResult(statements.submitTask(task));
		try {
			return task.call();
		}
		catch (StoreException e) {
			throw e;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new StoreException(e);
		}
	}

	public void get(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws RDFHandlerException, StoreException
	{
		HTTPConnection method = statements.get();
		if (match != null) {
			method.ifNoneMatch(match);
		}

		try {
			method.acceptRDF(true);
			method.sendQueryString(getParams(subj, pred, obj, includeInferred, contexts));
			if (execute(method) && method.isNotModified()) {
				method.readRDF(handler);
			}
		}
		catch (NoCompatibleMediaType e) {
			throw new StoreException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (RDFParseException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void post(final Iterable<? extends TransactionOperation> txn)
		throws StoreException
	{
		HTTPConnection method = statements.post();

		// Create a RequestEntity for the transaction data
		method.sendEntity(new RequestEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public String getContentType() {
				return Protocol.TXN_MIME_TYPE;
			}

			public boolean isRepeatable() {
				return true;
			}

			public void writeRequest(OutputStream out)
				throws IOException
			{
				TransactionWriter txnWriter = new TransactionWriter();
				txnWriter.serialize(txn, out);
			}
		});

		try {
			if (!execute(method)) {
				throw new StoreException("Not Found");
			}
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public void upload(final Reader contents, String baseURI, final RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws RDFParseException, StoreException
	{
		final Charset charset = dataFormat.hasCharset() ? dataFormat.getCharset() : Charset.forName("UTF-8");

		RequestEntity entity = new RequestEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public String getContentType() {
				return dataFormat.getDefaultMIMEType() + "; charset=" + charset.name();
			}

			public boolean isRepeatable() {
				return false;
			}

			public void writeRequest(OutputStream out)
				throws IOException
			{
				OutputStreamWriter writer = new OutputStreamWriter(out, charset);
				IOUtil.transfer(contents, writer);
				writer.flush();
			}
		};

		upload(entity, baseURI, overwrite, contexts);
	}

	public void upload(InputStream contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws RDFParseException, StoreException
	{
		// Set Content-Length to -1 as we don't know it and we also don't want to
		// cache
		RequestEntity entity = new InputStreamRequestEntity(contents, -1, dataFormat.getDefaultMIMEType());
		upload(entity, baseURI, overwrite, contexts);
	}

	boolean execute(HTTPConnection method)
		throws IOException, StoreException
	{
		try {
			method.execute();
			return true;
		}
		catch (NotFound e) {
			return false;
		}
		catch (UnsupportedQueryLanguage e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (UnsupportedFileFormat e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (UnsupportedMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (Unauthorized e) {
			throw new StoreException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}

	private void upload(RequestEntity reqEntity, String baseURI, boolean overwrite, Resource... contexts)
		throws RDFParseException, StoreException
	{
		// Select appropriate HTTP method
		HTTPConnection method;
		if (overwrite) {
			method = statements.put();
		}
		else {
			method = statements.post();
		}

		// Set relevant query parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		for (String encodedContext : Protocol.encodeContexts(contexts)) {
			params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		if (baseURI != null && baseURI.trim().length() != 0) {
			String encodedBaseURI = Protocol.encodeValue(new URIImpl(baseURI));
			params.add(new NameValuePair(Protocol.BASEURI_PARAM_NAME, encodedBaseURI));
		}
		method.sendQueryString(params);

		// Set payload
		method.sendEntity(reqEntity);

		// Send request
		try {
			executeUpload(method);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	private List<NameValuePair> getParams(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		if (subj != null) {
			params.add(new NameValuePair(Protocol.SUBJECT_PARAM_NAME, Protocol.encodeValue(subj)));
		}
		if (pred != null) {
			params.add(new NameValuePair(Protocol.PREDICATE_PARAM_NAME, Protocol.encodeValue(pred)));
		}
		if (obj != null) {
			params.add(new NameValuePair(Protocol.OBJECT_PARAM_NAME, Protocol.encodeValue(obj)));
		}
		for (String encodedContext : Protocol.encodeContexts(contexts)) {
			params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		params.add(new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred)));
		if (limit != null) {
			params.add(new NameValuePair(Protocol.LIMIT, String.valueOf(limit)));
		}
		return params;
	}

	private void executeUpload(HTTPConnection method)
		throws IOException, StoreException, RDFParseException
	{
		try {
			method.execute();
			reset();
			eTag = method.readETag();
			maxAge = method.readMaxAge();
		}
		catch (MalformedData e) {
			throw new RDFParseException(e);
		}
		catch (UnsupportedQueryLanguage e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (UnsupportedFileFormat e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (UnsupportedMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (Unauthorized e) {
			throw new UnauthorizedException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}

	private void reset() {
		match = null;
	}

}
