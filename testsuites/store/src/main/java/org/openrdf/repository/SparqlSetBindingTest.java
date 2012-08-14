/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import junit.framework.TestCase;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class SparqlSetBindingTest extends TestCase {

	public String queryBinding = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			 + " } ";

	public String queryBindingSubselect = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?name ?mbox\n"
			+ " WHERE { ?x foaf:name  ?name ;\n" + "            foaf:mbox  ?mbox .\n"
			+ "        { SELECT ?x WHERE { ?x a foaf:Person } } } ";

	private Repository repository;

	private RepositoryConnection conn;

	private ValueFactory vf;

	private Literal ringo;

	private URI ringoRes;
	
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

	@Override
	protected void setUp()
		throws Exception
	{
		repository = createRepository();
		vf = repository.getValueFactory();
		ringo = vf.createLiteral("Ringo Starr");
		ringoRes = vf.createURI("http://example.org/ns#", "ringo");
		
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

	@Override
	protected void tearDown()
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
		URI subj = vf.createURI("http://example.org/ns#", id);
		URI foafName = vf.createURI("http://xmlns.com/foaf/0.1/", "name");
		URI foafMbox = vf.createURI("http://xmlns.com/foaf/0.1/", "mbox");
		
		conn.add(subj, RDF.TYPE, vf.createURI("http://xmlns.com/foaf/0.1/", "Person"));
		conn.add(subj, foafName, vf.createLiteral(name));
		conn.add(subj, foafMbox, vf.createURI("mailto:", email));
		conn.close();
	}
}
