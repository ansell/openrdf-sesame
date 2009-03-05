/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.openrdf.model.impl.LiteralImpl;

public class RegexTest extends TestCase {

	private static final ValueExpr expression = new And();

	public void testLikeExactString()
		throws Exception
	{
		assertRegexLike("^pattern$", "pattern");
	}

	public void testLikePrefix()
		throws Exception
	{
		assertRegexLike("^pattern", "pattern*");
	}

	public void testLikeSuffix()
		throws Exception
	{
		assertRegexLike("pattern$", "*pattern");
	}

	public void testLikeSubstring()
		throws Exception
	{
		assertRegexLike("pattern", "*pattern*");
	}

	public void testLikeWildcard()
		throws Exception
	{
		assertRegexLike("p.*tt.*rn", "*p*tt*rn*");
	}

	public void testLikeQuoted()
		throws Exception
	{
		assertRegexLike("\\Qp_tt_rn\\E", "*p_tt_rn*");
	}

	public void testLikeEscaped()
		throws Exception
	{
		assertRegexLike("\\Qp\\Att\\E\\\\E\\Qrn\\E", "*p\\Att\\Ern*");
	}

	public void testNotCompiled()
		throws Exception
	{
		Regex node = new Regex(expression, expression, null);
		Pattern pattern = node.compile(new LiteralImpl("regex"), null);
		assertNotNull(pattern);
		assertEquals("regex", pattern.pattern());
	}

	private void assertRegexLike(String regex, String like)
		throws Exception
	{
		Regex node = new Regex(expression, like, true);
		Pattern pattern = node.compile(null, null);
		assertNotNull(pattern);
		assertEquals(regex, pattern.pattern());
	}
}
