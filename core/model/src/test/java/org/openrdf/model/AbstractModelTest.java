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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Abstract test suite for the helper methods defined by the Model interface.
 *
 * @author Peter Ansell
 */
public abstract class AbstractModelTest {

	@Rule
	public Timeout timeout = new Timeout(10000);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	protected Literal literal1;

	protected Literal literal2;

	protected Literal literal3;

	protected URI uri1;

	protected URI uri2;

	protected URI uri3;

	protected BNode bnode1;

	protected BNode bnode2;

	protected BNode bnode3;

	protected final ValueFactory vf = ValueFactoryImpl.getInstance();

	protected abstract Model getNewModel();

	/**
	 * Helper method that asserts that the returned model is empty before
	 * returning.
	 * 
	 * @return An empty instance of the {@link Model} implementation being
	 *         tested.
	 */
	protected Model getNewEmptyModel() {
		Model model = getNewModel();
		assertTrue(model.isEmpty());
		return model;
	}

	protected Model getNewModelObjectSingleLiteral() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		assertEquals(1, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleURI() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, uri2);
		assertEquals(1, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, bnode1);
		assertEquals(1, model.size());
		return model;
	}

	protected Model getNewModelObjectDoubleLiteral() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, literal2);
		assertEquals(2, model.size());
		return model;
	}

	protected Model getNewModelObjectDoubleURI() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, uri2);
		model.add(uri1, RDFS.LABEL, uri3);
		assertEquals(2, model.size());
		return model;
	}

	protected Model getNewModelObjectDoubleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, bnode1);
		model.add(uri1, RDFS.LABEL, bnode2);
		assertEquals(2, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleLiteralSingleURI() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, uri2);
		assertEquals(2, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleLiteralSingleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, bnode1);
		assertEquals(2, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleURISingleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, uri1);
		model.add(uri1, RDFS.LABEL, bnode1);
		assertEquals(2, model.size());
		return model;
	}

	protected Model getNewModelObjectTripleLiteral() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, literal2);
		model.add(uri1, RDFS.LABEL, literal3);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectTripleURI() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, uri1);
		model.add(uri1, RDFS.LABEL, uri2);
		model.add(uri1, RDFS.LABEL, uri3);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectTripleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, bnode1);
		model.add(uri1, RDFS.LABEL, bnode2);
		model.add(uri1, RDFS.LABEL, bnode3);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleLiteralSingleURISingleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, uri2);
		model.add(uri1, RDFS.LABEL, bnode1);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleLiteralDoubleURI() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, uri2);
		model.add(uri1, RDFS.LABEL, uri3);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleLiteralDoubleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, bnode1);
		model.add(uri1, RDFS.LABEL, bnode2);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleURIDoubleBNode() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, uri1);
		model.add(uri1, RDFS.LABEL, bnode1);
		model.add(uri1, RDFS.LABEL, bnode2);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleURIDoubleLiteral() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, uri1);
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, literal2);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleBNodeDoubleURI() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, bnode1);
		model.add(uri1, RDFS.LABEL, uri2);
		model.add(uri1, RDFS.LABEL, uri3);
		assertEquals(3, model.size());
		return model;
	}

	protected Model getNewModelObjectSingleBNodeDoubleLiteral() {
		Model model = getNewEmptyModel();
		model.add(uri1, RDFS.LABEL, bnode1);
		model.add(uri1, RDFS.LABEL, literal1);
		model.add(uri1, RDFS.LABEL, literal2);
		assertEquals(3, model.size());
		return model;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		uri1 = vf.createURI("urn:test:uri:1");
		uri2 = vf.createURI("urn:test:uri:2");
		uri3 = vf.createURI("urn:test:uri:3");
		bnode1 = vf.createBNode();
		bnode2 = vf.createBNode("bnode2");
		bnode3 = vf.createBNode("bnode3");
		literal1 = vf.createLiteral("test literal 1");
		literal2 = vf.createLiteral("test literal 2");
		literal3 = vf.createLiteral("test literal 3");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#filter(Resource, URI, Value, Resource...)}.
	 */
	@Test
	public final void testFilterSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Model filter1 = model.filter(null, null, literal1);
		assertFalse(filter1.isEmpty());
		Model filter2 = model.filter(null, null, literal1, (Resource)null);
		assertFalse(filter2.isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.Model#contains(Resource, URI, Value, Resource...)}
	 * .
	 */
	@Test
	public final void testContainsSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		assertTrue(model.contains(null, null, literal1));
		assertTrue(model.contains(null, null, literal1, (Resource)null));
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#subjects()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testSubjects() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#predicates()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testPredicates() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objects()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testObjects() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#contexts()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testContexts() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueEmpty() {
		Model model = getNewEmptyModel();
		Optional<Value> value = model.objectValue();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Optional<Value> value = model.objectValue();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<Value> value = model.objectValue();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		Optional<Value> value = model.objectValue();
		assertTrue(value.isPresent());
		assertEquals(bnode1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueTripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueTripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueTripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectValue()}.
	 */
	@Test
	public final void testObjectValueSingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectValue();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralEmpty() {
		Model model = getNewEmptyModel();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleURI() {
		Model model = getNewModelObjectSingleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralTripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralTripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralTripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectLiteral()}.
	 */
	@Test
	public final void testObjectLiteralSingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceEmpty() {
		Model model = getNewEmptyModel();
		Optional<Resource> value = model.objectResource();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(bnode1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceTripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceTripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceTripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectResource()}.
	 */
	@Test
	public final void testObjectResourceSingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURIEmpty() {
		Model model = getNewEmptyModel();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURIDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURIDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURIDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURITripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURITripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURITripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testObjectURISingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringEmpty() {
		Model model = getNewEmptyModel();
		Optional<String> value = model.objectString();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Optional<String> value = model.objectString();
		assertTrue(value.isPresent());
		assertEquals(literal1.stringValue(), value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<String> value = model.objectString();
		assertTrue(value.isPresent());
		assertEquals(uri2.toString(), value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		Optional<String> value = model.objectString();
		assertTrue(value.isPresent());
		assertEquals(bnode1.stringValue(), value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringTripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringTripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringTripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectString()}.
	 */
	@Test
	public final void testObjectStringSingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectString();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralEmpty() {
		Model model = getNewEmptyModel();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralTripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralTripleURI() {
		Model model = getNewModelObjectTripleURI();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralTripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		Optional<Literal> value = model.objectLiteral();
		assertTrue(value.isPresent());
		assertEquals(literal1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		Optional<Literal> value = model.objectLiteral();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectLiteral()}.
	 */
	@Test
	public final void testAnObjectLiteralSingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectLiteral();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceEmpty() {
		Model model = getNewEmptyModel();
		Optional<Resource> value = model.objectResource();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Optional<Resource> value = model.objectResource();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(bnode1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		Optional<Resource> value = model.objectResource();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(bnode1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceTripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		Optional<Resource> value = model.objectResource();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceTripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceTripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(uri1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectResource();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#anObjectResource()}.
	 */
	@Test
	public final void testAnObjectResourceSingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		Optional<Resource> value = model.objectResource();
		assertTrue(value.isPresent());
		assertEquals(bnode1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURIEmpty() {
		Model model = getNewEmptyModel();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleLiteral() {
		Model model = getNewModelObjectSingleLiteral();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleURI() {
		Model model = getNewModelObjectSingleURI();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleBNode() {
		Model model = getNewModelObjectSingleBNode();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURIDoubleLiteral() {
		Model model = getNewModelObjectDoubleLiteral();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleLiteralSingleURI() {
		Model model = getNewModelObjectSingleLiteralSingleURI();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleLiteralSingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleBNode();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleURISingleBNode() {
		Model model = getNewModelObjectSingleURISingleBNode();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURIDoubleURI() {
		Model model = getNewModelObjectDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURIDoubleBNode() {
		Model model = getNewModelObjectDoubleBNode();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURITripleLiteral() {
		Model model = getNewModelObjectTripleLiteral();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURITripleURI() {
		Model model = getNewModelObjectTripleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURITripleBNode() {
		Model model = getNewModelObjectTripleBNode();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleLiteralSingleURISingleBNode() {
		Model model = getNewModelObjectSingleLiteralSingleURISingleBNode();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri2, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleLiteralDoubleBNode() {
		Model model = getNewModelObjectSingleLiteralDoubleBNode();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleLiteralDoubleURI() {
		Model model = getNewModelObjectSingleLiteralDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleURIDoubleBNode() {
		Model model = getNewModelObjectSingleURIDoubleBNode();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleURIDoubleLiteral() {
		Model model = getNewModelObjectSingleURIDoubleLiteral();
		Optional<URI> value = model.objectURI();
		assertTrue(value.isPresent());
		assertEquals(uri1, value.get());
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleBNodeDoubleURI() {
		Model model = getNewModelObjectSingleBNodeDoubleURI();
		// We expect an exception during the next method call
		thrown.expect(ModelException.class);
		model.objectURI();
	}

	/**
	 * Test method for {@link org.openrdf.model.Model#objectURI()}.
	 */
	@Test
	public final void testAnObjectURISingleBNodeDoubleLiteral() {
		Model model = getNewModelObjectSingleBNodeDoubleLiteral();
		Optional<URI> value = model.objectURI();
		assertFalse(value.isPresent());
	}

}
