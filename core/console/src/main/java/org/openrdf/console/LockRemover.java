/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.IOException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryLockedException;
import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;
import org.openrdf.sail.helpers.DirectoryLockManager;

/**
 *
 * @author DAle Visser
 */
public class LockRemover {
	
	private final ConsoleIO consoleIO;

	LockRemover(ConsoleIO consoleIO) {
		this.consoleIO = consoleIO;
	}
	
	protected boolean tryToRemoveLock(final Repository repo)
			throws IOException, RepositoryException
		{
			boolean lockRemoved = false;
			final LockManager lockManager = new DirectoryLockManager(repo.getDataDir());
			if (lockManager.isLocked()
					&& consoleIO.askProceed(
							"WARNING: The lock from another process on this repository needs to be removed", true))
			{
				repo.shutDown();
				lockRemoved = lockManager.revokeLock();
				repo.initialize();
			}
			return lockRemoved;
		}

		protected boolean tryToRemoveLock(final RepositoryLockedException rle)
			throws IOException
		{
			boolean lockRemoved = false;
			if (rle.getCause() instanceof SailLockedException) {
				final SailLockedException sle = (SailLockedException)rle.getCause();
				final LockManager lockManager = sle.getLockManager();
				if (lockManager != null
						&& lockManager.isLocked()
						&& consoleIO.askProceed("WARNING: The lock from process '" + sle.getLockedBy()
								+ "' on this repository needs to be removed", true))
				{
					lockRemoved = lockManager.revokeLock();
				}
			}
			return lockRemoved;
		}
}
