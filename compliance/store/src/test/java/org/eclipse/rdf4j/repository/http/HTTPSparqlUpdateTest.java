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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLUpdateTest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jeen
 */
public class HTTPSparqlUpdateTest extends SPARQLUpdateTest {

	private static HTTPMemServer server;

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
