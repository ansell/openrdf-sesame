/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.http;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;

import info.aduna.io.Java7FileUtil;

import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;

public class HTTPStoreConnectionTest extends RepositoryConnectionTest {

	private HTTPMemServer server;

	private Path testDir;

	@Override
	public void setUp()
		throws Exception
	{
		testDir = tempDir.newFolder("sesame-http-store-connection-datadir").toPath();
		server = new HTTPMemServer(testDir);

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
		try {
			super.tearDown();
		}
		finally {
			try {
				server.stop();
			}
			finally {
				Java7FileUtil.deleteDir(testDir);
			}
		}
	}

	@Override
	protected Repository createRepository() {
		return new HTTPRepository(server.getRepositoryUrl());
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testTransactionIsolation()
		throws Exception
	{
		System.err.println("temporarily disabled testTransactionIsolation() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testAutoCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testAutoCommit() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testRollback() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testEmptyCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testEmptyCommit() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testEmptyRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testEmptyRollback() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testSizeCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testSizeCommit() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testSizeRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testSizeRollback() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testGetContextIDs()
		throws Exception
	{
		System.err.println("temporarily disabled testGetContextIDs() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testOrderByQueriesAreInterruptable() {
		System.err.println("temporarily disabled testOrderByQueriesAreInterruptable() for HTTPRepository");
	}

	@Test
	public void testUpdateExecution()
		throws Exception
	{

		URI foobar = vf.createURI("foo:bar");

		String sparql = "INSERT DATA { <foo:bar> <foo:bar> <foo:bar> . } ";

		Update update = testCon.prepareUpdate(QueryLanguage.SPARQL, sparql);

		update.execute();

		assertTrue(testCon.hasStatement(foobar, foobar, foobar, true));

		testCon.clear();

		assertFalse(testCon.hasStatement(foobar, foobar, foobar, true));

		testCon.begin();
		update.execute();

		// NOTE this is only correct because HTTPconnection does not implement
		// true transaction isolation.
		assertFalse(testCon.hasStatement(foobar, foobar, foobar, true));

		testCon.commit();

		assertTrue(testCon.hasStatement(foobar, foobar, foobar, true));

	}

}
