/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlDatasetTest;

public class HTTPSparqlDatasetTest extends SparqlDatasetTest {

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

	protected Repository newRepository() {
		return new HTTPRepository(HTTPMemServer.REPOSITORY_URL);
	}
}
