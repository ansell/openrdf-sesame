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
package org.openrdf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.SKOS;

/**
 * An abstract test class to test the handling of namespaces by {@link Model}
 * implementations.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class ModelNamespacesTest {

	private Model testModel;

	/**
	 * Implementing tests must return a new, empty, Model for each call to this
	 * method.
	 * 
	 * @return A new empty implementation of {@link Model} that implements the
	 *         namespace related methods, {@link Model#getNamespace(String)},
	 *         {@link Model#getNamespaces()},
	 *         {@link Model#setNamespace(Namespace)},
	 *         {@link Model#setNamespace(String, String)}, and
	 *         {@link Model#removeNamespace(String)}.
	 */
	protected abstract Model getModelImplementation();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		testModel = getModelImplementation();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		testModel = null;
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#getNamespaces()}.
	 */
	@Test
	public final void testGetNamespacesEmpty() {
		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertTrue("Namespaces must initially be empty", namespaces.isEmpty());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#getNamespaces()}.
	 */
	@Test
	public final void testGetNamespacesSingle() {
		testModel.setNamespace(RDF.PREFIX, RDF.NAMESPACE);

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(1, namespaces.size());

		assertTrue("Did not find the expected namespace in the set",
				namespaces.contains(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE)));
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#getNamespaces()}.
	 */
	@Test
	public final void testGetNamespacesMultiple() {
		testModel.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
		testModel.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
		testModel.setNamespace(DC.PREFIX, DC.NAMESPACE);
		testModel.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
		testModel.setNamespace(SESAME.PREFIX, SESAME.NAMESPACE);

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(5, namespaces.size());

		assertTrue(namespaces.contains(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE)));
		assertTrue(namespaces.contains(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE)));
		assertTrue(namespaces.contains(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE)));
		assertTrue(namespaces.contains(new SimpleNamespace(SKOS.PREFIX, SKOS.NAMESPACE)));
		assertTrue(namespaces.contains(new SimpleNamespace(SESAME.PREFIX, SESAME.NAMESPACE)));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#getNamespace(java.lang.String)}.
	 */
	@Test
	public final void testGetNamespaceEmpty() {
		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertTrue("Namespaces must initially be empty", namespaces.isEmpty());

		assertFalse(testModel.getNamespace(RDF.PREFIX).isPresent());
		assertFalse(testModel.getNamespace(RDFS.PREFIX).isPresent());
		assertFalse(testModel.getNamespace(DC.PREFIX).isPresent());
		assertFalse(testModel.getNamespace(SKOS.PREFIX).isPresent());
		assertFalse(testModel.getNamespace(SESAME.PREFIX).isPresent());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#getNamespace(java.lang.String)}.
	 */
	@Test
	public final void testGetNamespaceSingle() {
		testModel.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(1, namespaces.size());

		assertTrue("Did not find the expected namespace in the set",
				namespaces.contains(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE)));

		assertFalse(testModel.getNamespace(RDF.PREFIX).isPresent());
		assertEquals(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE), testModel.getNamespace(RDFS.PREFIX).get());
		assertFalse(testModel.getNamespace(DC.PREFIX).isPresent());
		assertFalse(testModel.getNamespace(SKOS.PREFIX).isPresent());
		assertFalse(testModel.getNamespace(SESAME.PREFIX).isPresent());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#getNamespace(java.lang.String)}.
	 */
	@Test
	public final void testGetNamespaceMultiple() {
		testModel.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
		testModel.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
		testModel.setNamespace(DC.PREFIX, DC.NAMESPACE);
		testModel.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
		testModel.setNamespace(SESAME.PREFIX, SESAME.NAMESPACE);

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(5, namespaces.size());

		assertEquals(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE), testModel.getNamespace(RDF.PREFIX).get());
		assertEquals(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE), testModel.getNamespace(RDFS.PREFIX).get());
		assertEquals(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE), testModel.getNamespace(DC.PREFIX).get());
		assertEquals(new SimpleNamespace(SKOS.PREFIX, SKOS.NAMESPACE), testModel.getNamespace(SKOS.PREFIX).get());
		assertEquals(new SimpleNamespace(SESAME.PREFIX, SESAME.NAMESPACE),
				testModel.getNamespace(SESAME.PREFIX).get());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#setNamespace(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testSetNamespaceSamePrefix() {
		testModel.setNamespace("r", RDF.NAMESPACE);
		testModel.setNamespace("r", RDFS.NAMESPACE);
		
		Set<Namespace> namespaces = testModel.getNamespaces();
		
		assertNotNull("Namespaces set must not be null", namespaces);
		assertEquals(1, namespaces.size());
		
		assertEquals(new SimpleNamespace("r", RDFS.NAMESPACE), testModel.getNamespace("r").orElse(null));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#setNamespace(org.openrdf.model.Namespace)}.
	 */
	@Test
	public final void testSetNamespaceNamespace() {
		testModel.setNamespace(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(SKOS.PREFIX, SKOS.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(SESAME.PREFIX, SESAME.NAMESPACE));

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(5, namespaces.size());

		assertEquals(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE), testModel.getNamespace(RDF.PREFIX).get());
		assertEquals(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE), testModel.getNamespace(RDFS.PREFIX).get());
		assertEquals(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE), testModel.getNamespace(DC.PREFIX).get());
		assertEquals(new SimpleNamespace(SKOS.PREFIX, SKOS.NAMESPACE), testModel.getNamespace(SKOS.PREFIX).get());
		assertEquals(new SimpleNamespace(SESAME.PREFIX, SESAME.NAMESPACE),
				testModel.getNamespace(SESAME.PREFIX).get());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#setNamespace(org.openrdf.model.Namespace)}.
	 */
	@Test
	public final void testSetNamespaceNamespaceSamePrefix() {
		testModel.setNamespace(new SimpleNamespace("r", RDF.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace("r", RDFS.NAMESPACE));
		
		Set<Namespace> namespaces = testModel.getNamespaces();
		
		assertNotNull("Namespaces set must not be null", namespaces);
		assertEquals(1, namespaces.size());
		
		assertEquals(new SimpleNamespace("r", RDFS.NAMESPACE), testModel.getNamespace("r").orElse(null));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#removeNamespace(java.lang.String)}.
	 */
	@Test
	public final void testRemoveNamespaceEmpty() {
		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertTrue("Namespaces must initially be empty", namespaces.isEmpty());

		assertFalse(testModel.removeNamespace(RDF.NAMESPACE).isPresent());
		assertFalse(testModel.removeNamespace(RDFS.NAMESPACE).isPresent());
		assertFalse(testModel.removeNamespace(DC.NAMESPACE).isPresent());
		assertFalse(testModel.removeNamespace(SKOS.NAMESPACE).isPresent());
		assertFalse(testModel.removeNamespace(SESAME.NAMESPACE).isPresent());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#removeNamespace(java.lang.String)}.
	 */
	@Test
	public final void testRemoveNamespaceSingle() {
		testModel.setNamespace(DC.PREFIX, DC.NAMESPACE);

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(1, namespaces.size());

		assertTrue("Did not find the expected namespace in the set",
				namespaces.contains(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE)));

		assertFalse(testModel.removeNamespace(RDF.NAMESPACE).isPresent());
		assertFalse(testModel.removeNamespace(RDFS.NAMESPACE).isPresent());
		assertEquals(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE), testModel.removeNamespace(DC.PREFIX).get());
		assertFalse(testModel.removeNamespace(SKOS.NAMESPACE).isPresent());
		assertFalse(testModel.removeNamespace(SESAME.NAMESPACE).isPresent());

		Set<Namespace> namespacesAfter = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespacesAfter);
		assertTrue("Namespaces must now be empty", namespacesAfter.isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#removeNamespace(java.lang.String)}.
	 */
	@Test
	public final void testRemoveNamespaceMultiple() {
		testModel.setNamespace(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(SKOS.PREFIX, SKOS.NAMESPACE));
		testModel.setNamespace(new SimpleNamespace(SESAME.PREFIX, SESAME.NAMESPACE));

		Set<Namespace> namespaces = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespaces);
		assertFalse(namespaces.isEmpty());
		assertEquals(5, namespaces.size());

		assertEquals(new SimpleNamespace(RDF.PREFIX, RDF.NAMESPACE), testModel.removeNamespace(RDF.PREFIX).get());
		assertEquals(new SimpleNamespace(RDFS.PREFIX, RDFS.NAMESPACE),
				testModel.removeNamespace(RDFS.PREFIX).get());
		assertEquals(new SimpleNamespace(DC.PREFIX, DC.NAMESPACE), testModel.removeNamespace(DC.PREFIX).get());
		assertEquals(new SimpleNamespace(SKOS.PREFIX, SKOS.NAMESPACE),
				testModel.removeNamespace(SKOS.PREFIX).get());
		assertEquals(new SimpleNamespace(SESAME.PREFIX, SESAME.NAMESPACE),
				testModel.removeNamespace(SESAME.PREFIX).get());

		Set<Namespace> namespacesAfter = testModel.getNamespaces();

		assertNotNull("Namespaces set must not be null", namespacesAfter);
		assertTrue("Namespaces must now be empty", namespacesAfter.isEmpty());
	}

}
