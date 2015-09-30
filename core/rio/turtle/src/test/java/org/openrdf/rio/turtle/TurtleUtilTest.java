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
	@Test
	public final void testIsWhitespace() {
		assertFalse(TurtleUtil.isWhitespace(';'));
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
