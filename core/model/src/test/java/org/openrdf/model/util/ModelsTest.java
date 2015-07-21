/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Unit tests on {@link Models} utility methods.
 *
 * @author Jeen Broekstra
 */
public class ModelsTest {

	private Model model1;

	private Model model2;

	private static ValueFactory VF = ValueFactoryImpl.getInstance();

	private URI foo;

	private URI bar;

	@Before
	public void setUp() {
		model1 = new LinkedHashModel();
		model2 = new LinkedHashModel();

		foo = VF.createURI("http://example.org/foo");
		bar = VF.createURI("http://example.org/bar");
	}

	@Test
	public void testModelsIsomorphic() {

		// two identical statements, no bnodes
		model1.add(foo, RDF.TYPE, bar);

		assertFalse(Models.isomorphic(model1, model2));

		model2.add(foo, RDF.TYPE, bar);

		assertTrue(Models.isomorphic(model1, model2));

		// add same statement again
		model2.add(foo, RDF.TYPE, bar);

		assertTrue("Duplicate statement should not be considered", Models.isomorphic(model1, model2));

		// two identical statements with bnodes added.
		model1.add(foo, RDF.TYPE, VF.createBNode());
		model2.add(foo, RDF.TYPE, VF.createBNode());

		assertTrue(Models.isomorphic(model1, model2));

		// chained bnodes
		BNode chainedNode1 = VF.createBNode();

		model1.add(bar, RDFS.SUBCLASSOF, chainedNode1);
		model1.add(chainedNode1, RDFS.SUBCLASSOF, foo);

		BNode chainedNode2 = VF.createBNode();

		model2.add(bar, RDFS.SUBCLASSOF, chainedNode2);
		model2.add(chainedNode2, RDFS.SUBCLASSOF, foo);

		assertTrue(Models.isomorphic(model1, model2));

		// two bnode statements with non-identical predicates

		model1.add(foo, foo, VF.createBNode());
		model2.add(foo, bar, VF.createBNode());

		assertFalse(Models.isomorphic(model1, model2));

	}

	@Test
	public void testIsSubset() {

		// two empty sets
		assertTrue(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		// two identical statements, no bnodes
		model1.add(foo, RDF.TYPE, bar);

		assertFalse(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		model2.add(foo, RDF.TYPE, bar);

		assertTrue(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		// two identical statements with bnodes added.
		model1.add(foo, RDF.TYPE, VF.createBNode());

		assertFalse(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		model2.add(foo, RDF.TYPE, VF.createBNode());

		assertTrue(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		// chained bnodes
		BNode chainedNode1 = VF.createBNode();

		model1.add(bar, RDFS.SUBCLASSOF, chainedNode1);
		model1.add(chainedNode1, RDFS.SUBCLASSOF, foo);

		assertFalse(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		BNode chainedNode2 = VF.createBNode();

		model2.add(bar, RDFS.SUBCLASSOF, chainedNode2);
		model2.add(chainedNode2, RDFS.SUBCLASSOF, foo);

		assertTrue(Models.isSubset(model1, model2));
		assertTrue(Models.isSubset(model2, model1));

		// two bnode statements with non-identical predicates

		model1.add(foo, foo, VF.createBNode());
		model2.add(foo, bar, VF.createBNode());

		assertFalse(Models.isSubset(model1, model2));
		assertFalse(Models.isSubset(model2, model1));
	}

	@Test
	public void testAnyObject() {
		Literal lit = VF.createLiteral(1.0);
		model1.add(foo, bar, lit);
		model1.add(foo, bar, foo);

		try {
			model1.objectValue();
			fail("Expected ModelException");
		}
		catch (ModelException e) {
			// fall through, expected
		}

		Value result = Models.anyObject(model1);
		assertNotNull(result);
		assertTrue(result.equals(lit) || result.equals(foo));
	}

	@Test
	public void testAnyObjectURI() {
		Literal lit = VF.createLiteral(1.0);
		model1.add(foo, bar, lit);
		model1.add(foo, bar, foo);

		try {
			model1.objectURI();
			fail("Expected ModelException");
		}
		catch (ModelException e) {
			// fall through, expected
		}

		Value result = Models.anyObjectURI(model1);
		assertNotNull(result);
		assertEquals(foo, result);
	}

	@Test
	public void testAnyObjectLiteral() {
		Literal lit = VF.createLiteral(1.0);
		model1.add(foo, bar, lit);
		model1.add(foo, bar, foo);

		try {
			model1.objectLiteral();
			fail("Expected ModelException");
		}
		catch (ModelException e) {
			// fall through, expected
		}

		Value result = Models.anyObjectLiteral(model1);
		assertNotNull(result);
		assertEquals(lit, result);
	}

	@Test
	public void testAnyPredicate() {
		model1.add(foo, bar, foo);
		model1.add(foo, foo, foo);

		URI result = Models.anyPredicate(model1);
		assertNotNull(result);
		assertTrue(result.equals(bar) || result.equals(foo));
	}

	@Test
	public void testAnySubject() {
		model1.add(foo, bar, foo);
		model1.add(foo, foo, foo);
		model1.add(bar, foo, foo);

		Resource result = Models.anySubject(model1);
		assertNotNull(result);
		assertTrue(result.equals(bar) || result.equals(foo));
	}

	@Test
	public void testSetProperty() {
		Literal lit1 = VF.createLiteral(1.0);
		model1.add(foo, bar, lit1);
		model1.add(foo, bar, foo);

		Literal lit2 = VF.createLiteral(2.0);

		Model m = Models.setProperty(model1, foo, bar, lit2);

		assertNotNull(m);
		assertEquals(model1, m);
		assertFalse(model1.contains(foo, bar, lit1));
		assertFalse(model1.contains(foo, bar, foo));
		assertTrue(model1.contains(foo, bar, lit2));

	}

	@Test
	public void testSetPropertyWithContext1() {
		Literal lit1 = VF.createLiteral(1.0);
		URI graph1 = VF.createURI("urn:g1");
		URI graph2 = VF.createURI("urn:g2");
		model1.add(foo, bar, lit1, graph1);
		model1.add(foo, bar, bar);
		model1.add(foo, bar, foo, graph2);

		Literal lit2 = VF.createLiteral(2.0);

		Model m = Models.setProperty(model1, foo, bar, lit2, graph2);

		assertNotNull(m);
		assertEquals(model1, m);
		assertTrue(model1.contains(foo, bar, lit1));
		assertFalse(model1.contains(foo, bar, foo));
		assertTrue(model1.contains(foo, bar, bar));
		assertFalse(model1.contains(foo, bar, foo, graph2));
		assertTrue(model1.contains(foo, bar, lit2, graph2));
		assertTrue(model1.contains(foo, bar, lit2));
	}

	@Test
	public void testSetPropertyWithContext2() {
		Literal lit1 = VF.createLiteral(1.0);
		URI graph1 = VF.createURI("urn:g1");
		URI graph2 = VF.createURI("urn:g2");
		model1.add(foo, bar, lit1, graph1);
		model1.add(foo, bar, bar);
		model1.add(foo, bar, foo, graph2);

		Literal lit2 = VF.createLiteral(2.0);

		Model m = Models.setProperty(model1, foo, bar, lit2);

		assertNotNull(m);
		assertEquals(model1, m);
		assertFalse(model1.contains(foo, bar, lit1));
		assertFalse(model1.contains(foo, bar, lit1, graph1));
		assertFalse(model1.contains(foo, bar, foo));
		assertFalse(model1.contains(foo, bar, bar));
		assertFalse(model1.contains(foo, bar, foo, graph2));
		assertTrue(model1.contains(foo, bar, lit2));
	}
}
