/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

public class IteratorIterationTest extends IterationTest {

	@Override
	protected Iteration<String, Exception> createTestIteration()
	{
		return new IteratorIteration<String, Exception>(stringList1.iterator());
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
