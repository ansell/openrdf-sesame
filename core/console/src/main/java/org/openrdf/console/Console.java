/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;

import org.openrdf.Sesame;
import org.openrdf.repository.Repository;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * The Sesame Console is a command-line application for interacting with Sesame.
 * It reads commands from standard input and prints feedback to standard output.
 * Available options include loading and querying of data in repositories,
 * repository creation and verification of RDF files.
 * 
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public class Console implements ConsoleState, ConsoleParameters {

	/*------------------*
	 * Static constants *
	 *------------------*/

	private static final AppVersion VERSION = AppVersion.parse(Sesame.getVersion());

	private static final String APP_NAME = "OpenRDF Sesame console";

	/*-----------*
	 * Constants *
	 *-----------*/

	private final AppConfiguration appConfig = new AppConfiguration(APP_NAME, APP_NAME, VERSION);

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManager manager;

	private String managerID;

	private Repository repository;

	private String repositoryID;

	private final ConsoleIO consoleIO;

	private int consoleWidth = 80;

	private boolean showPrefix = true;

	private boolean queryPrefix = false;

	/*----------------*
	 * Static methods *
	 *----------------*/

	public static void main(final String[] args)
		throws IOException
	{
		final Console console = new Console();

		// Parse command line options
		final Options options = new Options();
		final Option helpOption = new Option("h", "help", false, "print this help");
		final Option versionOption = new Option("v", "version", false, "print version information");
		final Option serverURLOption = new Option("s", "serverURL", true,
				"URL of Sesame server to connect to, e.g. http://localhost/openrdf-sesame/");
		final Option dirOption = new Option("d", "dataDir", true, "Sesame data dir to 'connect' to");
		options.addOption(helpOption);
		final OptionGroup connectGroup = new OptionGroup();
		connectGroup.addOption(serverURLOption);
		connectGroup.addOption(dirOption);
		options.addOptionGroup(connectGroup);
		final CommandLineParser argsParser = new PosixParser();
		try {
			final CommandLine commandLine = argsParser.parse(options, args);
			if (commandLine.hasOption(helpOption.getOpt())) {
				printUsage(options);
				System.exit(0);
			}
			if (commandLine.hasOption(versionOption.getOpt())) {
				System.out.println(console.appConfig.getFullName());
				System.exit(0);
			}
			final String dir = commandLine.getOptionValue(dirOption.getOpt());
			final String serverURL = commandLine.getOptionValue(serverURLOption.getOpt());
			final String[] otherArgs = commandLine.getArgs();

			if (otherArgs.length > 1) {
				printUsage(options);
				System.exit(1);
			}

			boolean connected = false;
			if (dir == null) {
				connected = (serverURL == null) ? console.connect.connectDefault()
						: console.connect.connectRemote(serverURL);
			}
			else {
				connected = console.connect.connectLocal(dir);
			}

			if (!connected) {
				System.exit(2);
			}

			if (otherArgs.length > 0) {
				console.open.openRepository(otherArgs[0]);
			}
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		console.start();
	}

	private static void printUsage(final Options options) {
		System.out.println("Sesame Console, an interactive shell based utility to communicate with Sesame repositories.");
		final HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("start-console [OPTION] [repositoryID]", options);
		System.out.println();
		System.out.println("For bug reports and suggestions, see http://www.openrdf.org/");
	}

	private final Map<String, Command> commandMap = new HashMap<String, Command>();

	private final Connect connect;

	private final Disconnect disconnect;

	private final Open open;

	private final QueryEvaluator queryEvaluator;

	public Console()
		throws IOException
	{
		appConfig.init();
		consoleIO = new ConsoleIO(new BufferedReader(new InputStreamReader(System.in)), System.out, this);
		this.queryEvaluator = new QueryEvaluator(consoleIO, this, this);
		LockRemover lockRemover = new LockRemover(consoleIO);
		Close close = new Close(consoleIO, this);
		commandMap.put("close", close);
		this.disconnect = new Disconnect(consoleIO, this, close);
		commandMap.put("help", new PrintHelp(consoleIO));
		commandMap.put("info", new PrintInfo(consoleIO, this));
		this.connect = new Connect(consoleIO, this, disconnect);
		commandMap.put("connect", connect);
		commandMap.put("create", new Create(consoleIO, this, lockRemover));
		commandMap.put("drop", new Drop(consoleIO, this, close, lockRemover));
		this.open = new Open(consoleIO, this, close, lockRemover);
		commandMap.put("open", open);
		commandMap.put("show", new Show(consoleIO, this));
		commandMap.put("load", new Load(consoleIO, this, lockRemover));
		commandMap.put("verify", new Verify(consoleIO));
		commandMap.put("clear", new Clear(consoleIO, this, lockRemover));
		commandMap.put("set", new SetParameters(consoleIO, this));
	}

	public void start()
		throws IOException
	{
		consoleIO.writeln();
		consoleIO.writeln("Commands end with '.' at the end of a line");
		consoleIO.writeln("Type 'help.' for help");

		try {
			boolean exitFlag = false;
			while (!exitFlag) {
				final String command = consoleIO.readMultiLineInput();
				if (command == null) {
					// EOF
					break;
				}
				exitFlag = executeCommand(command);
			}
		}
		finally {
			disconnect.execute(false);
		}
		consoleIO.writeln("Bye");
	}

	private boolean executeCommand(final String command)
		throws IOException
	{
		boolean exit = false;
		if (0 < command.length()) {
			final String[] tokens = parse(command);
			final String operation = tokens[0].toLowerCase(Locale.ENGLISH);
			if ("quit".equals(operation) || "exit".equals(operation)) {
				exit = true;
			}
			else if (commandMap.containsKey(operation)) {
				commandMap.get(operation).execute(tokens);
			}
			else if ("disconnect".equals(operation)) {
				disconnect.execute(true);
			}
			else {
				queryEvaluator.executeQuery(command, operation);
			}
		}

		return exit;
	}

	private String[] parse(final String command) {
		final Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
		final Matcher matcher = pattern.matcher(command);
		final List<String> tokens = new ArrayList<String>();
		while (matcher.find()) {
			if (matcher.group(1) == null) {
				tokens.add(matcher.group());
			}
			else {
				tokens.add(matcher.group(1));
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}

	public String getApplicationName() {
		return this.appConfig.getFullName();
	}

	public File getDataDirectory() {
		return this.appConfig.getDataDir();
	}

	public String getManagerID() {
		return this.managerID;
	}

	public String getRepositoryID() {
		return this.repositoryID;
	}

	public RepositoryManager getManager() {
		return this.manager;
	}

	public void setManager(RepositoryManager manager) {
		this.manager = manager;
	}

	public void setManagerID(String managerID) {
		this.managerID = managerID;
	}

	public Repository getRepository() {
		return this.repository;
	}

	public void setRepositoryID(String repositoryID) {
		this.repositoryID = repositoryID;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public int getWidth() {
		return this.consoleWidth;
	}

	public void setWidth(int width) {
		this.consoleWidth = width;
	}

	public boolean isShowPrefix() {
		return this.showPrefix;
	}

	public void setShowPrefix(boolean value) {
		this.showPrefix = value;
	}

	public boolean isQueryPrefix() {
		return this.queryPrefix;
	}

	public void setQueryPrefix(boolean value) {
		this.queryPrefix = value;
	}
}
