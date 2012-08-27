/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.query.parser.sparql.SPARQLUpdateTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.http.HTTPMemServer;
import org.openrdf.repository.http.HTTPRepository;

/**
 * @author jeen
 */
public class SPARQLRepositorySparqlUpdateTest extends SPARQLUpdateTest {

	private HTTPMemServer server;

	@Override
	public void setUp()
		throws Exception
	{
		server = new HTTPMemServer();
		
		try {
			server.start();
			super.setUp();
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}
	}

	@Override
	public void tearDown()
		throws Exception
	{
		super.tearDown();
		server.stop();
	}

	@Override
	protected Repository newRepository()
		throws Exception
	{
		return new SPARQLRepository(HTTPMemServer.REPOSITORY_URL, HTTPMemServer.REPOSITORY_URL + "/statements");
	}

	@Ignore
	@Test
	@Override
	public void testAutoCommitHandling() 
	{
		// transaction isolation is not supported for HTTP connections. disabling test.
		System.err.println("temporarily disabled testAutoCommitHandling() for HTTPRepository");
	}
}
