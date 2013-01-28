/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
