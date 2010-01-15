/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import info.aduna.app.AppConfiguration;

import org.openrdf.http.server.helpers.ServerUtil;

/**
 * Main class for starting a Sesame server.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class Start {

	private static final Option helpOption = new Option("h", "help", false, "print this help");

	private static final Option versionOption = new Option("v", "version", false, "print version information");

	private static final Option dirOption = new Option("d", "dataDir", true, "Sesame data dir");

	private static final Option portOption = new Option("p", "port", true, "port to listen on (default: "
			+ SesameServer.DEFAULT_PORT + ")");

	private static final Option sslPortOption = new Option("sp", "sslPort", true,
			"port to listen on (default: " + SesameServer.DEFAULT_PORT + ")");

	private static final Option maxAgeOption = new Option("c", "maxCacheAge", true,
			"How many seconds clients can use their cache before validating it with the server");

	private static final Option keyOption = new Option("k", "shutdownKey", true, "Key to shut down server");

	public static void main(String[] args)
		throws Exception
	{
		Options options = createCliOptions();
		CommandLineParser argsParser = new PosixParser();

		try {
			CommandLine commandLine = argsParser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printUsage(options);
			}
			else if (commandLine.hasOption(versionOption.getOpt())) {
				printVersion();
			}
			else {
				startServer(options, commandLine);
			}
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			printUsage(options);
			System.exit(1);
		}
	}

	private static Options createCliOptions() {
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(versionOption);
		options.addOption(dirOption);
		options.addOption(portOption);
		options.addOption(sslPortOption);
		options.addOption(maxAgeOption);
		options.addOption(keyOption);
		return options;
	}

	private static void printUsage(Options options) {
		System.out.println("USAGE:");
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("start-server [OPTION]", options);
	}

	private static void printVersion() {
		System.out.println(SesameApplication.getServerName());
		System.out.println();
		System.out.println("For bug reports and suggestions, see http://www.openrdf.org/");
	}

	private static File getDataDirectory(CommandLine commandLine)
		throws ParseException, IOException
	{
		String dir = commandLine.getOptionValue(dirOption.getOpt());

		if (dir != null) {
			return new File(dir);
		}
		else {
			AppConfiguration appConfig = new AppConfiguration("OpenRDF Sesame");
			appConfig.init();
			return appConfig.getDataDir();
		}
	}

	private static int getPort(CommandLine commandLine)
		throws ParseException
	{
		String portString = commandLine.getOptionValue(portOption.getOpt());

		if (portString == null) {
			return SesameServer.DEFAULT_PORT;
		}

		try {
			return Integer.parseInt(portString);
		}
		catch (NumberFormatException e) {
			throw new ParseException("Invalid port number '" + portString + "'");
		}
	}

	private static int getSslPort(CommandLine commandLine)
		throws ParseException
	{
		String sslPortString = commandLine.getOptionValue(sslPortOption.getOpt());

		if (sslPortString == null) {
			return -1;
		}

		try {
			return Integer.parseInt(sslPortString);
		}
		catch (NumberFormatException e) {
			throw new ParseException("Invalid port number '" + sslPortString + "'");
		}
	}

	private static int getMaxCacheAge(CommandLine commandLine)
		throws ParseException
	{
		String ageString = commandLine.getOptionValue(maxAgeOption.getOpt());

		if (ageString == null) {
			return 0;
		}

		try {
			return Integer.parseInt(ageString);
		}
		catch (NumberFormatException e) {
			throw new ParseException("Invalid maxCacheAge value '" + ageString + "'");
		}
	}

	private static String getShutdownKey(CommandLine commandLine)
		throws ParseException
	{
		String key = commandLine.getOptionValue(keyOption.getOpt());

		if (key == null) {
			key = String.valueOf(ServerUtil.RANDOM.nextLong());
		}

		return key;
	}

	private static void startServer(Options options, CommandLine commandLine)
		throws Exception
	{
		File dataDir = getDataDirectory(commandLine);
		int port = getPort(commandLine);
		int sslPort = getSslPort(commandLine);
		int maxCacheAge = getMaxCacheAge(commandLine);
		String shutdownKey = getShutdownKey(commandLine);

		SesameServer server = new SesameServer(dataDir, port, sslPort);
		// server.setMaxCacheAge(maxCacheAge);
		server.setShutdownKey(shutdownKey);

		server.start();

		System.out.println("Server listening on port " + server.getPort());
		if (server.getSslPort() > 0) {
			System.out.println("SSL port: " + server.getSslPort());
		}
		System.out.println("data dir: " + server.getDataDir());
		System.out.println("Shutdown key is: " + server.getShutdownKey());
	}
}
