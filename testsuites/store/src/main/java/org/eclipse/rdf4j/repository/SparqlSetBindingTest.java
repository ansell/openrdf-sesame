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
package org.eclipse.rdf4j.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class SparqlSetBindingTest {

	public String queryBinding = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n" + " } ";

	public String queryBindingSubselect = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name ?mbox\n" + " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			+ "        { SELECT ?x WHERE { ?x a foaf:Person } } } ";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	private Literal ringo;

	private IRI ringoRes;

	@Test
	public void testBinding()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryBinding);
		query.setBinding("name", ringo);
		TupleQueryResult result = query.evaluate();
		assertEquals(ringo, result.next().getValue("name"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Test
	public void testBindingSubselect()
		throws Exception
	{
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryBindingSubselect);
		query.setBinding("x", ringoRes);
		TupleQueryResult result = query.evaluate();
		assertEquals(ringo, result.next().getValue("name"));
		assertFalse(result.hasNext());
		result.close();
	}

	@Before
	public void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		ringo = vf.createLiteral("Ringo Starr");
		ringoRes = vf.createIRI("http://example.org/ns#", "ringo");

		createUser("john", "John Lennon", "john@example.org");
		createUser("paul", "Paul McCartney", "paul@example.org");
		createUser("ringo", "Ringo Starr", "ringo@example.org");
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

		conn.add(subj, RDF.TYPE, vf.createIRI("http://xmlns.com/foaf/0.1/", "Person"));
		conn.add(subj, foafName, vf.createLiteral(name));
		conn.add(subj, foafMbox, vf.createIRI("mailto:", email));
		conn.close();
	}
}
