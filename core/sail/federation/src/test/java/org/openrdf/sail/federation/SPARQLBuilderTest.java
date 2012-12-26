package org.openrdf.sail.federation;

import static org.openrdf.query.QueryLanguage.SPARQL;
import junit.framework.TestCase;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class SPARQLBuilderTest extends TestCase {

	private Repository repository;
	private RepositoryConnection con;
	private ValueFactory vf;

	public void setUp() throws Exception {
		Federation federation = new Federation();
		federation.addMember(new SailRepository(new MemoryStore()));
		repository = new SailRepository(federation);
		repository.initialize();
		con = repository.getConnection();
		vf = con.getValueFactory();
		URI subj = vf.createURI("urn:test:subj");
		URI pred = vf.createURI("urn:test:pred");
		URI obj = vf.createURI("urn:test:obj");
		con.add(subj, pred, obj);
	}

	public void testStatementPattern() throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, "SELECT * WHERE {?s ?p ?o}");
		query.evaluate().close();
	}

	public void testJoin() throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, "SELECT * WHERE {?s ?p ?o; <urn:test:pred> ?obj}");
		query.evaluate().close();
	}

	public void testDistinct() throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, "SELECT DISTINCT ?s WHERE {?s ?p ?o; <urn:test:pred> ?obj}");
		query.evaluate().close();
	}

	public void testOptional() throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, "SELECT * WHERE {?s ?p ?o . OPTIONAL { ?s <urn:test:pred> ?obj}}");
		query.evaluate().close();
	}

	public void testFilter() throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, "SELECT ?s WHERE {?s ?p ?o; <urn:test:pred> ?obj FILTER (str(?obj) = \"urn:test:obj\")}");
		query.evaluate().close();
	}

	public void testBindings() throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, "SELECT * WHERE {?s ?p ?o . OPTIONAL { ?s <urn:test:pred> ?obj}}");
		query.setBinding("s", vf.createURI("urn:test:subj"));
		query.evaluate().close();
	}
}
