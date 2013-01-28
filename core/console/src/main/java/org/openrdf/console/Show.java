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

import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * @author Dale Visser
 */
public class Show implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Show.class);

	private static final String OUTPUT_SEPARATOR = "+----------";

	private final ConsoleIO consoleIO;

	private final ConsoleState state;

	Show(ConsoleIO consoleIO, ConsoleState state) {
		this.consoleIO = consoleIO;
		this.state = state;
	}

	public void execute(String... tokens) {
		if (tokens.length == 2) {
			final String target = tokens[1].toLowerCase(Locale.ENGLISH);
			if ("repositories".equals(target) || "r".equals(target)) {
				showRepositories();
			}
			else if ("namespaces".equals(target) || "n".equals(target)) {
				showNamespaces();
			}
			else if ("contexts".equals(target) || "c".equals(target)) {
				showContexts();
			}
			else {
				consoleIO.writeError("Unknown target '" + tokens[1] + "'");
			}
		}
		else {
			consoleIO.writeln(PrintHelp.SHOW);
		}
	}

	private void showRepositories() {
		try {
			RepositoryManager manager = state.getManager();
			final Set<String> repIDs = manager.getRepositoryIDs();
			if (repIDs.isEmpty()) {
				consoleIO.writeln("--no repositories found--");
			}
			else {
				consoleIO.writeln(OUTPUT_SEPARATOR);
				for (String repID : repIDs) {
					consoleIO.write("|" + repID);

					try {
						final RepositoryInfo repInfo = manager.getRepositoryInfo(repID);
						if (repInfo.getDescription() != null) {
							consoleIO.write(" (\"" + repInfo.getDescription() + "\")");
						}
					}
					catch (RepositoryException e) {
						consoleIO.write(" [ERROR: " + e.getMessage() + "]");
					}
					consoleIO.writeln();
				}
				consoleIO.writeln(OUTPUT_SEPARATOR);
			}
		}
		catch (RepositoryException e) {
			consoleIO.writeError("Failed to get repository list: " + e.getMessage());
			LOGGER.error("Failed to get repository list", e);
		}
	}

	private void showNamespaces() {
		Repository repository = state.getRepository();
		if (repository == null) {
			consoleIO.writeUnopenedError();
			return;
		}

		RepositoryConnection con;
		try {
			con = repository.getConnection();
			try {
				final CloseableIteration<? extends Namespace, RepositoryException> namespaces = con.getNamespaces();
				try {
					if (namespaces.hasNext()) {
						consoleIO.writeln(OUTPUT_SEPARATOR);
						while (namespaces.hasNext()) {
							final Namespace namespace = namespaces.next();
							consoleIO.writeln("|" + namespace.getPrefix() + "  " + namespace.getName());
						}
						consoleIO.writeln(OUTPUT_SEPARATOR);
					}
					else {
						consoleIO.writeln("--no namespaces found--");
					}
				}
				finally {
					namespaces.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			consoleIO.writeError(e.getMessage());
			LOGGER.error("Failed to show namespaces", e);
		}
	}

	private void showContexts() {
		Repository repository = state.getRepository();
		if (repository == null) {
			consoleIO.writeUnopenedError();
			return;
		}

		RepositoryConnection con;
		try {
			con = repository.getConnection();
			try {
				final CloseableIteration<? extends Resource, RepositoryException> contexts = con.getContextIDs();
				try {
					if (contexts.hasNext()) {
						consoleIO.writeln(OUTPUT_SEPARATOR);
						while (contexts.hasNext()) {
							consoleIO.writeln("|" + contexts.next().toString());
						}
						consoleIO.writeln(OUTPUT_SEPARATOR);
					}
					else {
						consoleIO.writeln("--no contexts found--");
					}
				}
				finally {
					contexts.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			consoleIO.writeError(e.getMessage());
			LOGGER.error("Failed to show contexts", e);
		}
	}

}
