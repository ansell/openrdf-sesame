/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.query.parser.sparql.SPARQLUpdateTest;
import org.openrdf.repository.Repository;

/**
 * @author jeen
 */
public class HTTPSparqlUpdateTest extends SPARQLUpdateTest {

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
		return new HTTPRepository(HTTPMemServer.REPOSITORY_URL);
	}

}
