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

import org.openrdf.repository.RDFSchemaRepositoryConnectionTest;
import org.openrdf.repository.Repository;

public class RDFSchemaHTTPRepositoryConnectionTest extends RDFSchemaRepositoryConnectionTest {

	private HTTPMemServer server;

	public RDFSchemaHTTPRepositoryConnectionTest(String name) {
		super(name);
	}

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
	protected Repository createRepository() {
		return new HTTPRepository(HTTPMemServer.INFERENCE_REPOSITORY_URL);
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

	@Override
	public void testInferencerQueryDuringTransaction()
		throws Exception
	{
		System.err.println("temporarily disabled testInferencerDuringTransaction() for HTTPRepository");
	}

	@Override
	public void testInferencerTransactionIsolation()
		throws Exception
	{
		System.err.println("temporarily disabled testInferencerTransactionIsolation() for HTTPRepository");
	}

	@Override
	public void testOrderByQueriesAreInterruptable() {
		System.err.println("temporarily disabled testOrderByQueriesAreInterruptable() for HTTPRepository");
	}
}
