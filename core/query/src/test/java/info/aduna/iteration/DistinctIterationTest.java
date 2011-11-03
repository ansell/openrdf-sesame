/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.HashSet;

public class DistinctIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new DistinctIteration<String, Exception>(createStringList1Iteration());
	}

	@Override
	protected int getTestIterationSize()
	{
		return new HashSet<String>(stringList1).size();
	}
}
