/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.parser.QueryParser;

/**
 * @author James Leigh
 */
public class QueryModelSerializer implements QueryParser {

	public static final QueryLanguage LANGUAGE = new QueryLanguage("QueryModelSerializer");

	private static final String UTF_8 = "UTF-8";

	public QueryModel parseQuery(String query, String baseURI)
		throws MalformedQueryException
	{
		try {
			ByteArrayInputStream buf = new ByteArrayInputStream(decode(query));
			ObjectInputStream stream = new ObjectInputStream(buf);
			return (QueryModel)stream.readObject();
		}
		catch (IOException e) {
			throw new MalformedQueryException(e);
		}
		catch (ClassNotFoundException e) {
			throw new MalformedQueryException(e);
		}
	}

	public String writeQueryModel(QueryModel query, String baseURI) {
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(buf);
			stream.writeObject(query);
			stream.close();
			return encode(buf.toByteArray());
		}
		catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private String encode(byte[] binary)
		throws UnsupportedEncodingException
	{
		return new String(Base64.encodeBase64(binary), UTF_8);
	}

	private byte[] decode(String encoded)
		throws UnsupportedEncodingException
	{
		return Base64.decodeBase64(encoded.getBytes(UTF_8));
	}

}
