/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import static java.lang.String.valueOf;
import static org.openrdf.http.protocol.Protocol.AMOUNT;
import static org.openrdf.http.protocol.Protocol.BNODE;
import static org.openrdf.http.protocol.Protocol.NODE_ID;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.model.BNode;
import org.openrdf.query.BindingSet;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class BNodeClient {

	private final HTTPConnectionPool pool;

	public BNodeClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public TupleResult post(int amount)
		throws StoreException, QueryResultParseException, NoCompatibleMediaType
	{
		HTTPConnection con = pool.post();

		NameValuePair pair = new NameValuePair(AMOUNT, valueOf(amount));
		con.sendQueryString(Arrays.asList(pair));

		try {
			con.acceptTupleQueryResult();
			execute(con);
			return con.getTupleQueryResult();
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}

	public BNode post(String nodeID)
		throws StoreException, QueryResultParseException, NoCompatibleMediaType
	{
		HTTPConnection con = pool.post();

		NameValuePair pair = new NameValuePair(NODE_ID, nodeID);
		con.sendQueryString(Arrays.asList(pair));

		try {
			con.acceptTupleQueryResult();
			execute(con);
			TupleResult result = con.getTupleQueryResult();
			try {
				if (result.hasNext()) {
					BindingSet bindings = result.next();
					return (BNode)bindings.getValue(BNODE);
				}
				return null;
			}
			finally {
				result.close();
			}
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}

	private void execute(HTTPConnection method)
		throws IOException, StoreException
	{
		try {
			method.execute();
		}
		catch (UnsupportedQueryLanguage e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (UnsupportedFileFormat e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (Unauthorized e) {
			throw new UnauthorizedException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}
}
