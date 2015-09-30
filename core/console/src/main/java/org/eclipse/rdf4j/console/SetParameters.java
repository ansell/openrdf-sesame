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

/**
 * @author dale
 */
public class SetParameters implements Command {

	private final ConsoleIO consoleIO;

	private final ConsoleParameters parameters;

	SetParameters(ConsoleIO consoleIO, ConsoleParameters parameters) {
		this.consoleIO = consoleIO;
		this.parameters = parameters;
	}

	public void execute(String... tokens) {
		if (tokens.length == 1) {
			showParameters();
		}
		else if (tokens.length == 2) {
			final String param = tokens[1];
			String key, value;
			final int eqIdx = param.indexOf('=');
			if (eqIdx == -1) {
				key = param;
				value = null; // NOPMD
			}
			else {
				key = param.substring(0, eqIdx);
				value = param.substring(eqIdx + 1);
			}
			setParameter(key, value);
		}
		else {
			consoleIO.writeln(PrintHelp.SET);
		}
	}

	private void showParameters() {
		setWidth(null);
		setShowPrefix(null);
		setQueryPrefix(null);
	}

	private void setParameter(final String key, final String value) {
		if ("width".equalsIgnoreCase(key)) {
			setWidth(value);
		}
		else if ("showprefix".equalsIgnoreCase(key)) {
			setShowPrefix(value);
		}
		else if ("queryprefix".equalsIgnoreCase(key)) {
			setQueryPrefix(value);
		}
		else {
			consoleIO.writeError("unknown parameter: " + key);
		}
	}

	private void setWidth(final String value) {
		if (value == null) {
			consoleIO.writeln("width: " + parameters.getWidth());
		}
		else {
			try {
				final int width = Integer.parseInt(value);
				if (width > 0) {
					parameters.setWidth(width);
				}
				else {
					consoleIO.writeError("Width must be larger than 0");
				}
			}
			catch (NumberFormatException e) {
				consoleIO.writeError("Width must be a positive number");
			}
		}
	}

	private void setShowPrefix(final String value) {
		if (value == null) {
			consoleIO.writeln("showPrefix: " + parameters.isShowPrefix());
		}
		else {
			parameters.setShowPrefix(Boolean.parseBoolean(value));
		}
	}

	private void setQueryPrefix(final String value) {
		if (value == null) {
			consoleIO.writeln("queryPrefix: " + parameters.isQueryPrefix());
		}
		else {
			parameters.setQueryPrefix(Boolean.parseBoolean(value));
		}
	}
}
