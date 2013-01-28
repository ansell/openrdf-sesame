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
