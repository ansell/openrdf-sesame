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
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.http.HTTPMemServer;
import org.openrdf.repository.sparql.SPARQLRepository;

public class SPARQLStoreConnectionTest extends RepositoryConnectionTest {

	private HTTPMemServer server;

	public SPARQLStoreConnectionTest(String name) {
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
		
		// overwrite bnode test values as SPARQL endpoints do not generally work well with bnodes
		bob = testRepository.getValueFactory().createURI("urn:x-local:bob");
		alice = testRepository.getValueFactory().createURI("urn:x-local:alice");
		alexander = testRepository.getValueFactory().createURI("urn:x-local:alexander");
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
	
	@Override
	@Ignore
	public void testAutoCommit()
		throws Exception
	{
		System.err.println("temporarily disabled testAutoCommit() for SPARQLRepository");
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
	public void testPrepareSeRQLQuery()
		throws Exception
	{
		System.err.println("disabled testPrepareSeRQLQuery() for SPARQLRepository");
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
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS +"> ");
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
			queryBuilder.append(" PREFIX foaf: <" + FOAF_NS +"> ");
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
	@Ignore
	public void testBNodeSerialization()
			throws Exception {
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
