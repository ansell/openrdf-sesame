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
package org.openrdf.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.IRIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;

public abstract class SparqlDatasetTest {

	public String queryNoFrom = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT (COUNT(DISTINCT ?name) as ?c) \n" + " WHERE { ?x foaf:name  ?name . } ";

	public String queryWithFrom = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT (COUNT(DISTINCT ?name) as ?c) \n" + " FROM <http://example.org/graph1> "
			+ " WHERE { ?x foaf:name  ?name . } ";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	private DatasetImpl dataset;

	private IRI graph1 = new IRIImpl("http://example.org/graph1");

	private IRI george;

	private IRI paul;

	private IRI john;

	private IRI ringo;

	@Test
	public void testNoFrom()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryNoFrom);
		TupleQueryResult result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(4, count.intValue());
		}
		result.close();

		query.setDataset(dataset);
		result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(2, count.intValue());
		}
		result.close();
	}

	@Test
	public void testWithFrom()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryWithFrom);
		TupleQueryResult result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(2, count.intValue());
		}
		result.close();

		query.setDataset(dataset);
		result = query.evaluate();

		assertTrue(result.hasNext());

		if (result.hasNext()) {
			BindingSet bs = result.next();
			assertFalse(result.hasNext());

			Literal count = (Literal)bs.getValue("c");
			assertEquals(2, count.intValue());
		}
		result.close();
	}

	@Before
	public void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		john = createUser("john", "John Lennon", "john@example.org");
		paul = createUser("paul", "Paul McCartney", "paul@example.org");
		george = createUser("george", "George Harrison", "george@example.org", graph1);
		ringo = createUser("ringo", "Ringo Starr", "ringo@example.org", graph1);
		conn = repository.getConnection();

		dataset = new DatasetImpl();
		dataset.addDefaultGraph(graph1);
	}

	protected Repository createRepository()
		throws Exception
	{
		Repository repository = newRepository();
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		try {
			con.clear();
			con.clearNamespaces();
		}
		finally {
			con.close();
		}
		return repository;
	}

	protected abstract Repository newRepository()
		throws Exception;

	@After
	public void tearDown()
		throws Exception
	{
		conn.close();
		conn = null;

		repository.shutDown();
		repository = null;
	}

	private IRI createUser(String id, String name, String email, Resource... context)
		throws RepositoryException
	{
		RepositoryConnection conn = repository.getConnection();
		IRI subj = vf.createIRI("http://example.org/ns#", id);
		IRI foafName = vf.createIRI("http://xmlns.com/foaf/0.1/", "name");
		IRI foafMbox = vf.createIRI("http://xmlns.com/foaf/0.1/", "mbox");

		conn.add(subj, RDF.TYPE, vf.createIRI("http://xmlns.com/foaf/0.1/", "Person"), context);
		conn.add(subj, foafName, vf.createLiteral(name), context);
		conn.add(subj, foafMbox, vf.createIRI("mailto:", email), context);
		conn.close();

		return subj;
	}
}
