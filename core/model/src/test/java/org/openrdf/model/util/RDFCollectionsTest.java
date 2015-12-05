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
		Model m = RDFCollections.asRDFCollection(values, new TreeModel());
		assertNotNull(m);

		Optional<Statement> stmt = m.filter(null, RDF.FIRST, null).stream().filter(
				st -> !m.contains(null, RDF.REST, st.getSubject())).findAny();

		assertTrue(stmt.isPresent());

		assertTrue(m.contains(stmt.get().getSubject(), RDF.FIRST, a));

		// test round trip

		List<Value> newList = RDFCollections.readCollection(m, new ArrayList<Value>());
		assertNotNull(newList);
		assertTrue(newList.contains(a));
		assertTrue(newList.contains(b));
		assertTrue(newList.contains(c));
	}

	@Test
	public void testConversionWithHeadNode() {
		IRI head = vf.createIRI("urn:head");
		Model m = RDFCollections.asRDFCollection(values, head, new TreeModel());
		assertNotNull(m);
		assertTrue(m.contains(head, RDF.FIRST, a));
		assertFalse(m.contains(null, RDF.REST, head));

		List<Value> newList = RDFCollections.readCollection(m, head, new ArrayList<Value>());
		assertNotNull(newList);
		assertTrue(newList.contains(a));
		assertTrue(newList.contains(b));
		assertTrue(newList.contains(c));

		newList = RDFCollections.readCollection(m, new ArrayList<Value>());
		assertNotNull(newList);
		assertTrue(newList.contains(a));
		assertTrue(newList.contains(b));
		assertTrue(newList.contains(c));

		// supply incorrect head node = empty result
		newList = RDFCollections.readCollection(m, vf.createBNode(), new ArrayList<Value>());
		assertNotNull(newList);
		assertTrue(newList.isEmpty());
		
		
	}

	public void testNonWellformedCollection() {
		Model m = RDFCollections.asRDFCollection(values, new TreeModel());
		m.remove(null, RDF.REST, RDF.NIL);
		try {
			RDFCollections.readCollection(m, new ArrayList<Value>());
			fail("collection missing terminator should result in error");
		}
		catch (ModelException e) {
			// fall through, expected
		}
		
		Resource head = vf.createIRI("urn:head");
		
		m = RDFCollections.asRDFCollection(values, head, new TreeModel());
		m.add(head, RDF.REST, head);
		
		try {
			RDFCollections.readCollection(m, new ArrayList<Value>());
			fail("collection with cycle should result in error");
		}
		catch (ModelException e) {
			// fall through, expected
		}
	}
}
