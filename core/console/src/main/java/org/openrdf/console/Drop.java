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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryReadOnlyException;
import org.openrdf.repository.config.RepositoryConfigException;

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

	/**
	 * @param repoID
	 * @param manager
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 */
	private void dropRepository(final String repoID)
		throws IOException, RepositoryException, RepositoryConfigException
	{
		final boolean proceed = consoleIO.askProceed("WARNING: you are about to drop repository '" + repoID
				+ "'.", true);
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
