/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
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
