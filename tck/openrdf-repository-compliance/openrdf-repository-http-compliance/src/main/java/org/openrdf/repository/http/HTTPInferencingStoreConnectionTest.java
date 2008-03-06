/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.server.TestServer;
import org.openrdf.repository.InferencingRepositoryConnectionTest;
import org.openrdf.repository.Repository;

public class HTTPInferencingStoreConnectionTest extends InferencingRepositoryConnectionTest {

	private TestServer server;

	public HTTPInferencingStoreConnectionTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		server = new TestServer();
		server.start();
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		server.stop();
	}

	protected Repository createRepository() {
		return new HTTPRepository(TestServer.INFERENCE_REPOSITORY_URL);
	}

}
