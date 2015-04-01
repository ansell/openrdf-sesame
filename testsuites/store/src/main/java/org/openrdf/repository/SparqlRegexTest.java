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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class SparqlRegexTest {

	public String queryInline = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			+ "         FILTER regex(str(?mbox), \"@Work.example\", \"i\") }";

	public String queryBinding = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			+ "         FILTER regex(str(?mbox), ?pattern) }";

	public String queryBindingFlags = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			+ "         FILTER regex(str(?mbox), ?pattern, ?flags) }";

	public String queryExpr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			+ "         ?y <http://example.org/ns#pattern>  ?pattern .\n"
			+ "         ?y <http://example.org/ns#flags>  ?flags .\n"
			+ "         FILTER regex(str(?mbox), ?pattern, ?flags) }";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	private Literal hunt;

	@Test
	public void testInline()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryInline);
		TupleQueryResult result = query.evaluate();
		assertEquals(hunt, result.next().getValue("name"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testBinding()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryBinding);
		query.setBinding("pattern", vf.createLiteral("@work.example"));
		TupleQueryResult result = query.evaluate();
		assertEquals(hunt, result.next().getValue("name"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testBindingFlags()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryBindingFlags);
		query.setBinding("pattern", vf.createLiteral("@Work.example"));
		query.setBinding("flags", vf.createLiteral("i"));
		TupleQueryResult result = query.evaluate();
		assertEquals(hunt, result.next().getValue("name"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testExpr()
		throws Exception
	{
		IRI pattern = vf.createIRI("http://example.org/ns#", "pattern");
		IRI flags = vf.createIRI("http://example.org/ns#", "flags");
		BNode bnode = vf.createBNode();
		conn.add(bnode, pattern, vf.createLiteral("@Work.example"));
		conn.add(bnode, flags, vf.createLiteral("i"));
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryExpr);
		TupleQueryResult result = query.evaluate();
		assertEquals(hunt, result.next().getValue("name"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Before
	public void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		hunt = vf.createLiteral("James Leigh Hunt");
		createUser("james", "James Leigh", "james@leigh");
		createUser("megan", "Megan Leigh", "megan@leigh");
		createUser("hunt", "James Leigh Hunt", "james@work.example");
		conn = repository.getConnection();
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

	private void createUser(String id, String name, String email)
		throws RepositoryException
	{
		RepositoryConnection conn = repository.getConnection();
		IRI subj = vf.createIRI("http://example.org/ns#", id);
		IRI foafName = vf.createIRI("http://xmlns.com/foaf/0.1/", "name");
		IRI foafMbox = vf.createIRI("http://xmlns.com/foaf/0.1/", "mbox");
		conn.add(subj, foafName, vf.createLiteral(name));
		conn.add(subj, foafMbox, vf.createIRI("mailto:", email));
		conn.close();
	}
}
