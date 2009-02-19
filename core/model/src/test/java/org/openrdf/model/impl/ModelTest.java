/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 * 
 * @author James Leigh
 */
public class ModelTest extends TestCase {
	private static final String NS = "http://example.org/";
	private ValueFactory vf = new ValueFactoryImpl();
	private URI painter = vf.createURI(NS, "Painter");
	private URI paints = vf.createURI(NS, "paints");
	private URI painting = vf.createURI(NS, "Painting");
	private URI picasso = vf.createURI(NS, "picasso");
	private URI guernica = vf.createURI(NS, "guernica");
	private URI rembrandt = vf.createURI(NS, "rembrandt");
	private URI nightwatch = vf.createURI(NS, "nightwatch");
	private URI context1 = vf.createURI(NS, "context1");
	private URI context2 = vf.createURI(NS, "context2");
	private Statement stmt1 = new StatementImpl(picasso, paints, guernica, null);
	private Statement stmt2 = new StatementImpl(rembrandt, paints, nightwatch, null);
	private List<Statement> stmts = Arrays.asList(stmt1, stmt2);
	private Statement stc1 = new StatementImpl(picasso, paints, guernica, context1);
	private Statement stc2 = new StatementImpl(rembrandt, paints, nightwatch, context2);
	private List<Statement> stcs = Arrays.asList(stc1, stc2);
	private Model model = createModel();

	public Model createModel() {
		return new LinkedHashModel();
	}

	public void testEmpty() {
		assertTrue(model.isEmpty());
		assertEquals(0, model.size());
		assertFalse(model.iterator().hasNext());
		Resource[] contexts = {};
		assertFalse(model.filter(picasso, paints, guernica, contexts).iterator().hasNext());
		Resource[] contexts1 = {};
		assertFalse(model.filter(picasso, paints, null, contexts1).iterator().hasNext());
		Resource[] contexts2 = {};
		assertFalse(model.filter(picasso, null, guernica, contexts2).iterator().hasNext());
		Resource[] contexts3 = {};
		assertFalse(model.filter(picasso, null, null, contexts3).iterator().hasNext());
		Resource[] contexts4 = {};
		assertFalse(model.filter(null, paints, guernica, contexts4).iterator().hasNext());
		Resource[] contexts5 = {};
		assertFalse(model.filter(null, paints, null, contexts5).iterator().hasNext());
		Resource[] contexts6 = {};
		assertFalse(model.filter(null, null, guernica, contexts6).iterator().hasNext());
		Resource[] contexts7 = {};
		assertFalse(model.filter(null, null, null, contexts7).iterator().hasNext());
		assertFalse(model.contains(stmt1));
		assertFalse(model.remove(stmt1));
		assertFalse(model.containsAll(stmts));
		assertFalse(model.removeAll(stmts));
		assertFalse(model.retainAll(stmts));
		assertEquals(Collections.emptySet(), model);
	}

	public void testAddNew() {
		model.add(stmt1);
		assertFalse(model.isEmpty());
		assertEquals(1, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = {};
		assertTrue(model.filter(picasso, paints, guernica, contexts).iterator().hasNext());
		Resource[] contexts1 = {};
		assertTrue(model.filter(picasso, paints, null, contexts1).iterator().hasNext());
		Resource[] contexts2 = {};
		assertTrue(model.filter(picasso, null, guernica, contexts2).iterator().hasNext());
		Resource[] contexts3 = {};
		assertTrue(model.filter(picasso, null, null, contexts3).iterator().hasNext());
		Resource[] contexts4 = {};
		assertTrue(model.filter(null, paints, guernica, contexts4).iterator().hasNext());
		Resource[] contexts5 = {};
		assertTrue(model.filter(null, paints, null, contexts5).iterator().hasNext());
		Resource[] contexts6 = {};
		assertTrue(model.filter(null, null, guernica, contexts6).iterator().hasNext());
		Resource[] contexts7 = {};
		assertTrue(model.filter(null, null, null, contexts7).iterator().hasNext());
		assertTrue(model.contains(stmt1));
		assertFalse(model.containsAll(stmts));
		assertFalse(model.retainAll(stmts));
		assertEquals(Collections.singleton(stmt1), model);
		assertFalse(model.contains(stc1));
	}

	public void testAddExisting() {
		testAddNew();
		testAddNew();
	}

	public void testAddNewWithContext() {
		model.add(stc1);
		assertFalse(model.isEmpty());
		assertEquals(1, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = { context1 };
		assertTrue(model.filter(picasso, paints, guernica, contexts).iterator().hasNext());
		Resource[] contexts1 = {};
		assertTrue(model.filter(picasso, paints, guernica, contexts1).iterator().hasNext());
		Resource[] contexts2 = { context1 };
		assertTrue(model.filter(picasso, paints, null, contexts2).iterator().hasNext());
		Resource[] contexts3 = {};
		assertTrue(model.filter(picasso, paints, null, contexts3).iterator().hasNext());
		Resource[] contexts4 = { context1 };
		assertTrue(model.filter(picasso, null, guernica, contexts4).iterator().hasNext());
		Resource[] contexts5 = {};
		assertTrue(model.filter(picasso, null, guernica, contexts5).iterator().hasNext());
		Resource[] contexts6 = { context1 };
		assertTrue(model.filter(picasso, null, null, contexts6).iterator().hasNext());
		Resource[] contexts7 = {};
		assertTrue(model.filter(picasso, null, null, contexts7).iterator().hasNext());
		Resource[] contexts8 = { context1 };
		assertTrue(model.filter(null, paints, guernica, contexts8).iterator().hasNext());
		Resource[] contexts9 = {};
		assertTrue(model.filter(null, paints, guernica, contexts9).iterator().hasNext());
		Resource[] contexts10 = { context1 };
		assertTrue(model.filter(null, paints, null, contexts10).iterator().hasNext());
		Resource[] contexts11 = {};
		assertTrue(model.filter(null, paints, null, contexts11).iterator().hasNext());
		Resource[] contexts12 = { context1 };
		assertTrue(model.filter(null, null, guernica, contexts12).iterator().hasNext());
		Resource[] contexts13 = {};
		assertTrue(model.filter(null, null, guernica, contexts13).iterator().hasNext());
		Resource[] contexts14 = { context1 };
		assertTrue(model.filter(null, null, null, contexts14).iterator().hasNext());
		Resource[] contexts15 = {};
		assertTrue(model.filter(null, null, null, contexts15).iterator().hasNext());
		assertFalse(model.contains(stmt1));
		assertTrue(model.contains(stc1));
		assertFalse(model.containsAll(stcs));
		assertFalse(model.retainAll(stcs));
		assertEquals(Collections.singleton(stc1), model);
	}

	public void testAddExistingWithContext() {
		testAddNewWithContext();
		testAddNewWithContext();
	}

	public void testAddExistingWithAndWithoutContext() {
		testAddNew();
		model.add(stc1);
		assertFalse(model.isEmpty());
		assertEquals(2, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = {};
		assertTrue(model.filter(null, null, null, contexts).iterator().hasNext());
		assertTrue(model.contains(stmt1));
		assertFalse(model.containsAll(stmts));
		assertTrue(model.contains(stc1));
		assertFalse(model.containsAll(stcs));
	}

	public void testRemove() {
		testAddNew();
		assertTrue(model.remove(stmt1));
		testEmpty();
	}

	public void testRemoveAll() {
		testAddNew();
		assertTrue(model.removeAll(stmts));
		testEmpty();
	}

	public void testRemoveWithContext() {
		testAddNewWithContext();
		assertTrue(model.remove(stc1));
		testEmpty();
	}

	public void testRemoveAllWithContext() {
		testAddNewWithContext();
		assertTrue(model.removeAll(stcs));
		testEmpty();
	}

	public void testRemoveWithAndWithoutContextA() {
		testAddExistingWithAndWithoutContext();
		assertTrue(model.remove(stmt1));
		assertFalse(model.isEmpty());
		assertEquals(1, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = {};
		assertTrue(model.filter(null, null, null, contexts).iterator().hasNext());
		assertFalse(model.contains(stmt1));
		assertFalse(model.containsAll(stmts));
		assertTrue(model.contains(stc1));
		assertFalse(model.containsAll(stcs));
		assertFalse(model.retainAll(stcs));
		assertEquals(Collections.singleton(stc1), model);
		assertEquals(stc1, model.iterator().next());
	}

	public void testRemoveWithAndWithoutContextB() {
		testAddExistingWithAndWithoutContext();
		assertTrue(model.remove(stc1));
		assertFalse(model.isEmpty());
		assertEquals(1, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = {};
		assertTrue(model.filter(null, null, null, contexts).iterator().hasNext());
		assertTrue(model.contains(stmt1));
		assertFalse(model.containsAll(stmts));
		assertFalse(model.retainAll(stmts));
		assertEquals(Collections.singleton(stmt1), model);
		assertFalse(model.contains(stc1));
		assertFalse(model.containsAll(stcs));
		assertEquals(stmt1, model.iterator().next());
	}

	public void testRemoveAllWithAndWithoutContextA() {
		testAddExistingWithAndWithoutContext();
		assertTrue(model.removeAll(stmts));
		assertFalse(model.isEmpty());
		assertEquals(1, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = {};
		assertTrue(model.filter(null, null, null, contexts).iterator().hasNext());
		assertFalse(model.contains(stmt1));
		assertFalse(model.containsAll(stmts));
		assertTrue(model.contains(stc1));
		assertFalse(model.containsAll(stcs));
		assertFalse(model.retainAll(stcs));
		assertEquals(Collections.singleton(stc1), model);
	}

	public void testRemoveAllWithAndWithoutContextB() {
		testAddExistingWithAndWithoutContext();
		assertTrue(model.removeAll(stcs));
		assertFalse(model.isEmpty());
		assertEquals(1, model.size());
		assertTrue(model.iterator().hasNext());
		Resource[] contexts = {};
		assertTrue(model.filter(null, null, null, contexts).iterator().hasNext());
		assertTrue(model.contains(stmt1));
		assertFalse(model.containsAll(stmts));
		assertFalse(model.retainAll(stmts));
		assertEquals(Collections.singleton(stmt1), model);
		assertFalse(model.contains(stc1));
		assertFalse(model.containsAll(stcs));
	}
}
