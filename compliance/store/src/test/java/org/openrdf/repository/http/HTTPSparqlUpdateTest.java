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

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

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
			File testFolder = tempDir.newFolder("sesame-http-sparql-update-datadir");
			testFolder.mkdirs();
			server.start(testFolder);
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
		return new HTTPRepository(server.getRepositoryUrl());
	}

	@Ignore
	@Test
	@Override
	public void testAutoCommitHandling() {
		// transaction isolation is not supported for HTTP connections. disabling
		// test.
		System.err.println("temporarily disabled testAutoCommitHandling() for HTTPRepository. See SES-1652");
	}

	@Ignore
	@Test
	@Override
	public void testConsecutiveUpdatesInSameTransaction() {
		// transaction isolation is not supported for HTTP connections. disabling
		// test.
		System.err.println("temporarily disabled testConsecutiveUpdatesInSameTransaction() for HTTPRepository. See SES-1652");
	}
}
