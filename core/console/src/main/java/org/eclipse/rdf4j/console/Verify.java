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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dale Visser
 */
public class Verify implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Verify.class);

	private final ConsoleIO consoleIO;

	Verify(ConsoleIO consoleIO) {
		this.consoleIO = consoleIO;
	}

	public void execute(String... tokens) {
		if (tokens.length != 2) {
			consoleIO.writeln(PrintHelp.VERIFY);
			return;
		}
		String dataPath = parseDataPath(tokens);
		try {
			final URL dataURL = new URL(dataPath);
			final RDFFormat format = Rio.getParserFormatForFileName(dataPath).orElseThrow(
					Rio.unsupportedFormat(dataPath));
			consoleIO.writeln("RDF Format is " + format.getName());
			final RDFParser parser = Rio.createParser(format);
			final VerificationListener listener = new VerificationListener(consoleIO);
			parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
			parser.setVerifyData(true);
			parser.setParseErrorListener(listener);
			parser.setRDFHandler(listener);
			consoleIO.writeln("Verifying data...");
			final InputStream dataStream = dataURL.openStream();
			try {
				parser.parse(dataStream, "urn://openrdf.org/RioVerifier/");
			}
			finally {
				dataStream.close();
			}
			final int warnings = listener.getWarnings();
			final int errors = listener.getErrors();
			if (warnings + errors > 0) {
				consoleIO.writeln("Found " + warnings + " warnings and " + errors + " errors");
			}
			else {
				consoleIO.writeln("Data verified, no errors were found");
			}
			if (errors == 0) {
				consoleIO.writeln("File contains " + listener.getStatements() + " statements");
			}
		}
		catch (MalformedURLException e) {
			consoleIO.writeError("Malformed URL: " + dataPath);
		}
		catch (IOException e) {
			consoleIO.writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			consoleIO.writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			LOGGER.error("Unexpected RDFParseException", e);
		}
		catch (RDFHandlerException e) {
			consoleIO.writeError("Unable to verify : " + e.getMessage());
			LOGGER.error("Unable to verify data file", e);
		}
	}

	private String parseDataPath(String... tokens) {
		StringBuilder dataPath = new StringBuilder(tokens[1]);
		try {
			new URL(dataPath.toString());
			// dataPath is a URI
		}
		catch (MalformedURLException e) {
			// File path specified, convert to URL
			dataPath.insert(0, "file:");
		}
		return dataPath.toString();
	}

}
