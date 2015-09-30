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

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryReadOnlyException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dale Visser
 */
public class Drop implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Drop.class);

	private final ConsoleIO consoleIO;

	private final ConsoleState state;

	private final Close close;

	private final LockRemover lockRemover;

	Drop(ConsoleIO consoleIO, ConsoleState state, Close close, LockRemover lockRemover) {
		this.consoleIO = consoleIO;
		this.state = state;
		this.close = close;
		this.lockRemover = lockRemover;
	}

	public void execute(String... tokens)
		throws IOException
	{
		if (tokens.length < 2) {
			consoleIO.writeln(PrintHelp.DROP);
		}
		else {
			final String repoID = tokens[1];
			try {
				dropRepository(repoID);
			}
			catch (RepositoryConfigException e) {
				consoleIO.writeError("Unable to drop repository '" + repoID + "': " + e.getMessage());
				LOGGER.warn("Unable to drop repository '" + repoID + "'", e);
			}
			catch (RepositoryReadOnlyException e) {
				try {
					if (lockRemover.tryToRemoveLock(state.getManager().getSystemRepository())) {
						execute(tokens);
					}
					else {
						consoleIO.writeError("Failed to drop repository");
						LOGGER.error("Failed to drop repository", e);
					}
				}
				catch (RepositoryException e2) {
					consoleIO.writeError("Failed to restart system: " + e2.getMessage());
					LOGGER.error("Failed to restart system", e2);
				}
			}
			catch (RepositoryException e) {
				consoleIO.writeError("Failed to update configuration in system repository: " + e.getMessage());
				LOGGER.warn("Failed to update configuration in system repository", e);
			}
		}
	}

	private void dropRepository(final String repoID)
		throws IOException, RepositoryException, RepositoryConfigException
	{
		boolean proceed = consoleIO.askProceed("WARNING: you are about to drop repository '" + repoID
				+ "'.", true);
		if (proceed && !state.getManager().isSafeToRemove(repoID)) {
			proceed = consoleIO.askProceed("WARNING: dropping this repository may break another that is proxying it.", false);
		}
		if (proceed) {
			if (repoID.equals(state.getRepositoryID())) {
				close.closeRepository(false);
			}
			final boolean isRemoved = state.getManager().removeRepository(repoID);
			if (isRemoved) {
				consoleIO.writeln("Dropped repository '" + repoID + "'");
			}
			else {
				consoleIO.writeln("Unknown repository '" + repoID + "'");
			}
		}
		else {
			consoleIO.writeln("Drop aborted");
		}
	}

}
