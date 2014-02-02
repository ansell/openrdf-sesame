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
package org.openrdf.rio.turtle;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the utility methods in {@link TurtleUtil}.
 * 
 * @author Peter Ansell
 */
public class TurtleUtilTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
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
	 * {@link org.openrdf.rio.turtle.TurtleUtil#findURISplitIndex(java.lang.String)}
	 * .
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testFindURISplitIndex() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isWhitespace(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsWhitespace() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPN_CHARS_BASE(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPN_CHARS_BASE() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPN_CHARS_U(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPN_CHARS_U() {

	}

	/**
	 * Test method for {@link org.openrdf.rio.turtle.TurtleUtil#isPN_CHARS(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPN_CHARS() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPrefixStartChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPrefixStartChar() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isNameStartChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsNameStartChar() {

	}

	/**
	 * Test method for {@link org.openrdf.rio.turtle.TurtleUtil#isNameChar(int)}.
	 */
	@Test
	public final void testIsNameChar() {
		assertFalse(TurtleUtil.isNameChar(';'));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isNameEndChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsNameEndChar() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isLocalEscapedChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsLocalEscapedChar() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPrefixChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPrefixChar() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isLanguageStartChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsLanguageStartChar() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isLanguageChar(int)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsLanguageChar() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPN_PREFIX(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPN_PREFIX() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPLX_START(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPLX_START() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPERCENT(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPERCENT() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPLX_INTERNAL(java.lang.String)}
	 * .
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPLX_INTERNAL() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPN_LOCAL_ESC(java.lang.String)}
	 * .
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPN_LOCAL_ESC() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#isPN_LOCAL(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testIsPN_LOCAL() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#encodeString(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testEncodeString() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#encodeLongString(java.lang.String)}
	 * .
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testEncodeLongString() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#encodeURIString(java.lang.String)}
	 * .
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testEncodeURIString() {

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.turtle.TurtleUtil#decodeString(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me")
	@Test
	public final void testDecodeString() {

	}

}
