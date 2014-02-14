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

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Abstract tests for the {@link ValueFactory} interface, including concurrency
 * tests, when these are enabled.
 * 
 * @author Peter Ansell
 */
public abstract class AbstractValueFactoryTest {

	private ValueFactory vf;

	@Before
	public void setUp()
		throws Exception
	{
		vf = getNewValueFactory();
	}

	@After
	public void tearDown()
		throws Exception
	{
		vf = null;
	}

	/**
	 * Implementing tests must override this method to provide an implementation
	 * of the {@link ValueFactory} interface.
	 * 
	 * @return A new instance of the {@link ValueFactory} interface.
	 */
	protected abstract ValueFactory getNewValueFactory();

	/**
	 * Determines whether to enable the concurrency tests for this
	 * implementation.
	 * 
	 * @return True if the implementation is thought to be threadsafe, and should
	 *         be tested using concurrent access from different threads.
	 */
	protected abstract boolean isThreadSafe();

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String)}.
	 */
	@Ignore
	@Test
	public final void testCreateURIString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createURI(java.lang.String, java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateURIStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createBNode()}.
	 */
	@Ignore
	@Test
	public final void testCreateBNode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createBNode(java.lang.String)}.
	 */
	@Ignore
	@Test
	public final void testCreateBNodeString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.lang.String)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.lang.String, java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralStringURI() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(boolean)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createLiteral(byte)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(short)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralShort() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createLiteral(int)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.ValueFactory#createLiteral(long)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(float)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralFloat() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(double)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralDouble() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(javax.xml.datatype.XMLGregorianCalendar)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateLiteralXMLGregorianCalendar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createLiteral(java.util.Date)}.
	 */
	@Ignore
	@Test
	public final void testCreateLiteralDate() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateStatementResourceURIValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.ValueFactory#createStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource)}
	 * .
	 */
	@Ignore
	@Test
	public final void testCreateStatementResourceURIValueResource() {
		fail("Not yet implemented"); // TODO
	}
}
