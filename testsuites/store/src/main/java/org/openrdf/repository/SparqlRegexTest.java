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
