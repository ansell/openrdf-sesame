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
