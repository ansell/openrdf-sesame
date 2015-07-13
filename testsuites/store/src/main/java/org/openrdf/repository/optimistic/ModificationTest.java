package org.openrdf.repository.optimistic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.OptimisticIsolationTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

public class ModificationTest {
	private Repository repo;
	private RepositoryConnection con;
	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;
	private String NS = "http://rdf.example.org/";
	private URI PAINTER;
	private URI PICASSO;

	@Before
	public void setUp() throws Exception {
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(ModificationTest.class);
		ValueFactory uf = repo.getValueFactory();
		PAINTER = uf.createURI(NS, "Painter");
		PICASSO = uf.createURI(NS, "picasso");
		con = repo.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
	}

	@Test
	public void testAdd() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER);
		con.commit();
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void testAutoCommit() throws Exception {
		con.add(PICASSO, RDF.TYPE, PAINTER);
		con.close();
		con = repo.getConnection();
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void testInsertData() throws Exception {
		con.begin(level);
		con.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <picasso> a <Painter> }", NS).execute();
		con.commit();
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void testInsertDataAutoCommit() throws Exception {
		con.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <picasso> a <Painter> }", NS).execute();
		con.close();
		con = repo.getConnection();
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void testRemove() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER);
		con.commit();
		con.begin(level);
		con.remove(PICASSO, RDF.TYPE, PAINTER);
		con.commit();
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void testAddIn() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.commit();
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
	}

	@Test
	public void testRemoveFrom() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.commit();
		con.begin(level);
		con.remove(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.commit();
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
	}

	@Test
	public void testMove() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.commit();
		con.begin(level);
		con.remove(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.add(PICASSO, RDF.TYPE, PAINTER, PAINTER);
		con.commit();
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PAINTER));
	}

	@Test
	public void testMoveOut() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.commit();
		con.begin(level);
		con.remove(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.add(PICASSO, RDF.TYPE, PAINTER);
		con.commit();
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void testCancel() throws Exception {
		con.begin(level);
		con.add(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.remove(PICASSO, RDF.TYPE, PAINTER, PICASSO);
		con.commit();
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
	}

	@Test
	public void testRemoveDuplicate() throws Exception {
		con.add(PICASSO, RDF.TYPE, PAINTER, PICASSO, PAINTER);
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PAINTER));
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
		con.begin(level);
		con.remove(PICASSO, RDF.TYPE, PAINTER, PAINTER);
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PAINTER));
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK {<"+PICASSO+"> a <"+PAINTER+">}").evaluate());
		con.commit();
		assertFalse(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PAINTER));
		assertTrue(con.hasStatement(PICASSO, RDF.TYPE, PAINTER, false, PICASSO));
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK {<"+PICASSO+"> a <"+PAINTER+">}").evaluate());
	}

}
