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
