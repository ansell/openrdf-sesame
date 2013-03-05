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

	private final PrintStream out, err;

	private final ConsoleState appInfo;

	private boolean echo = false;

	private boolean quiet = false;

	private boolean force = false;

	private boolean cautious = false;

	ConsoleIO(BufferedReader input, PrintStream out, PrintStream err, ConsoleState info) {
		this.input = input;
		this.out = out;
		this.err = err;
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
		if (!quiet) {
			if (repositoryID != null) {
				write(repositoryID);
			}
			write("> ");
		}
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
		if (echo) {
			writeln(result);
		}
		return result;
	}

	protected String readln(String... message)
		throws IOException
	{
		if (!quiet && message.length > 0) {
			String prompt = message[0];
			if (prompt != null) {
				write(prompt + " ");
			}
		}
		String result = input.readLine();
		if (echo) {
			writeln(result);
		}
		return result;
	}

	protected String readPassword(final String message)
		throws IOException
	{
		// TODO: Proper password reader
		String result = readln(message);
		if (echo && !result.isEmpty()) {
			writeln("************");
		}
		return result;
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
		err.println(errMsg);
	}

	protected void writeUnopenedError() {
		writeError(PLEASE_OPEN_FIRST);
	}

	protected void writeParseError(final String prefix, final int lineNo, final int colNo, final String msg) {
		String locationString = RDFParseException.getLocationString(lineNo, colNo);
		int locSize = locationString.length();
		final StringBuilder builder = new StringBuilder(locSize + prefix.length() + msg.length() + 3);
		builder.append(prefix).append(": ").append(msg);
		if (locSize > 0) {
			builder.append(" ").append(locationString);
		}
		writeError(builder.toString());
	}

	protected boolean askProceed(final String msg, final boolean defaultValue)
		throws IOException
	{
		final String defaultString = defaultValue ? "yes" : "no";
		boolean result = force ? true : (cautious ? false : defaultValue);
		if (!force && !cautious) {
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
		}
		return result;
	}

	/**
	 * @param echo
	 *        whether to echo user input to output stream
	 */
	protected void setEcho(boolean echo) {
		this.echo = echo;
	}

	/**
	 * @param quiet
	 *        whether to suppress printing of prompts to output
	 */
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	/**
	 */
	public void setForce() {
		this.force = true;
	}

	/**
	 */
	public void setCautious() {
		this.cautious = true;
	}
}
