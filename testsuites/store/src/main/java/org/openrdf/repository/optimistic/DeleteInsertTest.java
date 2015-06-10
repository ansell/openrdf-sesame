package org.openrdf.repository.optimistic;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.io.IOUtil;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.OptimisticIsolationTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

public class DeleteInsertTest {
	private Repository repo;
	private String NS = "http://example.org/";
	private RepositoryConnection con;
	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;
	private ClassLoader cl = getClass().getClassLoader();

	@Before
	public void setUp() throws Exception {
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(DeleteInsertTest.class);
		con = repo.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
	}

	@Test
	public void test() throws Exception {
		String load = IOUtil.readString(cl.getResource("test/insert-data.ru"));
		con.prepareUpdate(QueryLanguage.SPARQL, load, NS).execute();
		con.begin(level);
		String modify = IOUtil.readString(cl.getResource("test/delete-insert.ru"));
		con.prepareUpdate(QueryLanguage.SPARQL, modify, NS).execute();
		con.commit();
		String ask = IOUtil.readString(cl.getResource("test/ask.rq"));
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, ask, NS).evaluate());
	}
}
