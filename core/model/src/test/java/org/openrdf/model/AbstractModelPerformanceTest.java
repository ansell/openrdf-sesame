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
package org.openrdf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.util.ModelException;

/**
 * @author Peter Ansell
 */
public abstract class AbstractModelPerformanceTest extends AbstractModelTest {

	private static final int COUNT = 150;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		super.setUp();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#add(org.openrdf.model.Resource, org.openrdf.model.IRI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfAddResourceURIValueResourceArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#getNamespaces()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfGetNamespaces() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#getNamespace(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfGetNamespace() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#setNamespace(java.lang.String, java.lang.String)}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfSetNamespaceStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#setNamespace(org.openrdf.model.Namespace)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfSetNamespaceNamespace() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#removeNamespace(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfRemoveNamespace() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#contains(org.openrdf.model.Resource, org.openrdf.model.IRI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfContainsResourceURIValueResourceArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#clear(org.openrdf.model.Resource[])}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfClearResourceArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#remove(org.openrdf.model.Resource, org.openrdf.model.IRI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfRemoveResourceURIValueResourceArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#filter(org.openrdf.model.Resource, org.openrdf.model.IRI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfFilter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#subjects()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfSubjects() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#predicates()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfPredicates() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objects()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfObjects() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#contexts()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfContexts() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfObjectValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfObjectLiteral() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfObjectResource() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectIRI()}.
	 */
	@Test
	public final void testPerfObjectURISingle() {
		Model model = getNewEmptyModel();
		for (int i = 0; i < COUNT; i++) {
			// Add many statements with the same object URI
			model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:" + i % 10000),
					vf.createIRI("urn:test:uri:predicate:" + (i % 200)),
					vf.createIRI("urn:test:object:uri:single")));
		}

		long start = System.nanoTime();
		Optional<IRI> objectURI = model.objectIRI();
		System.out.println("testPerfObjectURISingle: " + (System.nanoTime() - start));
		assertTrue(objectURI.isPresent());
		assertEquals("urn:test:object:uri:single", objectURI.get().toString());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectIRI()}.
	 */
	@Test
	public final void testPerfObjectURIMultipleAddedFirst() {
		Model model = getNewEmptyModel();
		model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:1"),
				vf.createIRI("urn:test:uri:predicate:1"), vf.createIRI("urn:test:object:uri:other")));
		for (int i = 0; i < COUNT; i++) {
			// Add many statements with the same object URI
			model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:" + i % 10000),
					vf.createIRI("urn:test:uri:predicate:" + (i % 200)),
					vf.createIRI("urn:test:object:uri:single")));
		}

		thrown.expect(ModelException.class);
		long start = System.nanoTime();
		try {
			model.objectIRI();
		}
		finally {
			System.out.println("testPerfObjectURIMultipleAddedFirst: " + (System.nanoTime() - start));
		}
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectIRI()}.
	 */
	@Test
	public final void testPerfObjectURIMultipleAddedLast() {
		Model model = getNewEmptyModel();
		for (int i = 0; i < COUNT; i++) {
			// Add many statements with the same object URI
			model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:" + i % 10000),
					vf.createIRI("urn:test:uri:predicate:" + (i % 200)),
					vf.createIRI("urn:test:object:uri:single")));
		}
		model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:1"),
				vf.createIRI("urn:test:uri:predicate:1"), vf.createIRI("urn:test:object:uri:other")));

		thrown.expect(ModelException.class);
		long start = System.nanoTime();
		try {
			model.objectIRI();
		}
		finally {
			System.out.println("testPerfObjectURIMultipleAddedLast: " + (System.nanoTime() - start));
		}
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfObjectString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfAnObjectLiteral() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfAnObjectResource() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectURI()}.
	 */
	@Test
	public final void testPerfAnObjectURIMultipleAddedFirst() {
		Model model = getNewEmptyModel();
		model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:1"),
				vf.createIRI("urn:test:uri:predicate:1"), vf.createIRI("urn:test:object:uri:other")));
		for (int i = 0; i < COUNT; i++) {
			// Add many statements with the same object URI
			model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:" + i % 10000),
					vf.createIRI("urn:test:uri:predicate:" + (i % 200)),
					vf.createIRI("urn:test:object:uri:single")));
		}

		thrown.expect(ModelException.class);
		long start = System.nanoTime();
		try {
			model.objectURI();
		}
		finally {
			System.out.println("testPerfAnObjectURIMultipleAddedFirst: " + (System.nanoTime() - start));
		}
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectURI()}.
	 */
	@Test
	public final void testPerfAnObjectURIMultipleAddedLast() {
		Model model = getNewEmptyModel();
		for (int i = 0; i < COUNT; i++) {
			// Add many statements with the same object URI
			model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:" + i % 10000),
					vf.createIRI("urn:test:uri:predicate:" + (i % 200)),
					vf.createIRI("urn:test:object:uri:single")));
		}
		model.add(vf.createStatement(vf.createIRI("urn:test:uri:subject:1"),
				vf.createIRI("urn:test:uri:predicate:1"), vf.createIRI("urn:test:object:uri:other")));

		thrown.expect(ModelException.class);
		long start = System.nanoTime();
		try {
			model.objectURI();
		}
		finally {
			System.out.println("testPerfAnObjectURIMultipleAddedLast: " + (System.nanoTime() - start));
		}
	}

	/**
	 * Test method for {@link java.util.Set#iterator()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfIterator() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#size()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfSize() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#isEmpty()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfIsEmpty() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#contains(java.lang.Object)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfContainsObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#add(java.lang.Object)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfAddE() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#remove(java.lang.Object)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfRemoveObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#containsAll(java.util.Collection)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfContainsAll() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#addAll(java.util.Collection)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfAddAll() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#removeAll(java.util.Collection)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfRemoveAll() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#retainAll(java.util.Collection)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfRetainAll() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.util.Set#clear()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfClear() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link java.util.Collection#removeIf(java.util.function.Predicate)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPerfRemoveIf() {
		fail("Not yet implemented"); // TODO
	}

}
