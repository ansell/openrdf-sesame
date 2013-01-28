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
