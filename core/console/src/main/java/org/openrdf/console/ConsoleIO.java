/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.openrdf.rio.RDFParseException;

/**
 * @author Dale Visser
 */
class ConsoleIO {

	private static final String PLEASE_OPEN_FIRST = "please open a repository first";

	private final BufferedReader input;

	private final PrintStream out;

	private final ConsoleState appInfo;

	ConsoleIO(BufferedReader input, PrintStream out, ConsoleState info) {
		this.input = input;
		this.out = out;
		this.appInfo = info;
	}

	/**
	 * Reads multiple lines from the input until a line that ends with a '.' is
	 * read.
	 */
	protected String readMultiLineInput()
		throws IOException
	{
		String repositoryID = appInfo.getRepositoryID();
		if (repositoryID != null) {
			write(repositoryID);
		}
		write("> ");
		String line = input.readLine();
		String result = null;
		if (line != null) {
			final StringBuilder buf = new StringBuilder(256);
			buf.append(line);
			while (line != null && !line.endsWith(".")) {
				line = input.readLine();
				buf.append('\n');
				buf.append(line);
			}

			// Remove closing dot
			buf.setLength(buf.length() - 1);
			result = buf.toString().trim();
		}
		return result;
	}

	protected String readln(String... message)
		throws IOException
	{
		if (message.length > 0) {
			String prompt = message[0];
			if (prompt != null) {
				write(prompt + " ");
			}
		}
		return input.readLine();
	}

	protected String readPassword(final String message)
		throws IOException
	{
		// TODO: Proper password reader
		return readln(message);
	}

	protected void write(final String string) {
		out.print(string);
	}

	protected void writeln() {
		out.println();
	}

	protected void writeln(final String string) {
		out.println(string);
	}

	protected void writeError(final String errMsg) {
		writeln("ERROR: " + errMsg);
	}

	protected void writeUnopenedError() {
		writeError(PLEASE_OPEN_FIRST);
	}

	protected void writeParseError(final String prefix, final int lineNo, final int colNo, final String msg) {
		final StringBuilder builder = new StringBuilder(256);
		builder.append(prefix);
		builder.append(": ");
		builder.append(msg);
		final String locationString = RDFParseException.getLocationString(lineNo, colNo);
		if (locationString.length() > 0) {
			builder.append(" ").append(locationString);
		}
		writeln(builder.toString());
	}

	protected boolean askProceed(final String msg, final boolean defaultValue)
		throws IOException
	{
		final String defaultString = defaultValue ? "yes" : "no";
		boolean result = defaultValue;
		while (true) {
			writeln(msg);
			write("Proceed? (yes|no) [" + defaultString + "]: ");
			final String reply = readln();
			if ("no".equalsIgnoreCase(reply) || "no.".equalsIgnoreCase(reply)) {
				result = false;
				break;
			}
			else if ("yes".equalsIgnoreCase(reply) || "yes.".equalsIgnoreCase(reply)) {
				result = true;
				break;
			}
			else if (reply.trim().isEmpty()) {
				break;
			}
		}
		return result;
	}

}
