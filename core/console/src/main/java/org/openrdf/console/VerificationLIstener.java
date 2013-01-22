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

import org.openrdf.model.Statement;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * @author Dale Visser
 */
class VerificationListener extends RDFHandlerBase implements ParseErrorListener {

	private final ConsoleIO consoleIO;

	VerificationListener(ConsoleIO consoleIO) {
		super();
		this.consoleIO = consoleIO;
	}

	private int warnings;

	private int errors;

	private int statements;

	public int getWarnings() {
		return warnings;
	}

	public int getErrors() {
		return errors;
	}

	public int getStatements() {
		return statements;
	}

	public void handleStatement(final Statement statement)
		throws RDFHandlerException
	{
		statements++;
	}

	public void warning(final String msg, final int lineNo, final int colNo) {
		warnings++;
		consoleIO.writeParseError("WARNING", lineNo, colNo, msg);
	}

	public void error(final String msg, final int lineNo, final int colNo) {
		errors++;
		consoleIO.writeParseError("ERROR", lineNo, colNo, msg);
	}

	public void fatalError(final String msg, final int lineNo, final int colNo) {
		errors++;
		consoleIO.writeParseError("FATAL ERROR", lineNo, colNo, msg);
	}
}
