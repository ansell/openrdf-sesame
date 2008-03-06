/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * @author jeen
 */
public class RepositoryUtilTest extends TestCase {

	private Collection<Statement> model1;

	private Collection<Statement> model2;

	private static ValueFactory VF = new ValueFactoryImpl();

	private URI foo;

	private URI bar;

	protected void setUp() {
		model1 = new HashSet<Statement>();
		model2 = new HashSet<Statement>();

		foo = VF.createURI("http://example.org/foo");
		bar = VF.createURI("http://example.org/bar");
	}

	public void testModelsEqual() {

		// two identical statements, no bnodes
		model1.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertFalse(RepositoryUtil.modelsEqual(model1, model2));

		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertTrue(RepositoryUtil.modelsEqual(model1, model2));

		// add same statement again
		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertTrue("Duplicate statement should not be considered", RepositoryUtil.modelsEqual(model1, model2));

		// two identical statements with bnodes added.
		model1.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));
		model2.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));

		assertTrue(RepositoryUtil.modelsEqual(model1, model2));

		// chained bnodes
		BNode chainedNode1 = VF.createBNode();

		model1.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode1));
		model1.add(VF.createStatement(chainedNode1, RDFS.SUBCLASSOF, foo));

		BNode chainedNode2 = VF.createBNode();

		model2.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode2));
		model2.add(VF.createStatement(chainedNode2, RDFS.SUBCLASSOF, foo));

		assertTrue(RepositoryUtil.modelsEqual(model1, model2));

		// two bnode statements with non-identical predicates
		
		model1.add(VF.createStatement(foo, foo, VF.createBNode()));
		model2.add(VF.createStatement(foo, bar, VF.createBNode()));

		assertFalse(RepositoryUtil.modelsEqual(model1, model2));
		
	}

	public void testIsSubset() {

		// two empty sets
		assertTrue(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));
		
		// two identical statements, no bnodes
		model1.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertFalse(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));

		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertTrue(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));

		// two identical statements with bnodes added.
		model1.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));
		
		assertFalse(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));
		
		model2.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));

		assertTrue(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));

		// chained bnodes
		BNode chainedNode1 = VF.createBNode();

		model1.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode1));
		model1.add(VF.createStatement(chainedNode1, RDFS.SUBCLASSOF, foo));
		
		assertFalse(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));

		BNode chainedNode2 = VF.createBNode();

		model2.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode2));
		model2.add(VF.createStatement(chainedNode2, RDFS.SUBCLASSOF, foo));

		assertTrue(RepositoryUtil.isSubset(model1, model2));
		assertTrue(RepositoryUtil.isSubset(model2, model1));

		// two bnode statements with non-identical predicates
		
		model1.add(VF.createStatement(foo, foo, VF.createBNode()));
		model2.add(VF.createStatement(foo, bar, VF.createBNode()));

		assertFalse(RepositoryUtil.isSubset(model1, model2));
		assertFalse(RepositoryUtil.isSubset(model2, model1));
	}

	public void testDifference() {

		// the difference of two empty sets is the empty set.
		Collection<? extends Statement> diff = RepositoryUtil.difference(model1, model2);
		
		assertTrue(diff != null);
		assertTrue(diff.size() == 0);
		
		model1.add(VF.createStatement(foo, RDF.TYPE, bar));
		
		diff = RepositoryUtil.difference(model1, model2);
		
		assertTrue(diff.size() == 1);

		for (Statement s: diff) {
			assertTrue(s.getSubject().equals(foo));
			assertTrue(s.getPredicate().equals(RDF.TYPE));
			assertTrue(s.getObject().equals(bar));
		}
		
		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		diff = RepositoryUtil.difference(model1, model2);
		
		assertTrue(diff.size() == 0);

	}

	public void testAsGraph() {

	}
}
