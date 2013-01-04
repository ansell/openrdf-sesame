/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;


/**
 * @author Dale Visser
 */
class PrintInfo implements Command {

	private final ConsoleState appInfo;

	private final ConsoleIO consoleIO;

	PrintInfo(ConsoleIO consoleIO, ConsoleState appInfo) {
		this.consoleIO = consoleIO;
		this.appInfo = appInfo;
	}

	public void execute(String... parameters) {
		consoleIO.writeln(appInfo.getApplicationName());
		consoleIO.writeln("Data dir: " + appInfo.getDataDirectory());
		String managerID = appInfo.getManagerID();
		consoleIO.writeln("Connected to: " + (managerID == null ? "-" : managerID));
	}
}
