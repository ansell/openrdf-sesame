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
public class SHA512Test extends HashFunctionTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		setHashFunction(new SHA512());
		setToHash("abc");
		setExpectedDigest("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f");
	}

}
