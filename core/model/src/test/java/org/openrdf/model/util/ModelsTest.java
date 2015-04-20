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

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Unit tests on {@link Models} utility methods.
 * 
 * @author Jeen Broekstra
 */
public class ModelsTest extends TestCase {

	private Model model1;

	private Model model2;

	private static ValueFactory VF = SimpleValueFactory.getInstance();

	private IRI foo;

	private IRI bar;
	
	private BNode baz;

	@Override
	protected void setUp() {
		model1 = new LinkedHashModel();
		model2 = new LinkedHashModel();

		foo = VF.createIRI("http://example.org/foo");
		bar = VF.createIRI("http://example.org/bar");
		baz = VF.createBNode();
	}

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

	public void testObject() {
		Literal lit = VF.createLiteral(1.0);
		model1.add(foo, bar, lit);
		model1.add(foo, bar, foo);

		Value result = Models.object(model1).orElse(null);
		assertNotNull(result);
		assertTrue(result.equals(lit) || result.equals(foo));
	}

	public void testObjectURI() {
		Literal lit = VF.createLiteral(1.0);
		model1.add(foo, bar, lit);
		model1.add(foo, bar, foo);

		Value result = Models.objectURI(model1).orElse(null);
		assertNotNull(result);
		assertEquals(foo, result);
	}

	public void testObjectLiteral() {
		Literal lit = VF.createLiteral(1.0);
		model1.add(foo, bar, lit);
		model1.add(foo, bar, foo);
		
		Value result = Models.objectLiteral(model1).orElse(null);
		assertNotNull(result);
		assertEquals(lit, result);
	}
	
	public void testPredicate() {
		model1.add(foo, bar, foo);
		model1.add(foo, foo, foo);

		IRI result = Models.predicate(model1).orElse(null);
		assertNotNull(result);
		assertTrue(result.equals(bar) || result.equals(foo));
	}

	public void testSubject() {
		model1.add(foo, bar, foo);
		model1.add(foo, foo, foo);
		model1.add(bar, foo, foo);
		model1.add(baz, foo, foo);

		Resource result = Models.subject(model1).orElse(null);
		assertNotNull(result);
		assertTrue(result.equals(bar) || result.equals(foo) || result.equals(baz));
	}
	
	public void testSubjectURI() {
		model1.add(foo, bar, foo);
		model1.add(foo, foo, foo);
		model1.add(baz, foo, foo);
		model1.add(bar, foo, foo);

		Resource result = Models.subjectURI(model1).orElse(null);
		assertNotNull(result);
		assertTrue(result.equals(bar) || result.equals(foo));
	}
	
	public void testSubjectBNode() {
		model1.add(foo, bar, foo);
		model1.add(foo, foo, foo);
		model1.add(baz, foo, foo);
		model1.add(bar, foo, foo);

		Resource result = Models.subjectBNode(model1).orElse(null);
		assertNotNull(result);
		assertTrue(result.equals(baz));
	}

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

	public void testSetPropertyWithContext1() {
		Literal lit1 = VF.createLiteral(1.0);
		IRI graph1 = VF.createIRI("urn:g1");
		IRI graph2 = VF.createIRI("urn:g2");
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

	public void testSetPropertyWithContext2() {
		Literal lit1 = VF.createLiteral(1.0);
		IRI graph1 = VF.createIRI("urn:g1");
		IRI graph2 = VF.createIRI("urn:g2");
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
