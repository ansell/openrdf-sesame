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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class SparqlAggregatesTest {

	public String selectNameMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name; foaf:mbox  ?mbox }";

	public String concatMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name (group_concat(?mbox) AS ?mbox)\n"
			+ " WHERE { ?x foaf:name  ?name; foaf:mbox  ?mbox } GROUP BY ?name";

	public String concatOptionalMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
		+ "SELECT ?name (group_concat(?mbox) AS ?mbox)\n"
		+ " WHERE { ?x foaf:name  ?name OPTIONAL { ?x foaf:mbox  ?mbox } } GROUP BY ?name";

	public String countMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name (count(?mbox) AS ?mbox)\n"
			+ " WHERE { ?x foaf:name  ?name; foaf:mbox  ?mbox } GROUP BY ?name";

	public String countOptionalMbox = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
		+ "SELECT ?name (count(?mb) AS ?mbox)\n"
		+ " WHERE { ?x foaf:name  ?name OPTIONAL { ?x foaf:mbox  ?mb } } GROUP BY ?name";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	@Test
	public void testSelect()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, selectNameMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		result.next();
		result.next();
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testConcat()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, concatMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		assertNotNull(result.next().getValue("mbox"));
		assertNotNull(result.next().getValue("mbox"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testConcatOptional()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, concatOptionalMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		result.next();
		result.next();
		result.next();
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testCount()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, countMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		assertEquals("1", result.next().getValue("mbox").stringValue());
		assertEquals("1", result.next().getValue("mbox").stringValue());
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testCountOptional()
		throws Exception
	{
		Set<String> zeroOr1 = new HashSet<String>();
		zeroOr1.add("0");
		zeroOr1.add("1");
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, countOptionalMbox);
		TupleQueryResult result = query.evaluate();
		assertTrue(result.hasNext());
		assertTrue(zeroOr1.contains(result.next().getValue("mbox").stringValue()));
		assertTrue(zeroOr1.contains(result.next().getValue("mbox").stringValue()));
		assertTrue(zeroOr1.contains(result.next().getValue("mbox").stringValue()));
		assertFalse(result.hasNext());
		result.close();
	}

	@Before
	public void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		createUser("james", "James Leigh", "james@leigh");
		createUser("megan", "Megan Leigh", "megan@leigh");
		createUser("hunt", "James Leigh Hunt", null);
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
		if (email != null) {
			conn.add(subj, foafMbox, vf.createIRI("mailto:", email));
		}
		conn.close();
	}
}
