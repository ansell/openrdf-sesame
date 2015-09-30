/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.http;

import static org.junit.Assert.fail;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.repository.RDFSchemaRepositoryConnectionTest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnectionTest;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class RDFSchemaHTTPRepositoryConnectionTest extends RDFSchemaRepositoryConnectionTest {

	private static HTTPMemServer server;

	public RDFSchemaHTTPRepositoryConnectionTest(IsolationLevel level) {
		super(level);
	}

	@BeforeClass
	public static void startServer()
		throws Exception
	{
		server = new HTTPMemServer();
		try {
			server.start();
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}
	}

	@AfterClass
	public static void stopServer()
		throws Exception
	{
		server.stop();
	}

	@Override
	protected Repository createRepository() {
		return new HTTPRepository(HTTPMemServer.INFERENCE_REPOSITORY_URL);
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testTransactionIsolationForRead()
		throws Exception
	{
		System.err.println("temporarily disabled testTransactionIsolationForRead() for HTTPRepository");
	}
	
	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testTransactionIsolationForReadWithDeleteOperation()
	throws Exception 
	{
		System.err.println("temporarily disabled testTransactionIsolationForReadWithDeleteOperation() for HTTPRepository");
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
	public void testInferencerQueryDuringTransaction()
		throws Exception
	{
		System.err.println("temporarily disabled testInferencerDuringTransaction() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testInferencerTransactionIsolation()
		throws Exception
	{
		System.err.println("temporarily disabled testInferencerTransactionIsolation() for HTTPRepository");
	}

	@Ignore("temporarily disabled for HTTPRepository")
	@Test
	@Override
	public void testOrderByQueriesAreInterruptable() {
		System.err.println("temporarily disabled testOrderByQueriesAreInterruptable() for HTTPRepository");
	}

	@Test
	@Override
	public void testAddMalformedLiteralsDefaultConfig()
		throws Exception
	{
		try {
			testCon.add(
					RepositoryConnectionTest.class.getResourceAsStream(TEST_DIR_PREFIX + "malformed-literals.ttl"),
					"", RDFFormat.TURTLE);
		}
		catch (OpenRDFException e) {
			fail("upload of malformed literals should not fail with error in default configuration for HTTPRepository");
		}
	}

	@Test
	@Override
	@Ignore("See SES-1833")
	public void testAddMalformedLiteralsStrictConfig()
		throws Exception
	{
		System.err.println("SES-1833: temporarily disabled testAddMalformedLiteralsStrictConfig() for HTTPRepository");
	}

}