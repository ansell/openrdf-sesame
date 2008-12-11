/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

import info.aduna.io.FileUtil;

import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

/**
 * Stand alone server for Sesame.
 * 
 * @author James Leigh
 */
public class SesameServer {

	private static final String POM_PROPERTIES = "/META-INF/maven/org.openrdf.sesame/sesame-http-server/pom.properties";

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
				System.out.println(getVersion());
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

			server.start();
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

	}

	private static void printUsage(Options options) {
		System.out.println("Sesame Server, an standalone RDF server.");
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("server [OPTION]", options);
		System.out.println();
		System.out.println("For bug reports and suggestions, see http://www.openrdf.org/");
	}

	private static String getVersion() {
		InputStream in = SesameServer.class.getClassLoader().getResourceAsStream(POM_PROPERTIES);
		if (in == null)
			return null;
		Properties pom = new Properties();
		try {
			pom.load(in);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return (String)pom.get("version");
	}

	public static int DEFAULT_PORT = 8080;

	private File dataDir;

	private Server jetty;

	private RepositoryManager manager;

	private SesameServlet servlet;

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
		this.dataDir = dir;
		if (dataDir == null) {
			dataDir = createTempDir();
		}
		manager = new LocalRepositoryManager(dataDir);
		manager.initialize();
		jetty = new Server(port);
		servlet = new SesameServlet(manager);
		Context root = new Context(jetty, "/");
		root.addServlet(new ServletHolder(servlet), "/*");
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
