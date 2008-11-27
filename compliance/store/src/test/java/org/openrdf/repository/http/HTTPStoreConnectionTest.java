/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;

public class HTTPStoreConnectionTest extends RepositoryConnectionTest {

	private HTTPMemServer server;

	public HTTPStoreConnectionTest(String name) {
		super(name);
	}

	@Override
	public void setUp()
		throws Exception
	{
		server = new HTTPMemServer();
		server.setMaxCacheAge(10);
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
	protected Repository createRepository() {
		return new HTTPRepository(HTTPMemServer.REPOSITORY_URL);
	}

	@Override
	public void testTransactionIsolation()
		throws Exception
	{
		System.err.println("temporarily disabled testTransactionIsolation() for HTTPRepository");
	}

	@Override
	public void testAutoCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testAutoCommit() for HTTPRepository");
	}

	@Override
	public void testRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testRollback() for HTTPRepository");
	}

	@Override
	public void testEmptyCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testEmptyCommit() for HTTPRepository");
	}

	@Override
	public void testEmptyRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testEmptyRollback() for HTTPRepository");
	}

	@Override
	public void testSizeCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testSizeCommit() for HTTPRepository");
	}

	@Override
	public void testSizeRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testSizeRollback() for HTTPRepository");
	}

	@Override
	public void testGetContextIDs()
		throws Exception
	{
		System.err.println("temporarily disabled testGetContextIDs() for HTTPRepository");
	}
}
