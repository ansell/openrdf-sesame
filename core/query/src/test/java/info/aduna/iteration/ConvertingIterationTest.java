/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.Arrays;
import java.util.List;

public class ConvertingIterationTest extends CloseableIterationTest {

	private static final List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	protected static CloseableIteration<String, Exception> createConvertingIteration() {
		Iteration<Integer, Exception> intIteration = new CloseableIteratorIteration<Integer, Exception>(intList.iterator());
		return new ConvertingIteration<Integer, String, Exception>(intIteration) {
			protected String convert(Integer integer) {
				return integer.toString();
			}
		};
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createConvertingIteration();
	}

	@Override
	protected int getTestIterationSize()
	{
		return 10;
	}
}
