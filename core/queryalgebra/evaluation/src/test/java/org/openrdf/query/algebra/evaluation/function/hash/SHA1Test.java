/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.hash;

import org.junit.Before;

/**
 * @author jeen
 */
public class SHA1Test extends HashFunctionTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		setHashFunction(new SHA1());
		setToHash("abc");
		setExpectedDigest("a9993e364706816aba3e25717850c26c9cd0d89d");
	}

}
