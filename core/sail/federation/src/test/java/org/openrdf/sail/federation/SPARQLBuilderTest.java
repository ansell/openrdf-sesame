package org.openrdf.sail.federation;

import static org.openrdf.query.QueryLanguage.SPARQL;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

@RunWith(Parameterized.class)
public class SPARQLBuilderTest {

	@Parameters(name = "{index}({0})-{2}:{3}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "StatementPattern", "SELECT * WHERE {?s ?p ?o}", "", "" },
				{ "Join", "SELECT * WHERE {?s ?p ?o; <urn:test:pred> ?obj}", "", "" },
				{ "Distinct", "SELECT DISTINCT ?s WHERE {?s ?p ?o; <urn:test:pred> ?obj}", "", "" },
				{ "Optional", "SELECT * WHERE {?s ?p ?o . OPTIONAL { ?s <urn:test:pred> ?obj}}", "", "" },
				{
						"Filter",
						"SELECT ?s WHERE {?s ?p ?o; <urn:test:pred> ?obj FILTER (str(?obj) = \"urn:test:obj\")}",
						"",
						"" },
				{
						"Bindings",
						"SELECT * WHERE {?s ?p ?o . OPTIONAL { ?s <urn:test:pred> ?obj}}",
						"s",
						"urn:test:subj" } });
	}

	private RepositoryConnection con;

	private ValueFactory valueFactory;

	private final String pattern, prefix, namespace;

	public SPARQLBuilderTest(String name, String pattern, String prefix, String namespace) {
		super();
		assert !name.isEmpty();
		this.pattern = pattern;
		this.prefix = prefix;
		this.namespace = namespace;
	}

	@Before
	public void setUp()
		throws Exception
	{
		Federation federation = new Federation();
		federation.addMember(new SailRepository(new MemoryStore()));
		Repository repository = new SailRepository(federation);
		repository.initialize();
		con = repository.getConnection();
		valueFactory = con.getValueFactory();
		IRI subj = valueFactory.createIRI("urn:test:subj");
		IRI pred = valueFactory.createIRI("urn:test:pred");
		IRI obj = valueFactory.createIRI("urn:test:obj");
		con.add(subj, pred, obj);
	}

	@Test
	public void test()
		throws OpenRDFException
	{ // NOPMD
		// Thrown exceptions are the only failure path.
		TupleQuery tupleQuery = con.prepareTupleQuery(SPARQL, pattern);
		if (!(prefix.isEmpty() || namespace.isEmpty())) {
			tupleQuery.setBinding(prefix, valueFactory.createIRI(namespace));
		}
		tupleQuery.evaluate().close();
	}
}
