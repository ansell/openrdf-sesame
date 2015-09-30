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
package org.eclipse.rdf4j.console;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.ParseErrorListener;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

/**
 * @author Dale Visser
 */
class VerificationListener extends AbstractRDFHandler implements ParseErrorListener {

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

	public void warning(final String msg, final long lineNo, final long colNo) {
		warnings++;
		consoleIO.writeParseError("WARNING", lineNo, colNo, msg);
	}

	public void error(final String msg, final long lineNo, final long colNo) {
		errors++;
		consoleIO.writeParseError("ERROR", lineNo, colNo, msg);
	}

	public void fatalError(final String msg, final long lineNo, final long colNo) {
		errors++;
		consoleIO.writeParseError("FATAL ERROR", lineNo, colNo, msg);
	}
}
