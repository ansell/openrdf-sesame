package org.openrdf.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.RDF;

public class RDFCollectionsTest {

	private final static ValueFactory vf = SimpleValueFactory.getInstance();

	private final List<Value> values = new ArrayList<>();

	private Literal a;

	private Literal b;

	private Literal c;

	@Before
	public void setUp()
		throws Exception
	{
		a = Literals.createLiteral(vf, "A");
		b = Literals.createLiteral(vf, "B");
		c = Literals.createLiteral(vf, "C");

		values.add(a);
		values.add(b);
		values.add(c);
	}

	@Test
	public void testConversionRoundtrip() {
		IRI head = vf.createIRI("urn:head");
		Model m = RDFCollections.asRDF(values, head, new TreeModel());
		assertNotNull(m);
		assertTrue(m.contains(head, RDF.FIRST, a));
		assertFalse(m.contains(null, RDF.REST, head));

		List<Value> newList = RDFCollections.asValues(m, head, new ArrayList<Value>());
		assertNotNull(newList);
		assertTrue(newList.contains(a));
		assertTrue(newList.contains(b));
		assertTrue(newList.contains(c));

	}

	public void testNonWellformedCollection() {
		Resource head = vf.createBNode();
		Model m = RDFCollections.asRDF(values, head, new TreeModel());
		m.remove(null, RDF.REST, RDF.NIL);
		try {
			RDFCollections.asValues(m, head, new ArrayList<Value>());
			fail("collection missing terminator should result in error");
		}
		catch (ModelException e) {
			// fall through, expected
		}

		m = RDFCollections.asRDF(values, head, new TreeModel());
		m.add(head, RDF.REST, head);

		try {
			RDFCollections.asValues(m, head, new ArrayList<Value>());
			fail("collection with cycle should result in error");
		}
		catch (ModelException e) {
			// fall through, expected
		}

		// supply incorrect head node
		try {
			RDFCollections.asValues(m, vf.createBNode(), new ArrayList<Value>());
			fail("resource that is not a collection should result in error");
		}
		catch (ModelException e) {
			// fall through, expected
		}

	}
}
