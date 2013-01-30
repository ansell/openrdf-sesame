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
