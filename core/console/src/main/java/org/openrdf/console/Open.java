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
package org.openrdf.console;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryLockedException;
import org.openrdf.repository.config.RepositoryConfigException;

/**
 * @author Dale Visser
 */
public class Open implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Open.class);

	private final ConsoleIO consoleIO;

	private final ConsoleState state;

	private final Close close;

	private final LockRemover lockRemover;

	Open(ConsoleIO consoleIO, ConsoleState state, Close close, LockRemover lockRemover) {
		this.consoleIO = consoleIO;
		this.state = state;
		this.close = close;
		this.lockRemover = lockRemover;
	}

	public void execute(String... tokens) {
		if (tokens.length == 2) {
			openRepository(tokens[1]);
		}
		else {
			consoleIO.writeln(PrintHelp.OPEN);
		}
	}

	private static final String OPEN_FAILURE = "Failed to open repository";

	protected void openRepository(final String repoID) {
		try {
			final Repository newRepository = state.getManager().getRepository(repoID);

			if (newRepository == null) {
				consoleIO.writeError("Unknown repository: '" + repoID + "'");
			}
			else {
				// Close current repository, if any
				close.closeRepository(false);
				state.setRepository(newRepository);
				state.setRepositoryID(repoID);
				consoleIO.writeln("Opened repository '" + repoID + "'");
			}
		}
		catch (RepositoryLockedException e) {
			try {
				if (lockRemover.tryToRemoveLock(e)) {
					openRepository(repoID);
				}
				else {
					consoleIO.writeError(OPEN_FAILURE);
					LOGGER.error(OPEN_FAILURE, e);
				}
			}
			catch (IOException e1) {
				consoleIO.writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (RepositoryConfigException e) {
			consoleIO.writeError(e.getMessage());
			LOGGER.error(OPEN_FAILURE, e);
		}
		catch (RepositoryException e) {
			consoleIO.writeError(e.getMessage());
			LOGGER.error(OPEN_FAILURE, e);
		}
	}

}
