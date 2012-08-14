/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 *
 */
public abstract class CloseableIterationTest extends IterationTest {

	protected abstract CloseableIteration<String, Exception> createTestIteration()
		throws Exception;

	public void testClosedIteration()
		throws Exception
	{
		for (int n = 0; n < getTestIterationSize(); n++) {
			CloseableIteration<String, Exception> iter = createTestIteration();

			// Close after n iterations
			for (int i = 0; i < n; i++) {
				iter.next();
			}

			iter.close();

			assertFalse("closed iteration should not contain any more elements", iter.hasNext());

			try {
				iter.next();
				fail("next() called on a closed iteration should throw a NoSuchElementException");
			}
			catch (NoSuchElementException e) {
				// expected exception
			}
			catch (Exception e) {
				fail("next() called on a closed iteration should throw a NoSuchElementException");
			}
		}
	}
}
