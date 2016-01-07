package org.openrdf.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;

public class StatementsTest {

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	@Test
	public void testMultipleContexts() {
		Resource c1 = vf.createIRI("urn:c1");
		Resource c2 = vf.createIRI("urn:c1");
		Resource c3 = vf.createIRI("urn:c1");

		Model m = Statements.create(vf, FOAF.AGE, RDF.TYPE, RDF.PROPERTY, new TreeModel(), c1, c2, null, c3);
		assertFalse(m.isEmpty());
		assertTrue(m.contains(FOAF.AGE, RDF.TYPE, RDF.PROPERTY, (Resource)null));
		assertTrue(m.contains(FOAF.AGE, RDF.TYPE, RDF.PROPERTY, c1));
		assertTrue(m.contains(FOAF.AGE, RDF.TYPE, RDF.PROPERTY, c2));
		assertTrue(m.contains(FOAF.AGE, RDF.TYPE, RDF.PROPERTY, c3));
	}

	@Test
	public void testNoContext() {
		Model m = Statements.create(vf, FOAF.AGE, RDF.TYPE, RDF.PROPERTY, new TreeModel());
		assertFalse(m.isEmpty());
		assertTrue(m.contains(FOAF.AGE, RDF.TYPE, RDF.PROPERTY));
	}

	@Test
	public void testInvalidInput() {
		try {
			Statements.consume(vf, FOAF.AGE, RDF.TYPE, RDF.PROPERTY,
					st -> fail("should have resulted in Exception"), null);
		}
		catch (IllegalArgumentException e) {
			// fall through.
		}

		try {
			Statements.consume(vf, null, RDF.TYPE, RDF.PROPERTY, st -> fail("should have resulted in Exception"));
		}
		catch (NullPointerException e) {
			// fall through.
		}
	}
}
