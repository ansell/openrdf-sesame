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

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.SKOS;

/**
 * @author Peter Ansell
 */
public class NamespacesTest {

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Namespaces#asMap(java.util.Set)}.
	 */
	@Test
	public final void testAsMapEmpty() {
		Map<String, String> map = Namespaces.asMap(Collections.<Namespace> emptySet());

		assertTrue(map.isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Namespaces#asMap(java.util.Set)}.
	 */
	@Test
	public final void testAsMapOne() {
		Set<Namespace> input = new HashSet<Namespace>();
		input.add(new NamespaceImpl(RDF.PREFIX, RDF.NAMESPACE));

		Map<String, String> map = Namespaces.asMap(input);

		assertFalse(map.isEmpty());
		assertEquals(1, map.size());

		assertTrue(map.containsKey(RDF.PREFIX));
		assertEquals(RDF.NAMESPACE, map.get(RDF.PREFIX));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Namespaces#asMap(java.util.Set)}.
	 */
	@Test
	public final void testAsMapMultiple() {
		Set<Namespace> input = new HashSet<Namespace>();
		input.add(new NamespaceImpl(RDF.PREFIX, RDF.NAMESPACE));
		input.add(new NamespaceImpl(RDFS.PREFIX, RDFS.NAMESPACE));
		input.add(new NamespaceImpl(DC.PREFIX, DC.NAMESPACE));
		input.add(new NamespaceImpl(SKOS.PREFIX, SKOS.NAMESPACE));
		input.add(new NamespaceImpl(SESAME.PREFIX, SESAME.NAMESPACE));

		Map<String, String> map = Namespaces.asMap(input);

		assertFalse(map.isEmpty());
		assertEquals(5, map.size());

		assertTrue(map.containsKey(RDF.PREFIX));
		assertEquals(RDF.NAMESPACE, map.get(RDF.PREFIX));
		assertTrue(map.containsKey(RDFS.PREFIX));
		assertEquals(RDFS.NAMESPACE, map.get(RDFS.PREFIX));
		assertTrue(map.containsKey(DC.PREFIX));
		assertEquals(DC.NAMESPACE, map.get(DC.PREFIX));
		assertTrue(map.containsKey(SKOS.PREFIX));
		assertEquals(SKOS.NAMESPACE, map.get(SKOS.PREFIX));
		assertTrue(map.containsKey(SESAME.PREFIX));
		assertEquals(SESAME.NAMESPACE, map.get(SESAME.PREFIX));
	}

}
