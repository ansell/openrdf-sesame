/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
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

import info.aduna.io.file.FileUtil;

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
		Option nameOption = new Option("n", "name", true, "Server name");
		nameOption.setOptionalArg(true);
		Option dirOption = new Option("d", "dataDir", true, "Sesame data dir to 'connect' to");
		Option portOption = new Option("p", "port", true, "port to listen on");
		Option maxAgeOption = new Option("c", "maxCacheAge", true,
				"How many seconds clients can use their cache before validating it with the server");
		Option urlOption = new Option("u", "url", true,
				"If the server should resolve URLs to URIs and for which repository");
		urlOption.setOptionalArg(true);

		options.addOption(helpOption);
		options.addOption(versionOption);
		options.addOption(nameOption);
		options.addOption(dirOption);
		options.addOption(portOption);
		options.addOption(maxAgeOption);
		options.addOption(urlOption);

		CommandLineParser argsParser = new PosixParser();

		try {
			CommandLine commandLine = argsParser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printUsage(options);
				System.exit(0);
			}

			if (commandLine.hasOption(versionOption.getOpt())) {
				System.out.println(SesameServlet.getDefaultServerName());
				System.exit(0);
			}

			String dir = commandLine.getOptionValue(dirOption.getOpt());
			String[] otherArgs = commandLine.getArgs();

			if (otherArgs.length > 1) {
				printUsage(options);
				System.exit(1);
			}

			String portString = commandLine.getOptionValue(portOption.getOpt());
			int port = DEFAULT_PORT;
			if (portString != null) {
				port = Integer.parseInt(portString);
			}

			SesameServer server;
			if (dir != null) {
				server = new SesameServer(port, new File(dir));
			}
			else {
				server = new SesameServer(port);
			}

			String ageString = commandLine.getOptionValue(maxAgeOption.getOpt());
			if (ageString != null) {
				server.setMaxCacheAge(Integer.parseInt(ageString));
			}

			if (commandLine.hasOption(urlOption.getOpt())) {
				server.setUrlResolution(commandLine.getOptionValue(urlOption.getOpt()));
			}

			if (commandLine.hasOption(nameOption.getOpt())) {
				server.setServerName(commandLine.getOptionValue(nameOption.getOpt()));
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

	private final File dataDir;

	private final Server jetty;

	private final RepositoryManager manager;

	private final SesameServlet servlet;

	public SesameServer()
		throws IOException, StoreConfigException
	{
		this(DEFAULT_PORT, null);
	}

	public SesameServer(int port)
		throws IOException, StoreConfigException
	{
		this(port, null);
	}

	public SesameServer(File dataDir)
		throws IOException, StoreConfigException
	{
		this(DEFAULT_PORT, dataDir);
	}

	public SesameServer(int port, File dir)
		throws IOException, StoreConfigException
	{
		this.dataDir = dir != null ? dir : createTempDir();
		manager = new LocalRepositoryManager(dataDir);
		manager.initialize();
		jetty = new Server(port);
		servlet = new SesameServlet(manager);
		servlet.setServerName(servlet.getServerName() + " Jetty/" + Server.getVersion());
		Context root = new Context(jetty, "/");
		root.setMaxFormContentSize(0);
		root.addServlet(new ServletHolder(servlet), "/*");
	}

	public String getServerName() {
		return servlet.getServerName();
	}

	public void setServerName(String name) {
		if (name == null || name.trim().length() == 0) {
			jetty.setSendServerVersion(false);
			servlet.setServerName(null);
		}
		else {
			servlet.setServerName(name);
		}
	}

	public File getDataDir() {
		return dataDir;
	}

	private File createTempDir()
		throws IOException
	{
		final File dir = FileUtil.createTempDir("sesame");
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					FileUtil.deleteDir(dir);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return dir;
	}

	public void setMaxCacheAge(int maxCacheAge) {
		servlet.setMaxCacheAge(maxCacheAge);
	}

	public void setUrlResolution(String urlResolution) {
		servlet.setUrlResolution(urlResolution);
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
