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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
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

	@Ignore
	@Test
	@Override
	public void testAutoCommitHandling() {
		// transaction isolation is not supported for HTTP connections. disabling
		// test.
		System.err.println("temporarily disabled testAutoCommitHandling() for HTTPRepository. See SES-1652");
	}

	@Test
	public void testBindingsInUpdateTransaction()
		throws Exception
	{
		// See issue SES-1889
		logger.debug("executing test testBindingsInUpdateTransaction");

		StringBuilder update1 = new StringBuilder();
		update1.append(getNamespaceDeclarations());
		update1.append("DELETE { ?x foaf:name ?y } WHERE {?x foaf:name ?y }");

		try {
			assertTrue(con.hasStatement(bob, FOAF.NAME, f.createLiteral("Bob"), true));
			assertTrue(con.hasStatement(alice, FOAF.NAME, f.createLiteral("Alice"), true));

			con.begin();
			Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update1.toString());
			operation.setBinding("x", bob);
			

			operation.execute();

			con.commit();
		
			// only bob's name should have been deleted (due to the binding)
			assertFalse(con.hasStatement(bob, FOAF.NAME, f.createLiteral("Bob"), true));
			assertTrue(con.hasStatement(alice, FOAF.NAME, f.createLiteral("Alice"), true));

		}
		catch (Exception e) {
			if(con.isActive()) {
				con.rollback();
			}
		}
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
