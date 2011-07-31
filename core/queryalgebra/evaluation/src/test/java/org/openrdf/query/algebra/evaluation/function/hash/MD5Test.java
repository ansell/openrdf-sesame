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
public class MD5Test extends HashFunctionTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		setHashFunction(new MD5());
		setToHash("abc");
		setExpectedDigest("900150983cd24fb0d6963f7d28e17f72");
	}

}
