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
package org.openrdf.repository.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.http.HTTPMemServer;

public class SPARQLStoreConnectionTest extends RepositoryConnectionTest {

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
			server = null;
			throw e;
		}

	}

	@AfterClass
	public static void stopServer()
		throws Exception
	{
		server.stop();
		server = null;
	}

	@Before
	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();
		// overwrite bnode test values as SPARQL endpoints do not generally work
		// well with bnodes
		bob = testRepository.getValueFactory().createURI("urn:x-local:bob");
		alice = testRepository.getValueFactory().createURI("urn:x-local:alice");
		alexander = testRepository.getValueFactory().createURI("urn:x-local:alexander");
	}

	@Override
	protected Repository createRepository() {
		return new SPARQLRepository(HTTPMemServer.REPOSITORY_URL,
				Protocol.getStatementsLocation(HTTPMemServer.REPOSITORY_URL));
	}

	@Override
	@Ignore
	public void testSizeRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testSizeRollback() for SPARQLRepository");
	}

	@Test
	@Ignore
	@Override
	public void testURISerialization()
		throws Exception
	{
		System.err.println("temporarily disabled testURISerialization() for SPARQLRepository");
	}

	@Test
	@Ignore
	@Override
	public void testStatementSerialization()
		throws Exception
	{
		System.err.println("temporarily disabled testStatementSerialization() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testAutoCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testAutoCommit() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testRollback() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testEmptyRollback()
		throws Exception
	{
		System.err.println("temporarily disabled testEmptyRollback() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testEmptyCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testEmptyCommit() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testPrepareSeRQLQuery()
		throws Exception
	{
		System.err.println("disabled testPrepareSeRQLQuery() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testLiteralSerialization()
		throws Exception
	{
		System.err.println("temporarily disabled testLiteralSerialization() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testSizeCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testSizeCommit() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testGetStatementsInMultipleContexts()
		throws Exception
	{
		System.err.println("temporarily disabled testGetStatementsInMultipleContexts() for SPARQLRepository: implementation of statement context using SPARQL not yet complete");
		// TODO see SES-1776
	}

	@Override
	@Ignore
	public void testGetStatementsInSingleContext()
		throws Exception
	{
		System.err.println("temporarily disabled testGetStatementsInSingleContext() for SPARQLRepository: implementation of statement context using SPARQL not yet complete");
		// TODO see SES-1776
	}

	@Ignore
	@Override
	public void testOrderByQueriesAreInterruptable()
		throws Exception
	{
		System.err.println("temporarily disabled testOrderByQueriesAreInterruptable() for SPARQLRepository");
	}

	@Test
	@Override
	@Ignore("can not execute test because required data add results in illegal SPARQL syntax")
	public void testGetStatementsMalformedLanguageLiteral()
		throws Exception
	{
		System.err.println("temporarily disabled testGetStatementsMalformedLanguageLiteral() for SPARQLRepository");
	}

	@Override
	public void testPreparedTupleQuery()
		throws Exception
	{
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append(" SELECT ?name ?mbox");
		queryBuilder.append(" WHERE { [] foaf:name ?name;");
		queryBuilder.append("            foaf:mbox ?mbox. }");

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryBuilder.toString());
		query.setBinding("name", nameBob);

		TupleQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("name"));
				assertTrue(solution.hasBinding("mbox"));

				Value nameResult = solution.getValue("name");
				Value mboxResult = solution.getValue("mbox");

				assertEquals("unexpected value for name: " + nameResult, nameBob, nameResult);
				assertEquals("unexpected value for mbox: " + mboxResult, mboxBob, mboxResult);
			}
		}
		finally {
			result.close();
		}
	}

	@Override
	@Ignore
	public void testGetNamespaces()
		throws Exception
	{
		System.err.println("disabled testGetNamespaces() as namespace retrieval is not supported by SPARQL");
	}

	@Override
	@Ignore
	public void testGetNamespace()
		throws Exception
	{
		System.err.println("disabled testGetNamespace() as namespace retrieval is not supported by SPARQL");
	}

	@Ignore("disabled for SPARQLRepository")
	@Test
	@Override
	public void testReadOfAddedStatement1()
		throws Exception
	{
		System.err.println("temporarily disabled testReadOfAddedStatement1s() for SPARQLRepository");
	}

	@Ignore("temporarily disabled for SPARQLRepository")
	@Test
	@Override
	public void testReadOfAddedStatement2()
		throws Exception
	{
		System.err.println("temporarily disabled testReadOfAddedStatement2() for SPARQLRepository");
	}

	@Ignore("temporarily disabled for SPARQLRepository")
	@Test
	@Override
	public void testTransactionIsolationForRead()
		throws Exception
	{
		System.err.println("temporarily disabled testTransactionIsolationForRead() for SPARQLRepository");
	}

	@Override
	@Ignore
	public void testTransactionIsolation()
		throws Exception
	{
		System.err.println("temporarily disabled testTransactionIsolation() for SPARQLRepository");
	}

	@Override
	public void testPreparedTupleQuery2()
		throws Exception
	{
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + ">");
		queryBuilder.append(" SELECT ?name ?mbox");
		queryBuilder.append(" WHERE {?p  foaf:name ?name ;");
		queryBuilder.append("            foaf:mbox ?mbox .");
		queryBuilder.append(" FILTER (?p = ?VAR) } ");

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryBuilder.toString());
		query.setBinding("VAR", bob);

		TupleQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("name"));
				assertTrue(solution.hasBinding("mbox"));

				Value nameResult = solution.getValue("name");
				Value mboxResult = solution.getValue("mbox");

				assertEquals("unexpected value for name: " + nameResult, nameBob, nameResult);
				assertEquals("unexpected value for mbox: " + mboxResult, mboxBob, mboxResult);
			}
		}
		finally {
			result.close();
		}
	}

	@Override
	public void testPreparedTupleQueryUnicode()
		throws Exception
	{
		testCon.add(alexander, name, Александър);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append(" SELECT ?person");
		queryBuilder.append(" WHERE {?person foaf:name ?name . }");

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryBuilder.toString());
		query.setBinding("name", Александър);

		TupleQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("person"));
				assertEquals(alexander, solution.getValue("person"));
			}
		}
		finally {
			result.close();
		}
	}

	@Override
	public void testSimpleGraphQuery()
		throws Exception
	{
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + ">");
		queryBuilder.append(" CONSTRUCT ");
		queryBuilder.append(" WHERE { [] foaf:name ?name; ");
		queryBuilder.append("            foaf:mbox ?mbox. }");

		GraphQueryResult result = testCon.prepareGraphQuery(QueryLanguage.SPARQL, queryBuilder.toString()).evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				Statement st = result.next();
				if (name.equals(st.getPredicate())) {
					assertTrue(nameAlice.equals(st.getObject()) || nameBob.equals(st.getObject()));
				}
				else {
					assertTrue(mbox.equals(st.getPredicate()));
					assertTrue(mboxAlice.equals(st.getObject()) || mboxBob.equals(st.getObject()));
				}
			}
		}
		finally {
			result.close();
		}
	}

	@Override
	public void testPreparedGraphQuery()
		throws Exception
	{
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append(" CONSTRUCT ");
		queryBuilder.append(" WHERE { [] foaf:name ?name ;");
		queryBuilder.append("            foaf:mbox ?mbox . ");
		queryBuilder.append(" } ");

		GraphQuery query = testCon.prepareGraphQuery(QueryLanguage.SPARQL, queryBuilder.toString());
		query.setBinding("name", nameBob);

		GraphQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				Statement st = result.next();
				assertTrue(name.equals(st.getPredicate()) || mbox.equals(st.getPredicate()));
				if (name.equals(st.getPredicate())) {
					assertTrue("unexpected value for name: " + st.getObject(), nameBob.equals(st.getObject()));
				}
				else {
					assertTrue(mbox.equals(st.getPredicate()));
					assertTrue("unexpected value for mbox: " + st.getObject(), mboxBob.equals(st.getObject()));
				}

			}
		}
		finally {
			result.close();
		}
	}

	@Override
	public void testSimpleTupleQuery()
		throws Exception
	{
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append(" SELECT ?name ?mbox");
		queryBuilder.append(" WHERE { [] foaf:name ?name ;");
		queryBuilder.append("            foaf:mbox ?mbox . ");
		queryBuilder.append(" } ");
		TupleQueryResult result = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryBuilder.toString()).evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("name"));
				assertTrue(solution.hasBinding("mbox"));

				Value nameResult = solution.getValue("name");
				Value mboxResult = solution.getValue("mbox");

				assertTrue((nameAlice.equals(nameResult) || nameBob.equals(nameResult)));
				assertTrue((mboxAlice.equals(mboxResult) || mboxBob.equals(mboxResult)));
			}
		}
		finally {
			result.close();
		}
	}

	@Override
	public void testSimpleTupleQueryUnicode()
		throws Exception
	{
		testCon.add(alexander, name, Александър);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + ">");
		queryBuilder.append(" SELECT ?person");
		queryBuilder.append(" WHERE { ?person foaf:name \"").append(Александър.getLabel()).append("\" . } ");

		TupleQueryResult result = testCon.prepareTupleQuery(QueryLanguage.SPARQL, queryBuilder.toString()).evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("person"));
				assertEquals(alexander, solution.getValue("person"));
			}
		}
		finally {
			result.close();
		}
	}

	@Override
	@Ignore
	public void testBNodeSerialization()
		throws Exception
	{
		System.err.println("temporarily disabled testBNodeSerialization() for SPARQLRepository");
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
		testCon.commit();

		assertTrue(testCon.hasStatement(foobar, foobar, foobar, true));

	}

}
