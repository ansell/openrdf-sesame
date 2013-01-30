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
public class ModelUtilTest extends TestCase {

	private Collection<Statement> model1;

	private Collection<Statement> model2;

	private static ValueFactory VF = new ValueFactoryImpl();

	private URI foo;

	private URI bar;

	@Override
	protected void setUp() {
		model1 = new HashSet<Statement>();
		model2 = new HashSet<Statement>();

		foo = VF.createURI("http://example.org/foo");
		bar = VF.createURI("http://example.org/bar");
	}

	public void testModelsEqual() {

		// two identical statements, no bnodes
		model1.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertFalse(ModelUtil.equals(model1, model2));

		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertTrue(ModelUtil.equals(model1, model2));

		// add same statement again
		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertTrue("Duplicate statement should not be considered", ModelUtil.equals(model1, model2));

		// two identical statements with bnodes added.
		model1.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));
		model2.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));

		assertTrue(ModelUtil.equals(model1, model2));

		// chained bnodes
		BNode chainedNode1 = VF.createBNode();

		model1.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode1));
		model1.add(VF.createStatement(chainedNode1, RDFS.SUBCLASSOF, foo));

		BNode chainedNode2 = VF.createBNode();

		model2.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode2));
		model2.add(VF.createStatement(chainedNode2, RDFS.SUBCLASSOF, foo));

		assertTrue(ModelUtil.equals(model1, model2));

		// two bnode statements with non-identical predicates

		model1.add(VF.createStatement(foo, foo, VF.createBNode()));
		model2.add(VF.createStatement(foo, bar, VF.createBNode()));

		assertFalse(ModelUtil.equals(model1, model2));

	}

	public void testIsSubset() {

		// two empty sets
		assertTrue(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		// two identical statements, no bnodes
		model1.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertFalse(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		model2.add(VF.createStatement(foo, RDF.TYPE, bar));

		assertTrue(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		// two identical statements with bnodes added.
		model1.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));

		assertFalse(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		model2.add(VF.createStatement(foo, RDF.TYPE, VF.createBNode()));

		assertTrue(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		// chained bnodes
		BNode chainedNode1 = VF.createBNode();

		model1.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode1));
		model1.add(VF.createStatement(chainedNode1, RDFS.SUBCLASSOF, foo));

		assertFalse(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		BNode chainedNode2 = VF.createBNode();

		model2.add(VF.createStatement(bar, RDFS.SUBCLASSOF, chainedNode2));
		model2.add(VF.createStatement(chainedNode2, RDFS.SUBCLASSOF, foo));

		assertTrue(ModelUtil.isSubset(model1, model2));
		assertTrue(ModelUtil.isSubset(model2, model1));

		// two bnode statements with non-identical predicates

		model1.add(VF.createStatement(foo, foo, VF.createBNode()));
		model2.add(VF.createStatement(foo, bar, VF.createBNode()));

		assertFalse(ModelUtil.isSubset(model1, model2));
		assertFalse(ModelUtil.isSubset(model2, model1));
	}
}
