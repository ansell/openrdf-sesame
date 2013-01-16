/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import org.openrdf.repository.manager.RepositoryManager;


/**
 *
 * @author Dale Visser
 */
public class Disconnect {
	
	private final ConsoleIO consoleIO;
	private final ConsoleState appInfo;
	private final Close close;

	Disconnect(ConsoleIO consoleIO, ConsoleState appInfo, Close close){
		this.consoleIO = consoleIO;
		this.appInfo = appInfo;
		this.close = close;
	}

	public void execute(boolean verbose) {
		final RepositoryManager manager = this.appInfo.getManager();
		if (manager == null) {
			if (verbose) {
				consoleIO.writeln("Already disconnected");
			}
		}
		else {
			close.closeRepository(false);
			consoleIO.writeln("Disconnecting from " + this.appInfo.getManagerID());
			manager.shutDown();
			appInfo.setManager(null);
			appInfo.setManagerID(null);
		}
	}
}
