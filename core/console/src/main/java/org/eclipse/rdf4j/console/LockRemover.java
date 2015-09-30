/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.console;

import java.io.IOException;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryLockedException;
import org.eclipse.rdf4j.sail.LockManager;
import org.eclipse.rdf4j.sail.SailLockedException;
import org.eclipse.rdf4j.sail.helpers.DirectoryLockManager;

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
