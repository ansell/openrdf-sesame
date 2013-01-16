/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

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
			final RDFFormat format = Rio.getParserFormatForFileName(dataPath, RDFFormat.RDFXML);
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

	/**
	 * @param tokens
	 * @return
	 */
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
