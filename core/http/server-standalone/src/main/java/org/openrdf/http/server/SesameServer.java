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
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

/**
 * Stand alone server for Sesame.
 * 
 * @author James Leigh
 */
public class SesameServer {

	public static void main(String[] args)
		throws Exception
	{
		// Parse command line options
		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "print this help");
		Option versionOption = new Option("v", "version", false, "print version information");
		Option dirOption = new Option("d", "dataDir", true, "Sesame data dir to 'connect' to");
		Option portOption = new Option("p", "port", true, "port to listen on");
		Option maxAgeOption = new Option("c", "maxCacheAge", true,
				"How many seconds clients can use their cache before validating it with the server");

		options.addOption(helpOption);
		options.addOption(versionOption);
		options.addOption(dirOption);
		options.addOption(portOption);
		options.addOption(maxAgeOption);

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
				printUsage(options);
				System.exit(1);
			}

			String portString = commandLine.getOptionValue(portOption.getOpt());
			int port = DEFAULT_PORT;
			if (portString != null) {
				port = Integer.parseInt(portString);
			}

			SesameServer server = new SesameServer(new File(dirString), port);

			String ageString = commandLine.getOptionValue(maxAgeOption.getOpt());
			if (ageString != null) {
				server.setMaxCacheAge(Integer.parseInt(ageString));
			}

			server.start();

			System.out.println("Server listening on port " + port);
			System.out.println("data dir: " + server.getDataDir());
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

	public static final int DEFAULT_PORT = 8080;

	private final Server jetty;

	private final LocalRepositoryManager manager;

	private final SesameServlet servlet;

	/**
	 * Creates a new Sesame server that listens to the default port number (
	 * <tt>8080</tt>).
	 * 
	 * @param dataDir
	 *        The data directory for the server.
	 * @throws IOException
	 * @throws StoreConfigException
	 */
	public SesameServer(File dataDir)
		throws IOException, StoreConfigException
	{
		this(dataDir, DEFAULT_PORT);
	}

	public SesameServer(File dataDir, int port)
		throws IOException, StoreConfigException
	{
		assert dataDir != null : "dataDir must not be null";

		manager = new LocalRepositoryManager(dataDir);
		manager.initialize();

		servlet = new SesameServlet(manager);

		jetty = new Server(port);
		Context root = new Context(jetty, "/");
		root.setMaxFormContentSize(0);
		root.addServlet(new ServletHolder(servlet), "/*");
	}

	public File getDataDir() {
		return manager.getBaseDir();
	}

	public void setMaxCacheAge(int maxCacheAge) {
		servlet.setMaxCacheAge(maxCacheAge);
	}

	public RepositoryManager getRepositoryManager() {
		return manager;
	}

	public void start()
		throws Exception
	{
		jetty.start();
	}

	public void stop()
		throws Exception
	{
		jetty.stop();
	}

}
