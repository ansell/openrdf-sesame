/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import org.openrdf.repository.Repository;

/**
 * @author Dale Visser
 */
public class Close implements Command {

	private final ConsoleIO consoleIO;

	private final ConsoleState appInfo;

	Close(ConsoleIO consoleIO, ConsoleState appInfo) {
		this.consoleIO = consoleIO;
		this.appInfo = appInfo;
	}

	public void execute(String... tokens) {
		if (tokens.length == 1) {
			closeRepository(true);
		}
		else {
			consoleIO.writeln(PrintHelp.CLOSE);
		}
	}

	protected void closeRepository(final boolean verbose) {
		final Repository repository = this.appInfo.getRepository();
		if (repository == null) {
			if (verbose) {
				consoleIO.writeln("There are no open repositories that can be closed");
			}
		}
		else {
			consoleIO.writeln("Closing repository '" + this.appInfo.getRepositoryID() + "'...");
			this.appInfo.setRepository(null);
			this.appInfo.setRepositoryID(null);
		}
	}

}
