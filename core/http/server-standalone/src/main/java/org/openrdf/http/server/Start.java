/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Main class for starting a Sesame server.
 * 
 * @author James Leigh
 */
public class Start {

	public static void main(String[] args)
		throws Exception
	{
		// Parse command line options
		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "print this help");
		Option versionOption = new Option("v", "version", false, "print version information");
		Option dirOption = new Option("d", "dataDir", true, "Sesame data dir to 'connect' to (required)");
		Option portOption = new Option("p", "port", true, "port to listen on");
		Option maxAgeOption = new Option("c", "maxCacheAge", true,
				"How many seconds clients can use their cache before validating it with the server");
		Option keyOption = new Option("k", "shutdownKey", true, "Key to shut down server");

		options.addOption(helpOption);
		options.addOption(versionOption);
		options.addOption(dirOption);
		options.addOption(portOption);
		options.addOption(maxAgeOption);
		options.addOption(keyOption);

		CommandLineParser argsParser = new PosixParser();

		try {
			CommandLine commandLine = argsParser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printUsage(options);
				System.exit(0);
			}

			if (commandLine.hasOption(versionOption.getOpt())) {
				System.out.println(SesameServlet.getServerName());
				System.exit(0);
			}

			String dirString = commandLine.getOptionValue(dirOption.getOpt());
			String[] otherArgs = commandLine.getArgs();

			if (dirString == null || otherArgs.length > 1) {
				System.out.println("Please specify a data directory");
				printUsage(options);
				System.exit(2);
			}

			// Try to set the user dir for resolving relative paths
			try {
				System.setProperty("user.dir", new File(dirString).getAbsolutePath());
			}
			catch (SecurityException e) {
				System.err.println("Warning: Unable to set user dir: " + e.getMessage());
			}

			String portString = commandLine.getOptionValue(portOption.getOpt());
			int port = SesameServer.DEFAULT_PORT;
			if (portString != null) {
				try {
					port = Integer.parseInt(portString);
				}
				catch (NumberFormatException e) {
					System.out.println("Invalid port number '" + portString + "'");
					System.exit(3);
				}
			}

			SesameServer server = new SesameServer(new File(dirString), port);

			String ageString = commandLine.getOptionValue(maxAgeOption.getOpt());
			if (ageString != null) {
				server.setMaxCacheAge(Integer.parseInt(ageString));
			}

			String keyString = commandLine.getOptionValue(keyOption.getOpt());
			if (keyString != null) {
				server.setShutdownKey(keyString);
			}

			try {
				server.start();

				System.out.println("Server listening on port " + port);
				System.out.println("data dir: " + server.getDataDir());
				System.out.println("Shutdown key is: " + server.getShutdownKey());
			}
			catch (Exception e) {
				server.stop();
				throw e;
			}
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static void printUsage(Options options) {
		System.out.println("Sesame Server, a standalone RDF server.");
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("server [OPTION]", options);
		System.out.println();
		System.out.println("For bug reports and suggestions, see http://www.openrdf.org/");
	}
}
