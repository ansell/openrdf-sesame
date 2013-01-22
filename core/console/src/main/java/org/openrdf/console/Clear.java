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

import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryReadOnlyException;

/**
 * @author Dale Visser
 */
public class Clear implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Clear.class);

	private final ConsoleIO consoleIO;

	private final ConsoleState state;

	private final LockRemover lockRemover;

	Clear(ConsoleIO consoleIO, ConsoleState state, LockRemover lockRemover) {
		this.consoleIO = consoleIO;
		this.state = state;
		this.lockRemover = lockRemover;
	}

	public void execute(String... tokens) {
		Repository repository = state.getRepository();
		if (repository == null) {
			consoleIO.writeUnopenedError();
		}
		else {
			final ValueFactory valueFactory = repository.getValueFactory();
			Resource[] contexts = new Resource[tokens.length - 1];
			for (int i = 1; i < tokens.length; i++) {
				final String contextID = tokens[i];
				if (contextID.equalsIgnoreCase("null")) {
					contexts[i - 1] = null; // NOPMD
				}
				else if (contextID.startsWith("_:")) {
					contexts[i - 1] = valueFactory.createBNode(contextID.substring(2));
				}
				else {
					try {
						contexts[i - 1] = valueFactory.createURI(contextID);
					}
					catch (IllegalArgumentException e) {
						consoleIO.writeError("illegal URI: " + contextID);
						consoleIO.writeln(PrintHelp.CLEAR);
						return;
					}
				}
			}
			clear(repository, contexts);
		}
	}

	private void clear(Repository repository, Resource[] contexts) {
		if (contexts.length == 0) {
			consoleIO.writeln("Clearing repository...");
		}
		else {
			consoleIO.writeln("Removing specified contexts...");
		}
		try {
			final RepositoryConnection con = repository.getConnection();
			try {
				con.clear(contexts);
				if (contexts.length == 0) {
					con.clearNamespaces();
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (lockRemover.tryToRemoveLock(repository)) {
					this.clear(repository, contexts);
				}
				else {
					consoleIO.writeError("Failed to clear repository");
					LOGGER.error("Failed to clear repository", e);
				}
			}
			catch (RepositoryException e1) {
				consoleIO.writeError("Unable to restart repository: " + e1.getMessage());
				LOGGER.error("Unable to restart repository", e1);
			}
			catch (IOException e1) {
				consoleIO.writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (RepositoryException e) {
			consoleIO.writeError("Failed to clear repository: " + e.getMessage());
			LOGGER.error("Failed to clear repository", e);
		}
	}

}
