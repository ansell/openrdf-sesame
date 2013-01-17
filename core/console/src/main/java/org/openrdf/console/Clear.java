/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
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
