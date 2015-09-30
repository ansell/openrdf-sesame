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
