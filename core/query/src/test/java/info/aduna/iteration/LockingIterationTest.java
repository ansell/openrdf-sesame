/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;

public class LockingIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
		throws Exception
	{
		ExclusiveLockManager lockManager = new ExclusiveLockManager();
		Lock lock = lockManager.getExclusiveLock();
		return new LockingIteration<String, Exception>(lock, createStringList1Iteration());
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size();
	}
}
