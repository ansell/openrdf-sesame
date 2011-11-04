/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import junit.framework.TestCase;

import info.aduna.io.IOUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.rio.RDFFormat;

public class ProtocolTest extends TestCase {

	private TestServer server;

	@Override
	protected void setUp()
		throws Exception
	{
		server = new TestServer();
		server.start();
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		server.stop();
	}

	/**
	 * Tests the server's methods for updating all data in a repository.
	 */
	public void testRepository_PUT()
		throws Exception
	{
		putFile(Protocol.getStatementsLocation(TestServer.REPOSITORY_URL), "/testcases/default-graph-1.ttl");
	}

	/**
	 * Tests the server's methods for deleting all data in a repository.
	 */
	public void testRepository_DELETE()
		throws Exception
	{
		delete(Protocol.getStatementsLocation(TestServer.REPOSITORY_URL));
	}

	/**
	 * Tests the server's methods for updating the data in the default context of
	 * a repository.
	 */
	public void testNullContext_PUT()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		location += "?" + Protocol.CONTEXT_PARAM_NAME + "=" + Protocol.NULL_PARAM_VALUE;
		putFile(location, "/testcases/default-graph-1.ttl");
	}

	/**
	 * Tests the server's methods for deleting the data from the default context
	 * of a repository.
	 */
	public void testNullContext_DELETE()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		location += "?" + Protocol.CONTEXT_PARAM_NAME + "=" + Protocol.NULL_PARAM_VALUE;
		delete(location);
	}

	/**
	 * Tests the server's methods for updating the data in a named context of a
	 * repository.
	 */
	public void testNamedContext_PUT()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		String encContext = Protocol.encodeValue(new URIImpl("urn:x-local:graph1"));
		location += Protocol.CONTEXT_PARAM_NAME + "=" + encContext;
		putFile(location, "/testcases/named-graph-1.ttl");
	}

	/**
	 * Tests the server's methods for deleting the data from a named context of a
	 * repository.
	 */
	public void testNamedContext_DELETE()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		String encContext = Protocol.encodeValue(new URIImpl("urn:x-local:graph1"));
		location += Protocol.CONTEXT_PARAM_NAME + "=" + encContext;
		delete(location);
	}

	/**
	 * Tests the server's methods for quering a repository using GET requests to
	 * send SeRQL-select queries.
	 */
	public void testSeRQLselect()
		throws Exception
	{
		TupleQueryResult queryResult = evaluate(TestServer.REPOSITORY_URL, "select * from {X} P {Y}",
				QueryLanguage.SERQL);
		QueryResultIO.write(queryResult, TupleQueryResultFormat.SPARQL, System.out);
	}

	private void putFile(String location, String file)
		throws Exception
	{
		System.out.println("Put file to " + location);

		URL url = new URL(location);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);

		RDFFormat dataFormat = RDFFormat.forFileName(file, RDFFormat.RDFXML);
		conn.setRequestProperty("Content-Type", dataFormat.getDefaultMIMEType());

		InputStream dataStream = ProtocolTest.class.getResourceAsStream(file);
		try {
			OutputStream connOut = conn.getOutputStream();

			try {
				IOUtil.transfer(dataStream, connOut);
			}
			finally {
				connOut.close();
			}
		}
		finally {
			dataStream.close();
		}

		conn.connect();

		int responseCode = conn.getResponseCode();

		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
					+ responseCode + ")";
			fail(response);
		}
	}

	private void delete(String location)
		throws Exception
	{
		URL url = new URL(location);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("DELETE");

		conn.connect();

		int responseCode = conn.getResponseCode();

		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
					+ responseCode + ")";
			fail(response);
		}
	}

	private TupleQueryResult evaluate(String location, String query, QueryLanguage queryLn)
		throws Exception
	{
		location += "?query=" + URLEncoder.encode(query, "UTF-8") + "&queryLn=" + queryLn.getName();

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		// Request SPARQL-XML formatted results:
		conn.setRequestProperty("Accept", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// Process query results
				return QueryResultIO.parse(conn.getInputStream(), TupleQueryResultFormat.SPARQL);
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
				throw new RuntimeException(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}
}
