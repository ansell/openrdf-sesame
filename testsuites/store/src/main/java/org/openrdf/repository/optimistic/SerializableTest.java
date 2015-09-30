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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.OptimisticIsolationTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConflictException;

public class SerializableTest {
	private Repository repo;
	private RepositoryConnection a;
	private RepositoryConnection b;
	private IsolationLevel level = IsolationLevels.SERIALIZABLE;
	private String NS = "http://rdf.example.org/";
	private ValueFactory lf;
	private IRI PAINTER;
	private IRI PAINTS;
	private IRI PAINTING;
	private IRI YEAR;
	private IRI PERIOD;
	private IRI PICASSO;
	private IRI REMBRANDT;
	private IRI GUERNICA;
	private IRI JACQUELINE;
	private IRI NIGHTWATCH;
	private IRI ARTEMISIA;
	private IRI DANAE;
	private IRI JACOB;
	private IRI ANATOMY;
	private IRI BELSHAZZAR;

	@Before
	public void setUp() throws Exception {
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(SerializableTest.class);
		lf = repo.getValueFactory();
		ValueFactory uf = repo.getValueFactory();
		PAINTER = uf.createIRI(NS, "Painter");
		PAINTS = uf.createIRI(NS, "paints");
		PAINTING = uf.createIRI(NS, "Painting");
		YEAR = uf.createIRI(NS, "year");
		PERIOD = uf.createIRI(NS, "period");
		PICASSO = uf.createIRI(NS, "picasso");
		REMBRANDT = uf.createIRI(NS, "rembrandt");
		GUERNICA = uf.createIRI(NS, "guernica");
		JACQUELINE = uf.createIRI(NS, "jacqueline");
		NIGHTWATCH = uf.createIRI(NS, "nightwatch");
		ARTEMISIA = uf.createIRI(NS, "artemisia");
		DANAE = uf.createIRI(NS, "danaë");
		JACOB = uf.createIRI(NS, "jacob");
		ANATOMY = uf.createIRI(NS, "anatomy");
		BELSHAZZAR = uf.createIRI(NS, "belshazzar");
		a = repo.getConnection();
		b = repo.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		a.close();
		b.close();
		repo.shutDown();
	}

	@Test
	public void test_independentPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(a, PICASSO, RDF.TYPE, PAINTER, false));
		assertEquals(1, size(b, REMBRANDT, RDF.TYPE, PAINTER, false));
		a.commit();
		b.commit();
		assertEquals(2, size(a, null, RDF.TYPE, PAINTER, false));
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void test_safePattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(a, null, RDF.TYPE, PAINTER, false));
		a.commit();
		b.commit();
	}

	@Test
	public void test_afterPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(a, null, RDF.TYPE, PAINTER, false));
		a.commit();
		b.commit();
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void test_afterInsertDataPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <picasso> a <Painter> }", NS).execute();
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <rembrandt> a <Painter> }", NS).execute();
		assertEquals(1, size(a, null, RDF.TYPE, PAINTER, false));
		a.commit();
		b.commit();
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void test_conflictPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(b, null, RDF.TYPE, PAINTER, false));
		a.commit();
		try {
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
	}

	@Test
	public void test_safeQuery() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { [a <Painter>] <paints> ?painting }");
		assertEquals(3, result.size());
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_safeInsert() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n"
				+ "WHERE { [a <Painter>] <paints> ?painting }", NS).execute();
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_conflictQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { [a <Painter>] <paints> ?painting }");
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		try {
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(0, size(a, null, RDF.TYPE, PAINTING, false));
	}

	@Test
	public void test_conflictInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n"
				+ "WHERE { [a <Painter>] <paints> ?painting }", NS).execute();
		a.commit();
		try {
			size(b, null, PAINTS, null, false);
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(0, size(a, null, RDF.TYPE, PAINTING, false));
	}

	@Test
	public void test_safeOptionalQuery() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_safeOptionalInsert() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n"
				+ "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }", NS).execute();
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_conflictOptionalQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		try {
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(7, size(a, null, null, null, false));
	}

	@Test
	public void test_conflictOptionalInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n"
				+ "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }", NS).execute();
		a.commit();
		try {
			size(b, null, PAINTS, null, false);
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(7, size(a, null, null, null, false));
	}

	@Test
	public void test_safeFilterQuery() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { ?painter a <Painter>; <paints> ?painting "
				+ "FILTER  regex(str(?painter), \"rem\", \"i\") }");
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		try {
			b.commit();
			assertEquals(10, size(a, null, null, null, false));
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
			assertEquals(7, size(a, null, null, null, false));
		}
	}

	@Test
	public void test_safeFilterInsert() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n"
				+ "WHERE { ?painter a <Painter>; <paints> ?painting "
				+ "FILTER  regex(str(?painter), \"rem\", \"i\") }", NS).execute();
		a.commit();
		try {
			b.commit();
			assertEquals(10, size(a, null, null, null, false));
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
			assertEquals(7, size(a, null, null, null, false));
		}
	}

	@Test
	public void test_conflictOptionalFilterQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(GUERNICA, RDF.TYPE, PAINTING);
		a.add(JACQUELINE, RDF.TYPE, PAINTING);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { [a <Painter>] <paints> ?painting "
				+ "OPTIONAL { ?painting a ?type  } FILTER (!bound(?type)) }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		try {
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(9, size(a, null, null, null, false));
	}

	@Test
	public void test_conflictOptionalFilterInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(GUERNICA, RDF.TYPE, PAINTING);
		a.add(JACQUELINE, RDF.TYPE, PAINTING);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n"
				+ "WHERE { [a <Painter>] <paints> ?painting "
				+ "OPTIONAL { ?painting a ?type  } FILTER (!bound(?type)) }", NS).execute();
		a.commit();
		try {
			size(b, null, RDF.TYPE, PAINTING, false);
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(9, size(a, null, null, null, false));
	}

	@Test
	public void test_safeRangeQuery() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
				+ "FILTER  (1631 <= ?year && ?year <= 1635) }");
		for (Value painting : result) {
			b.add((Resource) painting, PERIOD, lf.createLiteral("First Amsterdam period"));
		}
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.commit();
		try {
			b.commit();
			assertEquals(17, size(a, null, null, null, false));
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
			assertEquals(13, size(a, null, null, null, false));
		}
	}

	@Test
	public void test_safeRangeInsert() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting <period> \"First Amsterdam period\" }\n"
				+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
				+ "FILTER  (1631 <= ?year && ?year <= 1635) }", NS).execute();
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.commit();
		try {
			b.commit();
			assertEquals(17, size(a, null, null, null, false));
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
			assertEquals(13, size(a, null, null, null, false));
		}
	}

	@Test
	public void test_conflictRangeQuery() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		List<Value> result = eval("painting", b, "SELECT ?painting "
				+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
				+ "FILTER  (1631 <= ?year && ?year <= 1635) }");
		for (Value painting : result) {
			b.add((Resource) painting, PERIOD, lf.createLiteral("First Amsterdam period"));
		}
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.commit();
		try {
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(13, size(a, null, null, null, false));
	}

	@Test
	public void test_conflictRangeInsert() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting <period> \"First Amsterdam period\" }\n"
				+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
				+ "FILTER  (1631 <= ?year && ?year <= 1635) }", NS).execute();
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.commit();
		try {
			size(b, REMBRANDT, PAINTS, null, false);
			b.commit();
			fail();
		} catch (RepositoryException e) {
			e.printStackTrace();
			assertTrue(e.getCause() instanceof SailConflictException);
		}
		assertEquals(13, size(a, null, null, null, false));
	}

	private int size(RepositoryConnection con, Resource subj, IRI pred,
			Value obj, boolean inf, Resource... ctx) throws Exception {
		return QueryResults.asList(con.getStatements(subj, pred, obj, inf, ctx)).size();
	}

	private List<Value> eval(String var, RepositoryConnection con, String qry)
			throws Exception {
		TupleQueryResult result;
		result = con.prepareTupleQuery(QueryLanguage.SPARQL, qry, NS).evaluate();
		try {
			List<Value> list = new ArrayList<Value>();
			while (result.hasNext()) {
				list.add(result.next().getValue(var));
			}
			return list;
		} finally {
			result.close();
		}
	}

}
