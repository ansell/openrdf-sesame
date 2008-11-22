/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;

import info.aduna.io.IOUtil;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class StatementClient {

	private HTTPConnectionPool statements;

	public StatementClient(HTTPConnectionPool statements) {
		this.statements = statements;
	}

	/*---------------------------*
	 * Get/add/remove statements *
	 *---------------------------*/

	public void get(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws RDFHandlerException, StoreException
	{
		HTTPConnection method = statements.get();

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

		method.sendQueryString(params);

		try {
			method.acceptRDF(true);
			method.executeQuery();
			method.readRDF(handler);
		}
		catch (MalformedQueryException e) {
			throw new StoreException(e.getMessage(), e);
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
			method.execute();
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

	protected void upload(RequestEntity reqEntity, String baseURI, boolean overwrite, Resource... contexts)
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
			method.executeUpload();
		}
		finally {
			method.release();
		}
	}

}
