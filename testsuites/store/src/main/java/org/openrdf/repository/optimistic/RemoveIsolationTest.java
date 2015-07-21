package org.openrdf.repository.optimistic;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.OptimisticIsolationTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

public class RemoveIsolationTest {

	private Repository repo;

	private RepositoryConnection con;

	private ValueFactory f;

	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;

	@Before
	public void setUp()
		throws Exception
	{
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(RemoveIsolationTest.class);
		con = repo.getConnection();
		f = con.getValueFactory();
	}

	@After
	public void tearDown()
		throws Exception
	{
		con.close();
		repo.shutDown();
	}

	@Test
	public void testRemoveOptimisticIsolation()
		throws Exception
	{
		con.begin(level);

		con.add(f.createURI("http://example.org/people/alice"),
				f.createURI("http://example.org/ontology/name"), f.createLiteral("Alice"));

		con.remove(con.getStatements(null, null, null, true));

		RepositoryResult<Statement> stats = con.getStatements(null, null, null, true);
		assertEquals(Collections.emptyList(), QueryResults.asList(stats));
		con.rollback();
	}

	@Test
	public void testRemoveIsolation()
		throws Exception
	{
		con.begin(level);

		con.add(f.createURI("http://example.org/people/alice"),
				f.createURI("http://example.org/ontology/name"), f.createLiteral("Alice"));

		con.remove(con.getStatements(null, null, null, true));

		RepositoryResult<Statement> stats = con.getStatements(null, null, null, true);
		assertEquals(Collections.emptyList(), QueryResults.asList(stats));
		con.rollback();
	}
}
