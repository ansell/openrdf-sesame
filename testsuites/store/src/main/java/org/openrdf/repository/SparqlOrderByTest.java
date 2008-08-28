package org.openrdf.repository;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.StoreException;
import org.openrdf.model.Literal;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public abstract class SparqlOrderByTest extends TestCase {

	private String query1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name\n" + "WHERE { ?x foaf:name ?name }\n"
			+ "ORDER BY ?name\n";

	private String query2 = "PREFIX     :    <http://example.org/ns#>\n"
			+ "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
			+ "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
			+ "SELECT ?name\n" + "WHERE { ?x foaf:name ?name ; :empId ?emp }\n"
			+ "ORDER BY DESC(?emp)\n";

	private String query3 = "PREFIX     :    <http://example.org/ns#>\n"
			+ "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
			+ "SELECT ?name\n" + "WHERE { ?x foaf:name ?name ; :empId ?emp }\n"
			+ "ORDER BY ?name DESC(?emp)\n";

	private Repository repository;

	private RepositoryConnection conn;

	public void testQuery1() throws Exception {
		assertTrue("James Leigh".compareTo("James Leigh Hunt") < 0);
		assertResult(query1, Arrays.asList("James Leigh", "James Leigh",
				"James Leigh Hunt", "Megan Leigh"));
	}

	public void testQuery2() throws Exception {
		assertResult(query2, Arrays.asList("Megan Leigh", "James Leigh",
				"James Leigh Hunt", "James Leigh"));
	}

	public void testQuery3() throws Exception {
		assertResult(query3, Arrays.asList("James Leigh", "James Leigh",
				"James Leigh Hunt", "Megan Leigh"));
	}

	@Override
	protected void setUp() throws Exception {
		repository = createRepository();
		createEmployee("james", "James Leigh", 123);
		createEmployee("jim", "James Leigh", 244);
		createEmployee("megan", "Megan Leigh", 1234);
		createEmployee("hunt", "James Leigh Hunt", 243);
		conn = repository.getConnection();
	}

	protected Repository createRepository() throws Exception {
		Repository repository = newRepository();
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		try {
			con.clear();
			con.clearNamespaces();
		} finally {
			con.close();
		}
		return repository;
	}

	protected abstract Repository newRepository() throws Exception;

	@Override
	protected void tearDown() throws Exception {
		conn.close();
		repository.shutDown();
	}

	private void createEmployee(String id, String name, int empId)
			throws StoreException {
		URIFactory uf = repository.getURIFactory();
		LiteralFactory lf = repository.getLiteralFactory();
		String foafName = "http://xmlns.com/foaf/0.1/name";
		String exEmpId = "http://example.org/ns#empId";
		RepositoryConnection conn = repository.getConnection();
		conn.add(uf.createURI("http://example.org/ns#" + id), uf
				.createURI(foafName), lf.createLiteral(name));
		conn.add(uf.createURI("http://example.org/ns#" + id), uf
				.createURI(exEmpId), lf.createLiteral(empId));
		conn.close();
	}

	private void assertResult(String queryStr, List<String> names)
			throws StoreException, MalformedQueryException,
			EvaluationException {
		TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL,
				queryStr);
		TupleQueryResult result = query.evaluate();
		for (String name : names) {
			Value value = result.next().getValue("name");
			assertEquals(name, ((Literal) value).getLabel());
		}
		assertFalse(result.hasNext());
		result.close();
	}
}
