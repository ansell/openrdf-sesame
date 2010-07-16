/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import static org.openrdf.query.QueryLanguage.SERQL;
import static org.openrdf.query.QueryLanguage.SPARQL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;
import info.aduna.io.IOUtil;
import info.aduna.iteration.CloseableIteration;
import info.aduna.text.StringUtil;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.Graph;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.serql.SeRQLUtil;
import org.openrdf.query.parser.sparql.SPARQLUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryLockedException;
import org.openrdf.repository.RepositoryReadOnlyException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;
import org.openrdf.sail.helpers.DirectoryLockManager;

/**
 * The Sesame Console is a command-line application for interacting with Sesame.
 * It reads commands from standard input and prints feedback to standard output.
 * Available options include loading and querying of data in repositories,
 * repository creation and verification of RDF files.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public class Console {

	/*------------------*
	 * Static constants *
	 *------------------*/

	private static final AppVersion VERSION = new AppVersion(2, 3, 2);

	private static final String APP_NAME = "OpenRDF Sesame console";

	private static final String TEMPLATES_DIR = "templates";

	public static final Map<String, Level> LOG_LEVELS;

	static {
		Map<String, Level> logLevels = new LinkedHashMap<String, Level>();
		logLevels.put("none", Level.OFF);
		logLevels.put("error", Level.SEVERE);
		logLevels.put("warning", Level.WARNING);
		logLevels.put("info", Level.INFO);
		logLevels.put("debug", Level.FINE);
		LOG_LEVELS = Collections.unmodifiableMap(logLevels);
	}

	/*-----------*
	 * Constants *
	 *-----------*/

	private final AppConfiguration appConfig = new AppConfiguration(APP_NAME, APP_NAME, VERSION);

	private final java.util.logging.Logger jdkRootLogger = java.util.logging.Logger.getLogger("");

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManager manager;

	private String managerID;

	private Repository repository;

	private String repositoryID;

	private BufferedReader in;

	private PrintStream out;

	private int consoleWidth = 80;

	private boolean showPrefix = true;

	private boolean queryPrefix = false;

	/*---------------*
	 * Static metods *
	 *---------------*/

	public static void main(String[] args)
		throws Exception
	{
		Console console = new Console();

		// Parse command line options
		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "print this help");
		Option versionOption = new Option("v", "version", false, "print version information");
		Option serverURLOption = new Option("s", "serverURL", true,
				"URL of Sesame server to connect to, e.g. http://localhost/openrdf-sesame/");
		Option dirOption = new Option("d", "dataDir", true, "Sesame data dir to 'connect' to");

		options.addOption(helpOption);

		OptionGroup connectGroup = new OptionGroup();
		connectGroup.addOption(serverURLOption);
		connectGroup.addOption(dirOption);
		options.addOptionGroup(connectGroup);

		CommandLineParser argsParser = new PosixParser();

		try {
			CommandLine commandLine = argsParser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printUsage(options);
				System.exit(0);
			}

			if (commandLine.hasOption(versionOption.getOpt())) {
				System.out.println(console.appConfig.getFullName());
				System.exit(0);
			}

			String dir = commandLine.getOptionValue(dirOption.getOpt());
			String serverURL = commandLine.getOptionValue(serverURLOption.getOpt());
			String[] otherArgs = commandLine.getArgs();

			if (otherArgs.length > 1) {
				printUsage(options);
				System.exit(1);
			}

			boolean connected = false;
			if (dir != null) {
				connected = console.connectLocal(dir);
			}
			else if (serverURL != null) {
				connected = console.connectRemote(serverURL);
			}
			else {
				connected = console.connectDefault();
			}

			if (!connected) {
				System.exit(2);
			}

			if (otherArgs.length > 0) {
				console.openRepository(otherArgs[0]);
			}
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		console.start();
	}

	private static void printUsage(Options options) {
		System.out.println("Sesame Console, an interactive shell based utility to communicate with Sesame repositories.");
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("start-console [OPTION] [repositoryID]", options);
		// writeln("Usage: start-console [OPTION] [repositoryID]");
		// writeln();
		// writeln(" -h, --help print this help");
		// writeln(" -s, --serverURL=URL URL of Sesame server to connect to, e.g.
		// http://localhost/openrdf-sesame/");
		// writeln(" -d, --dataDir=DIR Sesame data dir to 'connect' to");
		System.out.println();
		System.out.println("For bug reports and suggestions, see http://www.openrdf.org/");
	}

	public Console()
		throws IOException
	{
		// Set log level to WARNING by default
		jdkRootLogger.setLevel(Level.WARNING);

		appConfig.init();

		in = new BufferedReader(new InputStreamReader(System.in));
		out = System.out;
	}

	public void start()
		throws IOException
	{
		writeln();
		writeln("Commands end with '.' at the end of a line");
		writeln("Type 'help.' for help");

		try {
			boolean exitFlag = false;
			while (!exitFlag) {
				String command = readMultiLineInput();

				if (command == null) {
					// EOF
					break;
				}

				exitFlag = executeCommand(command);
			}
		}
		finally {
			disconnect(false);
		}

		writeln("Bye");
	}

	private boolean executeCommand(String command)
		throws IOException
	{
		boolean exit = false;
		String[] tokens = parse(command);
		String operation = tokens[0].toLowerCase(Locale.ENGLISH);

		if ("quit".equals(operation) || "exit".equals(operation)) {
			exit = true;
		}
		else if ("help".equals(operation)) {
			printHelp(tokens);
		}
		else if ("info".equals(operation)) {
			printInfo();
		}
		else if ("connect".equals(operation)) {
			connect(tokens);
		}
		else if ("disconnect".equals(operation)) {
			disconnect(true);
		}
		else if ("create".equals(operation)) {
			createRepository(tokens);
		}
		else if ("drop".equals(operation)) {
			dropRepository(tokens);
		}
		else if ("open".equals(operation)) {
			open(tokens);
		}
		else if ("close".equals(operation)) {
			close(tokens);
		}
		else if ("show".equals(operation)) {
			show(tokens);
		}
		else if ("load".equals(operation)) {
			load(tokens);
		}
		else if ("verify".equals(operation)) {
			verify(tokens);
		}
		else if ("clear".equals(operation)) {
			clear(tokens);
		}
		else if ("select".equals(operation)) {
			// TODO: should this be removed now that the 'serql' command is
			// supported?
			evaluateQuery(QueryLanguage.SERQL, command);
		}
		else if ("construct".equals(operation)) {
			// TODO: should this be removed now that the 'serql' command is
			// supported?
			evaluateQuery(QueryLanguage.SERQL, command);
		}
		else if ("serql".equals(operation)) {
			evaluateQuery(QueryLanguage.SERQL, command.substring("serql".length()));
		}
		else if ("sparql".equals(operation)) {
			evaluateQuery(QueryLanguage.SPARQL, command.substring("sparql".length()));
		}
		else if ("set".equals(operation)) {
			setParameter(tokens);
		}
		else if (command.length() == 0) {
			// empty line, ignore
		}
		else {
			writeError("Unknown command");
		}

		return exit;
	}

	private String[] parse(String command) {
		Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
		Matcher matcher = pattern.matcher(command);
		List<String> tokens = new ArrayList<String>();
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				tokens.add(matcher.group(1));
			}
			else {
				tokens.add(matcher.group());
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}

	private void printHelp(String[] tokens) {
		if (tokens.length < 2) {
			printCommandOverview();
		}
		else {
			String target = tokens[1].toLowerCase(Locale.ENGLISH);

			if ("connect".equals(target)) {
				printHelpConnect();
			}
			else if ("disconnect".equals(target)) {
				printHelpDisconnect();
			}
			else if ("create".equals(target)) {
				printHelpCreate();
			}
			else if ("drop".equals(target)) {
				printHelpDrop();
			}
			else if ("open".equals(target)) {
				printHelpOpen();
			}
			else if ("close".equals(target)) {
				printHelpClose();
			}
			else if ("show".equals(target)) {
				printHelpShow();
			}
			else if ("load".equals(target)) {
				printHelpLoad();
			}
			else if ("verify".equals(target)) {
				printHelpVerify();
			}
			else if ("clear".equals(target)) {
				printHelpClear();
			}
			else if ("set".equals(target)) {
				printHelpSet();
			}
			else {
				writeln("No info available for command " + tokens[1]);
			}
		}
	}

	private void printCommandOverview() {
		writeln("For more information on a specific command, try 'help <command>.'");
		writeln("List of all commands:");
		writeln("help        Displays this help message");
		writeln("info        Shows info about the console");
		writeln("connect     Connects to a (local or remote) set of repositories");
		writeln("disconnect  Disconnects from the current set of repositories");
		writeln("create      Creates a new repository");
		writeln("drop        Drops a repository");
		writeln("open        Opens a repository to work on, takes a repository ID as argument");
		writeln("close       Closes the current repository");
		writeln("show        Displays an overview of various resources");
		writeln("load        Loads a data file into a repository, takes a file path or URL as argument");
		writeln("verify      Verifies the syntax of an RDF data file, takes a file path or URL as argument");
		writeln("clear       Removes data from a repository");
		writeln("serql       Evaluates the SeRQL query, takes a query as argument");
		writeln("sparql      Evaluates the SPARQL query, takes a query as argument");
		writeln("set         Allows various console parameters to be set");
		writeln("exit, quit  Exit the console");
	}

	private void printInfo() {
		writeln(appConfig.getFullName());
		writeln("Data dir: " + appConfig.getDataDir());
		writeln("Connected to: " + (managerID == null ? "-" : managerID));
	}

	private void printHelpConnect() {
		writeln("Usage:");
		writeln("connect default                         Opens the default repository set for this console");
		writeln("connect <dataDirectory>                 Opens the repository set in the specified data dir");
		writeln("connect <serverURL> [user [password]]   Connects to a Sesame server with optional credentials");
	}

	private void connect(String[] tokens) {
		if (tokens.length < 2) {
			printHelpConnect();
			return;
		}

		String target = tokens[1];

		if ("default".equalsIgnoreCase(target)) {
			connectDefault();
		}
		else {
			try {
				new URL(target);
				// target is a valid URL
				String username = (tokens.length > 2) ? tokens[2] : null;
				String password = (tokens.length > 3) ? tokens[3] : null;
				connectRemote(target, username, password);
			}
			catch (MalformedURLException e) {
				// assume target is a directory path
				connectLocal(target);
			}
		}
	}

	private boolean connectDefault() {
		return installNewManager(new LocalRepositoryManager(appConfig.getDataDir()), "default data directory");
	}

	private boolean connectLocal(String path) {
		File dir = new File(path);
		if (!dir.exists() || !dir.isDirectory()) {
			writeError("Specified path is not an (existing) directory: " + path);
			return false;
		}

		return installNewManager(new LocalRepositoryManager(dir), dir.toString());
	}

	private boolean connectRemote(String url) {
		return connectRemote(url, null, null);
	}

	private boolean connectRemote(final String url, final String user, String pass) {
		if (pass == null) {
			pass = "";
		}

		try {
			// Ping server
			HTTPClient httpClient = new HTTPClient();
			httpClient.setServerURL(url);

			if (user != null) {
				httpClient.setUsernameAndPassword(user, pass);
			}

			// Ping the server
			httpClient.getServerProtocol();

			RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
			manager.setUsernameAndPassword(user, pass);
			return installNewManager(manager, url);
		}
		catch (UnauthorizedException e) {
			if (user != null && pass.length() > 0) {
				writeError("Authentication for user '" + user + "' failed");
				logger.warn("Authentication for user '" + user + "' failed", e);
			}
			else {
				// Ask user for credentials
				try {
					writeln("Authentication required");
					String username = readln("Username:");
					String password = readPassword("Password:");
					connectRemote(url, username, password);
				}
				catch (IOException ioe) {
					writeError("Failed to read user credentials");
					logger.warn("Failed to read user credentials", ioe);
				}
			}
		}
		catch (IOException e) {
			writeError("Failed to access the server: " + e.getMessage());
			logger.warn("Failed to access the server", e);
		}
		catch (RepositoryException e) {
			writeError("Failed to access the server: " + e.getMessage());
			logger.warn("Failed to access the server", e);
		}

		return false;
	}

	private boolean installNewManager(RepositoryManager newManager, String newManagerID) {
		if (newManagerID.equals(managerID)) {
			writeln("Already connected to " + managerID);
			return true;
		}

		try {
			newManager.initialize();

			disconnect(false);
			manager = newManager;
			managerID = newManagerID;

			writeln("Connected to " + managerID);
			return true;
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
			logger.error("Failed to install new manager", e);
			return false;
		}
	}

	private void printHelpDisconnect() {
		writeln("Usage:");
		writeln("disconnect   Disconnects from the current set of repositories or server");
	}

	private void disconnect(boolean verbose) {
		if (manager != null) {
			closeRepository(false);

			writeln("Disconnecting from " + managerID);
			manager.shutDown();
			manager = null;
			managerID = null;
		}
		else if (verbose) {
			writeln("Already disconnected");
		}
	}

	private void printHelpCreate() {
		writeln("Usage:");
		writeln("create <template-name>");
		writeln("  <template-name>   The name of a repository configuration template");
	}

	private void createRepository(String[] tokens)
		throws IOException
	{
		if (tokens.length < 2) {
			printHelpCreate();
		}
		else {
			createRepository(tokens[1]);
		}
	}

	private void createRepository(String templateName)
		throws IOException
	{
		Repository systemRepo = manager.getSystemRepository();

		try {
			// FIXME: remove assumption of .ttl extension
			String templateFileName = templateName + ".ttl";

			File templatesDir = new File(appConfig.getDataDir(), TEMPLATES_DIR);

			File templateFile = new File(templatesDir, templateFileName);
			InputStream templateStream;

			if (templateFile.exists()) {
				if (!templateFile.canRead()) {
					writeError("Not allowed to read template file: " + templateFile);
					return;
				}

				templateStream = new FileInputStream(templateFile);
			}
			else {
				// Try classpath for built-ins
				templateStream = Console.class.getResourceAsStream(templateFileName);

				if (templateStream == null) {
					writeError("No template called " + templateName + " found in " + templatesDir);
					return;
				}
			}

			String template;
			try {
				template = IOUtil.readString(new InputStreamReader(templateStream, "UTF-8"));
			}
			finally {
				templateStream.close();
			}

			ConfigTemplate configTemplate = new ConfigTemplate(template);

			Map<String, String> valueMap = new HashMap<String, String>();
			Map<String, List<String>> variableMap = configTemplate.getVariableMap();

			if (!variableMap.isEmpty()) {
				writeln("Please specify values for the following variables:");
			}

			for (Map.Entry<String, List<String>> entry : variableMap.entrySet()) {
				String var = entry.getKey();
				List<String> values = entry.getValue();

				write(var);
				if (values.size() > 1) {
					write(" (");
					for (int i = 0; i < values.size(); i++) {
						if (i > 0) {
							write("|");
						}
						write(values.get(i));
					}
					write(")");
				}
				if (!values.isEmpty()) {
					write(" [" + values.get(0) + "]");
				}
				write(": ");

				String value = in.readLine();
				if (value == null) {
					// EOF
					return;
				}

				value = value.trim();
				if (value.length() == 0) {
					value = null;
				}
				valueMap.put(var, value);
			}

			String configString = configTemplate.render(valueMap);
			// writeln(configString);

			ValueFactory vf = systemRepo.getValueFactory();

			Graph graph = new GraphImpl(vf);

			RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
			rdfParser.setRDFHandler(new StatementCollector(graph));
			rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);

			Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE,
					RepositoryConfigSchema.REPOSITORY);
			RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
			repConfig.validate();

			if (RepositoryConfigUtil.hasRepositoryConfig(systemRepo, repConfig.getID())) {
				boolean proceed = askProceed(
						"WARNING: you are about to overwrite the configuration of an existing repository!", false);

				if (!proceed) {
					writeln("Create aborted");
					return;
				}
			}

			try {
				RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
				writeln("Repository created");
			}
			catch (RepositoryReadOnlyException e) {
				if (tryToRemoveLock(e, systemRepo)) {
					RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
					writeln("Repository created");
				}
				else {
					writeError("Failed to create repository");
					logger.error("Failed to create repository", e);
				}
			}
		}
		catch (Exception e) {
			writeError(e.getMessage());
			logger.error("Failed to create repository", e);
		}
	}

	private void printHelpDrop() {
		writeln("Usage:");
		writeln("drop <repositoryID>   Drops the repository with the specified id");
	}

	private void dropRepository(String[] tokens)
		throws IOException
	{
		if (tokens.length < 2) {
			printHelpDrop();
			return;
		}

		String id = tokens[1];

		try {
			boolean proceed = askProceed("WARNING: you are about to drop repository '" + id + "'.", true);
			if (proceed) {
				if (id.equals(repositoryID)) {
					closeRepository(false);
				}
				boolean isRemoved = manager.removeRepositoryConfig(id);

				if (isRemoved) {
					writeln("Dropped repository '" + id + "'");
				}
				else {
					writeln("Unknown repository '" + id + "'");
				}
			}
			else {
				writeln("Drop aborted");
			}
		}
		catch (RepositoryConfigException e) {
			writeError("Unable to drop repository '" + id + "': " + e.getMessage());
			logger.warn("Unable to drop repository '" + id + "'", e);
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (tryToRemoveLock(e, manager.getSystemRepository())) {
					dropRepository(tokens);
				}
				else {
					writeError("Failed to drop repository");
					logger.error("Failed to drop repository", e);
				}
			}
			catch (RepositoryException e2) {
				writeError("Failed to restart system: " + e2.getMessage());
				logger.error("Failed to restart system", e2);
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to update configuration in system repository: " + e.getMessage());
			logger.warn("Failed to update configuration in system repository", e);
		}
	}

	private void printHelpOpen() {
		writeln("Usage:");
		writeln("open <repositoryID>   Opens the repository with the specified ID");
	}

	private void open(String[] tokens) {
		if (tokens.length != 2) {
			printHelpOpen();
		}
		else {
			openRepository(tokens[1]);
		}
	}

	private void openRepository(String id) {
		try {
			Repository newRepository = manager.getRepository(id);

			if (newRepository != null) {
				// Close current repository, if any
				closeRepository(false);

				repository = newRepository;
				repositoryID = id;
				writeln("Opened repository '" + id + "'");
			}
			else {
				writeError("Unknown repository: '" + id + "'");
			}
		}
		catch (RepositoryLockedException e) {
			try {
				if (tryToRemoveLock(e)) {
					openRepository(id);
				}
				else {
					writeError("Failed to open repository");
					logger.error("Failed to open repository", e);
				}
			}
			catch (IOException e1) {
				writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (RepositoryConfigException e) {
			writeError(e.getMessage());
			logger.error("Failed to open repository", e);
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
			logger.error("Failed to open repository", e);
		}
	}

	private void printHelpClose() {
		writeln("Usage:");
		writeln("close   Closes the current repository");
	}

	private void close(String[] tokens) {
		if (tokens.length != 1) {
			printHelpClose();
		}
		else {
			closeRepository(true);
		}
	}

	private void closeRepository(boolean verbose) {
		if (repository != null) {
			writeln("Closing repository '" + repositoryID + "'...");
			repository = null;
			repositoryID = null;
		}
		else if (verbose) {
			writeln("There are no open repositories that can be closed");
		}
	}

	private void printHelpShow() {
		writeln("Usage:");
		writeln("show {r, repositories}   Shows all available repositories");
		writeln("show {n, namespaces}     Shows all namespaces");
		writeln("show {c, contexts}       Shows all context identifiers");
	}

	private void show(String[] tokens) {
		if (tokens.length != 2) {
			printHelpShow();
		}
		else {
			String target = tokens[1].toLowerCase(Locale.ENGLISH);

			if ("repositories".equals(target) || "r".equals(target)) {
				showRepositories();
			}
			else if ("namespaces".equals(target) || "n".equals(target)) {
				showNamespaces();
			}
			else if ("contexts".equals(target) || "c".equals(target)) {
				showContexts();
			}
			else {
				writeError("Unknown target '" + tokens[1] + "'");
			}
		}
	}

	private void showRepositories() {
		try {
			Set<String> repIDs = manager.getRepositoryIDs();

			if (repIDs.isEmpty()) {
				writeln("--no repositories found--");
			}
			else {
				writeln("+----------");
				for (String repID : repIDs) {
					write("|" + repID);

					try {
						RepositoryInfo repInfo = manager.getRepositoryInfo(repID);
						if (repInfo.getDescription() != null) {
							write(" (\"" + repInfo.getDescription() + "\")");
						}
					}
					catch (RepositoryException e) {
						write(" [ERROR: " + e.getMessage() + "]");
					}
					writeln();
				}
				writeln("+----------");
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to get repository list: " + e.getMessage());
			logger.error("Failed to get repository list", e);
		}
	}

	private void showNamespaces() {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		RepositoryConnection con;
		try {
			con = repository.getConnection();

			try {
				CloseableIteration<? extends Namespace, RepositoryException> namespaces = con.getNamespaces();

				try {
					if (namespaces.hasNext()) {
						writeln("+----------");
						while (namespaces.hasNext()) {
							Namespace ns = namespaces.next();
							writeln("|" + ns.getPrefix() + "  " + ns.getName());
						}
						writeln("+----------");
					}
					else {
						writeln("--no namespaces found--");
					}
				}
				finally {
					namespaces.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
			logger.error("Failed to show namespaces", e);
		}
	}

	private void showContexts() {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		RepositoryConnection con;
		try {
			con = repository.getConnection();

			try {
				CloseableIteration<? extends Resource, RepositoryException> contexts = con.getContextIDs();

				try {
					if (contexts.hasNext()) {
						writeln("+----------");
						while (contexts.hasNext()) {
							Resource context = contexts.next();
							writeln("|" + context.toString());
						}
						writeln("+----------");
					}
					else {
						writeln("--no contexts found--");
					}
				}
				finally {
					contexts.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
			logger.error("Failed to show contexts", e);
		}
	}

	private void printHelpLoad() {
		writeln("Usage:");
		writeln("load <file-or-url> [from <base-uri>] [into <context-id>]");
		writeln("  <file-or-url>   The path or URL identifying the data file");
		writeln("  <base-uri>      The base URI to use for resolving relative references, defaults to <file-or-url>");
		writeln("  <context-id>    The ID of the context to add the data to, e.g. foo:bar or _:n123");
		writeln("Loads the specified data file into the current repository");
	}

	private void load(String[] tokens) {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		if (tokens.length < 2) {
			printHelpLoad();
			return;
		}

		String dataPath = tokens[1];
		URL dataURL = null;
		File dataFile = null;
		String baseURI = null;
		String context = null;

		int i = 2;

		if (tokens.length >= i + 2 && tokens[i].equalsIgnoreCase("from")) {
			baseURI = tokens[i + 1];
			i += 2;
		}

		if (tokens.length >= i + 2 && tokens[i].equalsIgnoreCase("into")) {
			context = tokens[tokens.length - 1];
			i += 2;
		}

		if (i < tokens.length) {
			printHelpLoad();
			return;
		}

		try {
			dataURL = new URL(dataPath);
			// dataPath is a URI
		}
		catch (MalformedURLException e) {
			// dataPath is a file
			dataFile = new File(dataPath);
		}

		try {
			Resource[] contexts = new Resource[0];

			if (context != null) {
				Resource contextURI;
				if (context.startsWith("_:")) {
					contextURI = repository.getValueFactory().createBNode(context.substring(2));
				}
				else {
					contextURI = repository.getValueFactory().createURI(context);
				}

				contexts = new Resource[] { contextURI };
			}

			writeln("Loading data...");
			long startTime = System.nanoTime();

			RepositoryConnection con = repository.getConnection();
			try {
				if (dataURL != null) {
					con.add(dataURL, baseURI, null, contexts);
				}
				else {
					con.add(dataFile, baseURI, null, contexts);
				}
			}
			finally {
				con.close();
			}

			long endTime = System.nanoTime();
			writeln("Data has been added to the repository (" + (endTime - startTime) / 1000000 + " ms)");
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (tryToRemoveLock(e, repository)) {
					load(tokens);
				}
				else {
					writeError("Failed to load data");
					logger.error("Failed to load data", e);
				}
			}
			catch (RepositoryException e1) {
				writeError("Unable to restart repository: " + e1.getMessage());
				logger.error("Unable to restart repository", e1);
			}
			catch (IOException e1) {
				writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (MalformedURLException e) {
			writeError("Malformed URL: " + dataPath);
		}
		catch (IllegalArgumentException e) {
			// Thrown when context URI is invalid
			writeError(e.getMessage());
		}
		catch (IOException e) {
			writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			writeError("Malformed document: " + e.getMessage());
		}
		catch (RepositoryException e) {
			writeError("Unable to add data to repository: " + e.getMessage());
			logger.error("Failed to add data to repository", e);
		}
	}

	private void printHelpVerify() {
		writeln("Usage:");
		writeln("verify <file-or-url>");
		writeln("  <file-or-url>   The path or URL identifying the data file");
		writeln("Verifies the validity of the specified data file");
	}

	private void verify(String[] tokens) {
		if (tokens.length != 2) {
			printHelpVerify();
			return;
		}

		String dataPath = tokens[1];

		try {
			new URL(dataPath);
			// dataPath is a URI
		}
		catch (MalformedURLException e) {
			// File path specified, convert to URL
			dataPath = "file:" + dataPath;
		}

		try {
			URL dataURL = new URL(dataPath);
			RDFFormat format = Rio.getParserFormatForFileName(dataPath, RDFFormat.RDFXML);

			writeln("RDF Format is " + format.getName());

			RDFParser parser = Rio.createParser(format);
			VerificationListener listener = new VerificationListener();
			parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
			parser.setVerifyData(true);
			parser.setParseErrorListener(listener);
			parser.setRDFHandler(listener);

			writeln("Verifying data...");
			InputStream in = dataURL.openStream();
			try {
				parser.parse(in, "urn://openrdf.org/RioVerifier/");
			}
			finally {
				in.close();
			}

			int warnings = listener.getWarnings();
			int errors = listener.getErrors();
			int statements = listener.getStatements();

			if (warnings + errors > 0) {
				writeln("Found " + warnings + " warnings and " + errors + " errors");
			}
			else {
				writeln("Data verified, no errors were found");
			}

			if (errors == 0) {
				writeln("File contains " + statements + " statements");
			}
		}
		catch (MalformedURLException e) {
			writeError("Malformed URL: " + dataPath);
		}
		catch (IOException e) {
			writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			// Any parse errors have already been reported by the
			// VerificationListener
		}
		catch (RDFHandlerException e) {
			writeError("Unable to verify : " + e.getMessage());
			logger.error("Unable to verify data file", e);
		}
	}

	private void printHelpClear() {
		writeln("Usage:");
		writeln("clear                   Clears the entire repository");
		writeln("clear (<uri>|null)...   Clears the specified context(s)");
	}

	private void clear(String[] tokens) {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		ValueFactory valueFactory = repository.getValueFactory();

		Resource[] contexts = new Resource[tokens.length - 1];

		for (int i = 1; i < tokens.length; i++) {
			String contextID = tokens[i];

			if (contextID.equalsIgnoreCase("null")) {
				contexts[i - 1] = null;
			}
			else if (contextID.startsWith("_:")) {
				contexts[i - 1] = valueFactory.createBNode(contextID.substring(2));
			}
			else {
				try {
					contexts[i - 1] = valueFactory.createURI(contextID);
				}
				catch (IllegalArgumentException e) {
					writeError("illegal URI: " + contextID);
					printHelpClear();
					return;
				}
			}
		}

		if (contexts.length == 0) {
			writeln("Clearing repository...");
		}
		else {
			writeln("Removing specified contexts...");
		}

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.clear(contexts);
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (tryToRemoveLock(e, repository)) {
					clear(tokens);
				}
				else {
					writeError("Failed to clear repository");
					logger.error("Failed to clear repository", e);
				}
			}
			catch (RepositoryException e1) {
				writeError("Unable to restart repository: " + e1.getMessage());
				logger.error("Unable to restart repository", e1);
			}
			catch (IOException e1) {
				writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to clear repository: " + e.getMessage());
			logger.error("Failed to clear repository", e);
		}
	}

	private void evaluateQuery(QueryLanguage ql, String queryString) {
		try {
			queryString = addQueryPrefixes(ql, queryString);
			ParsedQuery query = QueryParserUtil.parseQuery(ql, queryString, null);
			if (query instanceof ParsedTupleQuery) {
				evaluateTupleQuery(ql, queryString);
			}
			else if (query instanceof ParsedGraphQuery) {
				evaluateGraphQuery(ql, queryString);
			}
			else if (query instanceof ParsedBooleanQuery) {
				evaluateBooleanQuery(ql, queryString);
			}
			else {
				writeError("Unexpected query type");
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			writeError("Unsupported query lanaguge: " + ql.getName());
		}
		catch (MalformedQueryException e) {
			writeError("Malformed query: " + e.getMessage());
		}
		catch (QueryInterruptedException e) {
			writeError("Query interrupted: " + e.getMessage());
			logger.error("Query interrupted", e);
		}
		catch (QueryEvaluationException e) {
			writeError("Query evaluation error: " + e.getMessage());
			logger.error("Query evaluation error", e);
		}
		catch (RepositoryException e) {
			writeError("Failed to evaluate query: " + e.getMessage());
			logger.error("Failed to evaluate query", e);
		}
	}

	private String addQueryPrefixes(QueryLanguage ql, String queryString) {
		String result = queryString;

		if (repository != null && queryPrefix) {
			// FIXME this is a bit of a sloppy hack, a better way would be to
			// explicitly provide the query parser with namespace mappings in
			// advance.
			if ((SERQL.equals(ql) && queryString.toLowerCase().indexOf("using namespace ") == -1)
					|| SPARQL.equals(ql) && !queryString.toLowerCase().startsWith("prefix"))
			{
				try {
					RepositoryConnection con = repository.getConnection();
					try {
						Collection<Namespace> namespaces = con.getNamespaces().asList();

						if (!namespaces.isEmpty()) {
							StringBuilder namespaceClause = new StringBuilder(512);

							if (SERQL.equals(ql)) {
								namespaceClause.append(" USING NAMESPACE ");

								for (Namespace namespace : namespaces) {
									namespaceClause.append(namespace.getPrefix());
									namespaceClause.append(" = ");
									namespaceClause.append("<");
									namespaceClause.append(SeRQLUtil.encodeString(namespace.getName()));
									namespaceClause.append(">, ");
								}

								// Remove trailing ", "
								namespaceClause.setLength(namespaceClause.length() - 2);

								result += namespaceClause.toString();
							}
							else if (SPARQL.equals(ql)) {
								for (Namespace namespace : namespaces) {
									namespaceClause.append("PREFIX ");
									namespaceClause.append(namespace.getPrefix());
									namespaceClause.append(": ");
									namespaceClause.append("<");
									namespaceClause.append(SPARQLUtil.encodeString(namespace.getName()));
									namespaceClause.append("> ");
								}

								result = namespaceClause.toString() + result;
							}
						}
					}
					finally {
						con.close();
					}
				}
				catch (RepositoryException e) {
					writeError("Error connecting to repository: " + e.getMessage());
					logger.error("Error connecting to repository", e);
				}
			}
		}

		return result;
	}

	private void evaluateTupleQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		RepositoryConnection con = repository.getConnection();

		try {
			writeln("Evaluating query...");
			long startTime = System.nanoTime();

			Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());

			TupleQueryResult tupleQueryResult = con.prepareTupleQuery(ql, queryString).evaluate();

			try {
				int resultCount = 0;
				List<String> bindingNames = tupleQueryResult.getBindingNames();

				if (bindingNames.isEmpty()) {
					while (tupleQueryResult.hasNext()) {
						tupleQueryResult.next();
						resultCount++;
					}
				}
				else {
					int columnWidth = (consoleWidth - 1) / bindingNames.size() - 3;

					// Build table header
					StringBuilder sb = new StringBuilder(consoleWidth);
					for (String bindingName : bindingNames) {
						sb.append("| ").append(bindingName);
						StringUtil.appendN(' ', columnWidth - bindingName.length(), sb);
					}
					sb.append("|");
					String header = sb.toString();

					// Build separator line
					sb.setLength(0);
					for (int i = bindingNames.size(); i > 0; i--) {
						sb.append('+');
						StringUtil.appendN('-', columnWidth + 1, sb);
					}
					sb.append('+');
					String separatorLine = sb.toString();

					// Write table header
					writeln(separatorLine);
					writeln(header);
					writeln(separatorLine);

					// Write table rows

					while (tupleQueryResult.hasNext()) {
						BindingSet bindingSet = tupleQueryResult.next();
						resultCount++;

						sb.setLength(0);
						for (String bindingName : bindingNames) {
							Value value = bindingSet.getValue(bindingName);
							String valueStr = getStringRepForValue(value, namespaces);

							sb.append("| ").append(valueStr);
							StringUtil.appendN(' ', columnWidth - valueStr.length(), sb);
						}
						sb.append("|");
						writeln(sb.toString());
					}

					writeln(separatorLine);
				}

				long endTime = System.nanoTime();
				writeln(resultCount + " result(s) (" + (endTime - startTime) / 1000000 + " ms)");
			}
			finally {
				tupleQueryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	private void evaluateGraphQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		RepositoryConnection con = repository.getConnection();

		try {
			writeln("Evaluating query...");
			long startTime = System.nanoTime();

			Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());

			GraphQueryResult queryResult = con.prepareGraphQuery(ql, queryString).evaluate();

			try {
				int resultCount = 0;

				while (queryResult.hasNext()) {
					Statement st = queryResult.next();
					resultCount++;

					write(getStringRepForValue(st.getSubject(), namespaces));
					write("   ");
					write(getStringRepForValue(st.getPredicate(), namespaces));
					write("   ");
					write(getStringRepForValue(st.getObject(), namespaces));
					writeln();
				}

				long endTime = System.nanoTime();
				writeln(resultCount + " results (" + (endTime - startTime) / 1000000 + " ms)");
			}
			finally {
				queryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	private void evaluateBooleanQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		RepositoryConnection con = repository.getConnection();

		try {
			writeln("Evaluating query...");
			long startTime = System.nanoTime();

			boolean booleanQueryResult = con.prepareBooleanQuery(ql, queryString).evaluate();

			writeln("Answer: " + booleanQueryResult);

			long endTime = System.nanoTime();
			writeln("Query evaluated in " + (endTime - startTime) / 1000000 + " ms");
		}
		finally {
			con.close();
		}
	}

	/**
	 * @param namespace
	 * @param namespaces
	 * @return
	 */
	private String getPrefixForNamespace(String namespace, Collection<Namespace> namespaces) {
		for (Namespace ns : namespaces) {
			if (namespace.equals(ns.getName())) {
				return ns.getPrefix();
			}
		}
		return null;
	}

	private String getStringRepForValue(Value value, Collection<Namespace> namespaces) {
		if (value == null) {
			return "";
		}
		else if (showPrefix && value instanceof URI) {
			URI uri = (URI)value;

			String prefix = getPrefixForNamespace(uri.getNamespace(), namespaces);

			if (prefix != null) {
				return prefix + ":" + uri.getLocalName();
			}
			else {
				return NTriplesUtil.toNTriplesString(value);
			}
		}
		else {
			return NTriplesUtil.toNTriplesString(value);
		}
	}

	private void printHelpSet() {
		writeln("Usage:");
		writeln("set                            Shows all parameter values");
		writeln("set width=<number>             Set the width for query result tables");
		writeln("set log=<level>                Set the logging level (none, error, warning, info or debug)");
		writeln("set showPrefix=<true|false>    Toggles use of prefixed names in query results");
		writeln("set queryPrefix=<true|false>   Toggles automatic use of known namespace prefixes in queries (warning: buggy!)");
	}

	private void setParameter(String[] tokens) {
		if (tokens.length == 1) {
			showParameters();
		}
		else if (tokens.length == 2) {
			String param = tokens[1];

			String key, value;

			int eqIdx = param.indexOf('=');
			if (eqIdx == -1) {
				key = param;
				value = null;
			}
			else {
				key = param.substring(0, eqIdx);
				value = param.substring(eqIdx + 1);
			}

			setParameter(key, value);
		}
		else {
			printHelpSet();
		}
	}

	private void showParameters() {
		setLog(null);
		setWidth(null);
		setShowPrefix(null);
		setQueryPrefix(null);
	}

	private void setParameter(String key, String value) {
		key = key.toLowerCase(Locale.ENGLISH);

		if ("log".equals(key)) {
			setLog(value);
		}
		else if ("width".equals(key)) {
			setWidth(value);
		}
		else if ("showprefix".equals(key)) {
			setShowPrefix(value);
		}
		else if ("queryprefix".equals(key)) {
			setQueryPrefix(value);
		}
		else {
			writeError("unknown parameter: " + key);
		}
	}

	private void setWidth(String value) {
		if (value == null) {
			writeln("width: " + consoleWidth);
		}
		else {
			try {
				int width = Integer.parseInt(value);
				if (width > 0) {
					consoleWidth = width;
				}
				else {
					writeError("Width must be larger than 0");
				}
			}
			catch (NumberFormatException e) {
				writeError("Width must be a positive number");
			}
		}
	}

	private void setLog(String value) {
		if (value == null) {
			Level currentLevel = jdkRootLogger.getLevel();
			String levelString = currentLevel.getName();

			for (Map.Entry<String, Level> entry : LOG_LEVELS.entrySet()) {
				if (entry.getValue().equals(currentLevel)) {
					levelString = entry.getKey();
					break;
				}
			}

			writeln("log: " + levelString);
		}
		else {
			Level logLevel = LOG_LEVELS.get(value.toLowerCase());

			if (logLevel != null) {
				jdkRootLogger.setLevel(logLevel);
			}
			else {
				writeError("unknown logging level: " + value);
			}
		}
	}

	private void setShowPrefix(String value) {
		if (value == null) {
			writeln("showPrefix: " + showPrefix);
		}
		else {
			showPrefix = Boolean.parseBoolean(value);
		}
	}

	private void setQueryPrefix(String value) {
		if (value == null) {
			writeln("queryPrefix: " + queryPrefix);
		}
		else {
			queryPrefix = Boolean.parseBoolean(value);
		}
	}

	private boolean tryToRemoveLock(RepositoryReadOnlyException e, Repository repo)
		throws IOException, RepositoryException
	{
		boolean lockRemoved = false;

		LockManager lockManager = new DirectoryLockManager(repo.getDataDir());

		if (lockManager.isLocked()) {
			if (askProceed("WARNING: The lock from another process on this repository needs to be removed", true))
			{
				repo.shutDown();
				lockRemoved = lockManager.revokeLock();
				repo.initialize();
			}
		}

		return lockRemoved;
	}

	private boolean tryToRemoveLock(RepositoryLockedException e)
		throws IOException
	{
		boolean lockRemoved = false;

		if (e.getCause() instanceof SailLockedException) {
			SailLockedException sle = (SailLockedException)e.getCause();

			LockManager lockManager = sle.getLockManager();

			if (lockManager != null && lockManager.isLocked()) {
				if (askProceed("WARNING: The lock from process '" + sle.getLockedBy()
						+ "' on this repository needs to be removed", true))
				{
					lockRemoved = lockManager.revokeLock();
				}
			}
		}

		return lockRemoved;
	}

	private boolean askProceed(String msg, boolean defaultValue)
		throws IOException
	{
		String defaultString = defaultValue ? "yes" : "no";

		while (true) {
			writeln(msg);
			write("Proceed? (yes|no) [" + defaultString + "]: ");
			String reply = in.readLine();

			if ("no".equalsIgnoreCase(reply) || "no.".equalsIgnoreCase(reply)) {
				return false;
			}
			else if ("yes".equalsIgnoreCase(reply) || "yes.".equalsIgnoreCase(reply)) {
				return true;
			}
			else if (reply.trim().length() == 0) {
				return defaultValue;
			}
		}
	}

	/**
	 * Reads multiple lines from the input until a line that ends with a '.' is
	 * read.
	 */
	private String readMultiLineInput()
		throws IOException
	{
		if (repositoryID != null) {
			write(repositoryID);
		}
		write("> ");

		String line = in.readLine();
		if (line == null) {
			// EOF
			return null;
		}

		StringBuilder buf = new StringBuilder(256);
		buf.append(line);

		while (line != null && !line.endsWith(".")) {
			line = in.readLine();
			buf.append('\n');
			buf.append(line);
		}

		// Remove closing dot
		buf.setLength(buf.length() - 1);

		return buf.toString().trim();
	}

	private String readln(String message)
		throws IOException
	{
		if (message != null) {
			write(message + " ");
		}
		return in.readLine();
	}

	private String readPassword(String message)
		throws IOException
	{
		// TODO: Proper password reader
		return readln(message);
	}

	private void write(String s) {
		out.print(s);
	}

	private void writeln() {
		out.println();
	}

	private void writeln(String s) {
		out.println(s);
	}

	private void writeError(String errMsg) {
		writeln("ERROR: " + errMsg);
	}

	private void writeParseError(String prefix, int lineNo, int colNo, String msg) {
		StringBuilder sb = new StringBuilder(256);

		sb.append(prefix);
		sb.append(": ");
		sb.append(msg);

		String locationString = RDFParseException.getLocationString(lineNo, colNo);
		if (locationString.length() > 0) {
			sb.append(" ").append(locationString);
		}

		writeln(sb.toString());
	}

	class VerificationListener extends RDFHandlerBase implements ParseErrorListener {

		private int warnings;

		private int errors;

		private int statements;

		public int getWarnings() {
			return warnings;
		}

		public int getErrors() {
			return errors;
		}

		public int getStatements() {
			return statements;
		}

		public void handleStatement(Statement st)
			throws RDFHandlerException
		{
			statements++;
		}

		public void warning(String msg, int lineNo, int colNo) {
			warnings++;
			writeParseError("WARNING", lineNo, colNo, msg);
		}

		public void error(String msg, int lineNo, int colNo) {
			errors++;
			writeParseError("ERROR", lineNo, colNo, msg);
		}

		public void fatalError(String msg, int lineNo, int colNo) {
			errors++;
			writeParseError("FATAL ERROR", lineNo, colNo, msg);
		}
	}
}
