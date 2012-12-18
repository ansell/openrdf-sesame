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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

import org.openrdf.Sesame;
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
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
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

	private static final String PLEASE_OPEN_FIRST = "please open a repository first";

	private static final String OUTPUT_SEPARATOR = "+----------";

	private static final String USAGE = "Usage:";

	private static final AppVersion VERSION = AppVersion.parse(Sesame.getVersion());

	private static final String APP_NAME = "OpenRDF Sesame console";

	private static final String TEMPLATES_DIR = "templates";

	/*-----------*
	 * Constants *
	 *-----------*/

	private final AppConfiguration appConfig = new AppConfiguration(APP_NAME, APP_NAME, VERSION);

	private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManager manager;

	private String managerID;

	private Repository repository;

	private String repositoryID;

	private final BufferedReader in;

	private final PrintStream out;

	private int consoleWidth = 80;

	private boolean showPrefix = true;

	private boolean queryPrefix = false;

	/*---------------*
	 * Static metods *
	 *---------------*/

	public static void main(final String[] args)
		throws Exception
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
			if (dir != null) {
				connected = console.connectLocal(dir);
			}
			else if (serverURL == null) {
				connected = console.connectDefault();
			}
			else {
				connected = console.connectRemote(serverURL);
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

	private static void printUsage(final Options options) {
		System.out.println("Sesame Console, an interactive shell based utility to communicate with Sesame repositories.");
		final HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("start-console [OPTION] [repositoryID]", options);
		System.out.println();
		System.out.println("For bug reports and suggestions, see http://www.openrdf.org/");
	}

	public Console()
		throws IOException
	{
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
				final String command = readMultiLineInput();
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
			else {
				writeError("Unknown command");
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

	private void printHelp(final String[] tokens) {
		if (tokens.length < 2) {
			printCommandOverview();
		}
		else {
			final String target = tokens[1].toLowerCase(Locale.ENGLISH);
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
		writeln(USAGE);
		writeln("connect default                         Opens the default repository set for this console");
		writeln("connect <dataDirectory>                 Opens the repository set in the specified data dir");
		writeln("connect <serverURL> [user [password]]   Connects to a Sesame server with optional credentials");
	}

	private void connect(final String[] tokens) {
		if (tokens.length < 2) {
			printHelpConnect();
			return;
		}
		final String target = tokens[1];
		if ("default".equalsIgnoreCase(target)) {
			connectDefault();
		}
		else {
			try {
				new URL(target);
				// target is a valid URL
				final String username = (tokens.length > 2) ? tokens[2] : null;
				final String password = (tokens.length > 3) ? tokens[3] : null;
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

	private boolean connectLocal(final String path) {
		final File dir = new File(path);
		boolean result = false;
		if (dir.exists() && dir.isDirectory()) {
			result = installNewManager(new LocalRepositoryManager(dir), dir.toString());
		}
		else {
			writeError("Specified path is not an (existing) directory: " + path);
		}
		return result;
	}

	private boolean connectRemote(final String url) {
		return connectRemote(url, null, null);
	}

	private boolean connectRemote(final String url, final String user, final String passwd) {
		final String pass = (passwd == null) ? "" : passwd;
		boolean result = false;
		try {
			// Ping server
			final HTTPClient httpClient = new HTTPClient();
			try {
				httpClient.setServerURL(url);

				if (user != null) {
					httpClient.setUsernameAndPassword(user, pass);
				}

				// Ping the server
				httpClient.getServerProtocol();
			}
			finally {
				httpClient.shutDown();
			}
			final RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
			manager.setUsernameAndPassword(user, pass);
			result = installNewManager(manager, url);
		}
		catch (UnauthorizedException e) {
			if (user != null && pass.length() > 0) {
				writeError("Authentication for user '" + user + "' failed");
				LOGGER.warn("Authentication for user '" + user + "' failed", e);
			}
			else {
				// Ask user for credentials
				try {
					writeln("Authentication required");
					final String username = readln("Username:");
					final String password = readPassword("Password:");
					connectRemote(url, username, password);
				}
				catch (IOException ioe) {
					writeError("Failed to read user credentials");
					LOGGER.warn("Failed to read user credentials", ioe);
				}
			}
		}
		catch (IOException e) {
			writeError("Failed to access the server: " + e.getMessage());
			LOGGER.warn("Failed to access the server", e);
		}
		catch (RepositoryException e) {
			writeError("Failed to access the server: " + e.getMessage());
			LOGGER.warn("Failed to access the server", e);
		}

		return result;
	}

	private boolean installNewManager(final RepositoryManager newManager, final String newManagerID) {
		boolean installed = false;
		if (newManagerID.equals(managerID)) {
			writeln("Already connected to " + managerID);
			installed = true;
		}
		else {
			try {
				newManager.initialize();
				disconnect(false);
				manager = newManager;
				managerID = newManagerID;
				writeln("Connected to " + managerID);
				installed = true;
			}
			catch (RepositoryException e) {
				writeError(e.getMessage());
				LOGGER.error("Failed to install new manager", e);
			}
		}
		return installed;
	}

	private void printHelpDisconnect() {
		writeln(USAGE);
		writeln("disconnect   Disconnects from the current set of repositories or server");
	}

	private void disconnect(final boolean verbose) {
		if (manager == null) {
			if (verbose) {
				writeln("Already disconnected");
			}
		}
		else {
			closeRepository(false);
			writeln("Disconnecting from " + managerID);
			manager.shutDown();
			manager = null;
			managerID = null;
		}
	}

	private void printHelpCreate() {
		writeln(USAGE);
		writeln("create <template-name>");
		writeln("  <template-name>   The name of a repository configuration template");
	}

	private void createRepository(final String[] tokens)
		throws IOException
	{
		if (tokens.length < 2) {
			printHelpCreate();
		}
		else {
			createRepository(tokens[1]);
		}
	}

	private void createRepository(final String templateName)
		throws IOException
	{
		final Repository systemRepo = manager.getSystemRepository();
		try {
			// FIXME: remove assumption of .ttl extension
			final String templateFileName = templateName + ".ttl";
			final File templatesDir = new File(appConfig.getDataDir(), TEMPLATES_DIR);
			final File templateFile = new File(templatesDir, templateFileName);
			InputStream templateStream;
			if (templateFile.exists()) {
				if (!templateFile.canRead()) {
					writeError("Not allowed to read template file: " + templateFile);
					return;
				}
				templateStream = new FileInputStream(templateFile);
			}
			else {
				// Try class path for built-ins
				templateStream = RepositoryConfig.class.getResourceAsStream(templateFileName);
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
			final ConfigTemplate configTemplate = new ConfigTemplate(template);
			final Map<String, String> valueMap = new HashMap<String, String>();
			final Map<String, List<String>> variableMap = configTemplate.getVariableMap();
			if (!variableMap.isEmpty()) {
				writeln("Please specify values for the following variables:");
			}
			for (Map.Entry<String, List<String>> entry : variableMap.entrySet()) {
				final String var = entry.getKey();
				final List<String> values = entry.getValue();
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
			final String configString = configTemplate.render(valueMap);
			final ValueFactory factory = systemRepo.getValueFactory();
			final Graph graph = new GraphImpl(factory);
			final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, factory);
			rdfParser.setRDFHandler(new StatementCollector(graph));
			rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);
			final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE,
					RepositoryConfigSchema.REPOSITORY);
			final RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
			repConfig.validate();
			if (RepositoryConfigUtil.hasRepositoryConfig(systemRepo, repConfig.getID())) {
				final boolean proceed = askProceed(
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
				if (tryToRemoveLock(systemRepo)) {
					RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
					writeln("Repository created");
				}
				else {
					writeError("Failed to create repository");
					LOGGER.error("Failed to create repository", e);
				}
			}
		}
		catch (Exception e) {
			writeError(e.getMessage());
			LOGGER.error("Failed to create repository", e);
		}
	}

	private void printHelpDrop() {
		writeln(USAGE);
		writeln("drop <repositoryID>   Drops the repository with the specified id");
	}

	private void dropRepository(final String[] tokens)
		throws IOException
	{
		if (tokens.length < 2) {
			printHelpDrop();
			return;
		}
		final String repoID = tokens[1];
		try {
			final boolean proceed = askProceed("WARNING: you are about to drop repository '" + repoID + "'.",
					true);
			if (proceed) {
				if (repoID.equals(repositoryID)) {
					closeRepository(false);
				}
				final boolean isRemoved = manager.removeRepository(repoID);
				if (isRemoved) {
					writeln("Dropped repository '" + repoID + "'");
				}
				else {
					writeln("Unknown repository '" + repoID + "'");
				}
			}
			else {
				writeln("Drop aborted");
			}
		}
		catch (RepositoryConfigException e) {
			writeError("Unable to drop repository '" + repoID + "': " + e.getMessage());
			LOGGER.warn("Unable to drop repository '" + repoID + "'", e);
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (tryToRemoveLock(manager.getSystemRepository())) {
					dropRepository(tokens);
				}
				else {
					writeError("Failed to drop repository");
					LOGGER.error("Failed to drop repository", e);
				}
			}
			catch (RepositoryException e2) {
				writeError("Failed to restart system: " + e2.getMessage());
				LOGGER.error("Failed to restart system", e2);
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to update configuration in system repository: " + e.getMessage());
			LOGGER.warn("Failed to update configuration in system repository", e);
		}
	}

	private void printHelpOpen() {
		writeln(USAGE);
		writeln("open <repositoryID>   Opens the repository with the specified ID");
	}

	private void open(final String[] tokens) {
		if (tokens.length == 2) {
			openRepository(tokens[1]);
		}
		else {
			printHelpOpen();
		}
	}

	private static final String OPEN_FAILURE = "Failed to open repository";

	private void openRepository(final String repoID) {
		try {
			final Repository newRepository = manager.getRepository(repoID);

			if (newRepository == null) {
				writeError("Unknown repository: '" + repoID + "'");
			}
			else {
				// Close current repository, if any
				closeRepository(false);
				repository = newRepository;
				repositoryID = repoID;
				writeln("Opened repository '" + repoID + "'");
			}
		}
		catch (RepositoryLockedException e) {
			try {
				if (tryToRemoveLock(e)) {
					openRepository(repoID);
				}
				else {
					writeError(OPEN_FAILURE);
					LOGGER.error(OPEN_FAILURE, e);
				}
			}
			catch (IOException e1) {
				writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (RepositoryConfigException e) {
			writeError(e.getMessage());
			LOGGER.error(OPEN_FAILURE, e);
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
			LOGGER.error(OPEN_FAILURE, e);
		}
	}

	private void printHelpClose() {
		writeln(USAGE);
		writeln("close   Closes the current repository");
	}

	private void close(final String[] tokens) {
		if (tokens.length == 1) {
			closeRepository(true);
		}
		else {
			printHelpClose();
		}
	}

	private void closeRepository(final boolean verbose) {
		if (repository == null) {
			if (verbose) {
				writeln("There are no open repositories that can be closed");
			}
		}
		else {
			writeln("Closing repository '" + repositoryID + "'...");
			repository = null;
			repositoryID = null;
		}
	}

	private void printHelpShow() {
		writeln(USAGE);
		writeln("show {r, repositories}   Shows all available repositories");
		writeln("show {n, namespaces}     Shows all namespaces");
		writeln("show {c, contexts}       Shows all context identifiers");
	}

	private void show(final String[] tokens) {
		if (tokens.length == 2) {
			final String target = tokens[1].toLowerCase(Locale.ENGLISH);
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
		else {
			printHelpShow();
		}
	}

	private void showRepositories() {
		try {
			final Set<String> repIDs = manager.getRepositoryIDs();
			if (repIDs.isEmpty()) {
				writeln("--no repositories found--");
			}
			else {
				writeln(OUTPUT_SEPARATOR);
				for (String repID : repIDs) {
					write("|" + repID);

					try {
						final RepositoryInfo repInfo = manager.getRepositoryInfo(repID);
						if (repInfo.getDescription() != null) {
							write(" (\"" + repInfo.getDescription() + "\")");
						}
					}
					catch (RepositoryException e) {
						write(" [ERROR: " + e.getMessage() + "]");
					}
					writeln();
				}
				writeln(OUTPUT_SEPARATOR);
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to get repository list: " + e.getMessage());
			LOGGER.error("Failed to get repository list", e);
		}
	}

	private void showNamespaces() {
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}

		RepositoryConnection con;
		try {
			con = repository.getConnection();
			try {
				final CloseableIteration<? extends Namespace, RepositoryException> namespaces = con.getNamespaces();
				try {
					if (namespaces.hasNext()) {
						writeln(OUTPUT_SEPARATOR);
						while (namespaces.hasNext()) {
							final Namespace namespace = namespaces.next();
							writeln("|" + namespace.getPrefix() + "  " + namespace.getName());
						}
						writeln(OUTPUT_SEPARATOR);
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
			LOGGER.error("Failed to show namespaces", e);
		}
	}

	private void showContexts() {
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}

		RepositoryConnection con;
		try {
			con = repository.getConnection();
			try {
				final CloseableIteration<? extends Resource, RepositoryException> contexts = con.getContextIDs();
				try {
					if (contexts.hasNext()) {
						writeln(OUTPUT_SEPARATOR);
						while (contexts.hasNext()) {
							writeln("|" + contexts.next().toString());
						}
						writeln(OUTPUT_SEPARATOR);
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
			LOGGER.error("Failed to show contexts", e);
		}
	}

	private void printHelpLoad() {
		writeln(USAGE);
		writeln("load <file-or-url> [from <base-uri>] [into <context-id>]");
		writeln("  <file-or-url>   The path or URL identifying the data file");
		writeln("  <base-uri>      The base URI to use for resolving relative references, defaults to <file-or-url>");
		writeln("  <context-id>    The ID of the context to add the data to, e.g. foo:bar or _:n123");
		writeln("Loads the specified data file into the current repository");
	}

	private void load(final String[] tokens) {
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}
		if (tokens.length < 2) {
			printHelpLoad();
			return;
		}
		final String dataPath = tokens[1];
		URL dataURL = null;
		File dataFile = null;
		String baseURI = null;
		String context = null;
		int index = 2;
		if (tokens.length >= index + 2 && tokens[index].equalsIgnoreCase("from")) {
			baseURI = tokens[index + 1];
			index += 2;
		}
		if (tokens.length >= index + 2 && tokens[index].equalsIgnoreCase("into")) {
			context = tokens[tokens.length - 1];
			index += 2;
		}
		if (index < tokens.length) {
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
			final long startTime = System.nanoTime();
			final RepositoryConnection con = repository.getConnection();
			try {
				if (dataURL == null) {
					con.add(dataFile, baseURI, null, contexts);
				}
				else {
					con.add(dataURL, baseURI, null, contexts);
				}
			}
			finally {
				con.close();
			}
			final long endTime = System.nanoTime();
			writeln("Data has been added to the repository (" + (endTime - startTime) / 1000000 + " ms)");
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (tryToRemoveLock(repository)) {
					load(tokens);
				}
				else {
					writeError("Failed to load data");
					LOGGER.error("Failed to load data", e);
				}
			}
			catch (RepositoryException e1) {
				writeError("Unable to restart repository: " + e1.getMessage());
				LOGGER.error("Unable to restart repository", e1);
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
			LOGGER.error("Failed to add data to repository", e);
		}
	}

	private void printHelpVerify() {
		writeln(USAGE);
		writeln("verify <file-or-url>");
		writeln("  <file-or-url>   The path or URL identifying the data file");
		writeln("Verifies the validity of the specified data file");
	}

	private void verify(final String[] tokens) {
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
			final URL dataURL = new URL(dataPath);
			final RDFFormat format = Rio.getParserFormatForFileName(dataPath, RDFFormat.RDFXML);
			writeln("RDF Format is " + format.getName());
			final RDFParser parser = Rio.createParser(format);
			final VerificationListener listener = new VerificationListener();
			parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
			parser.setVerifyData(true);
			parser.setParseErrorListener(listener);
			parser.setRDFHandler(listener);
			writeln("Verifying data...");
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
				writeln("Found " + warnings + " warnings and " + errors + " errors");
			}
			else {
				writeln("Data verified, no errors were found");
			}
			if (errors == 0) {
				writeln("File contains " + listener.getStatements() + " statements");
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
			LOGGER.error("Unable to verify data file", e);
		}
	}

	private void printHelpClear() {
		writeln(USAGE);
		writeln("clear                   Clears the entire repository");
		writeln("clear (<uri>|null)...   Clears the specified context(s)");
	}

	private void clear(final String[] tokens) {
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}
		final ValueFactory valueFactory = repository.getValueFactory();
		Resource[] contexts = new Resource[tokens.length - 1];
		for (int i = 1; i < tokens.length; i++) {
			final String contextID = tokens[i];
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
			final RepositoryConnection con = repository.getConnection();
			try {
				con.clear(contexts);
				if (contexts.length == 0) {
					con.clearNamespaces();
				}
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryReadOnlyException e) {
			try {
				if (tryToRemoveLock(repository)) {
					clear(tokens);
				}
				else {
					writeError("Failed to clear repository");
					LOGGER.error("Failed to clear repository", e);
				}
			}
			catch (RepositoryException e1) {
				writeError("Unable to restart repository: " + e1.getMessage());
				LOGGER.error("Unable to restart repository", e1);
			}
			catch (IOException e1) {
				writeError("Unable to remove lock: " + e1.getMessage());
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to clear repository: " + e.getMessage());
			LOGGER.error("Failed to clear repository", e);
		}
	}

	private void evaluateQuery(final QueryLanguage queryLn, final String queryText) {
		try {
			final String queryString = addQueryPrefixes(queryLn, queryText);
			final ParsedOperation query = QueryParserUtil.parseOperation(queryLn, queryString, null);
			if (query instanceof ParsedTupleQuery) {
				evaluateTupleQuery(queryLn, queryString);
			}
			else if (query instanceof ParsedGraphQuery) {
				evaluateGraphQuery(queryLn, queryString);
			}
			else if (query instanceof ParsedBooleanQuery) {
				evaluateBooleanQuery(queryLn, queryString);
			}
			else if (query instanceof ParsedUpdate) {
				executeUpdate(queryLn, queryString);
			}
			else {
				writeError("Unexpected query type");
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			writeError("Unsupported query lanaguge: " + queryLn.getName());
		}
		catch (MalformedQueryException e) {
			writeError("Malformed query: " + e.getMessage());
		}
		catch (QueryInterruptedException e) {
			writeError("Query interrupted: " + e.getMessage());
			LOGGER.error("Query interrupted", e);
		}
		catch (QueryEvaluationException e) {
			writeError("Query evaluation error: " + e.getMessage());
			LOGGER.error("Query evaluation error", e);
		}
		catch (RepositoryException e) {
			writeError("Failed to evaluate query: " + e.getMessage());
			LOGGER.error("Failed to evaluate query", e);
		}
		catch (UpdateExecutionException e) {
			writeError("Failed to execute update: " + e.getMessage());
			LOGGER.error("Failed to execute update", e);
		}
	}

	private String addQueryPrefixes(final QueryLanguage queryLn, final String queryString) {
		final StringBuffer result = new StringBuffer(queryString.length() + 512);
		result.append(queryString);
		final String lowerCaseQuery = queryString.toLowerCase(Locale.ENGLISH);
		if (repository != null
				&& queryPrefix
				&& ((SERQL.equals(queryLn) && lowerCaseQuery.indexOf("using namespace ") == -1) || SPARQL.equals(queryLn)
						&& !lowerCaseQuery.startsWith("prefix")))
		{
			// FIXME this is a bit of a sloppy hack, a better way would be to
			// explicitly provide the query parser with name space mappings in
			// advance.
			try {
				final RepositoryConnection con = repository.getConnection();
				try {
					final Collection<Namespace> namespaces = con.getNamespaces().asList();
					if (!namespaces.isEmpty()) {
						final StringBuilder namespaceClause = new StringBuilder(512);
						if (SERQL.equals(queryLn)) {
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
							result.append(namespaceClause.toString());
						}
						else if (SPARQL.equals(queryLn)) {
							for (Namespace namespace : namespaces) {
								namespaceClause.append("PREFIX ");
								namespaceClause.append(namespace.getPrefix());
								namespaceClause.append(": ");
								namespaceClause.append("<");
								namespaceClause.append(SPARQLUtil.encodeString(namespace.getName()));
								namespaceClause.append("> ");
							}
							result.insert(0, namespaceClause);
						}
					}
				}
				finally {
					con.close();
				}
			}
			catch (RepositoryException e) {
				writeError("Error connecting to repository: " + e.getMessage());
				LOGGER.error("Error connecting to repository", e);
			}
		}
		return result.toString();
	}

	private void evaluateTupleQuery(final QueryLanguage queryLn, final String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}
		final RepositoryConnection con = repository.getConnection();
		try {
			writeln("Evaluating query...");
			final long startTime = System.nanoTime();
			final TupleQueryResult tupleQueryResult = con.prepareTupleQuery(queryLn, queryString).evaluate();
			try {
				int resultCount = 0;
				final List<String> bindingNames = tupleQueryResult.getBindingNames();
				if (bindingNames.isEmpty()) {
					while (tupleQueryResult.hasNext()) {
						tupleQueryResult.next();
						resultCount++;
					}
				}
				else {
					final int columnWidth = (consoleWidth - 1) / bindingNames.size() - 3;

					// Build table header
					final StringBuilder builder = new StringBuilder(consoleWidth);
					for (String bindingName : bindingNames) {
						builder.append("| ").append(bindingName);
						StringUtil.appendN(' ', columnWidth - bindingName.length(), builder);
					}
					builder.append("|");
					final String header = builder.toString();

					// Build separator line
					builder.setLength(0);
					for (int i = bindingNames.size(); i > 0; i--) {
						builder.append('+');
						StringUtil.appendN('-', columnWidth + 1, builder);
					}
					builder.append('+');
					final String separatorLine = builder.toString();

					// Write table header
					writeln(separatorLine);
					writeln(header);
					writeln(separatorLine);

					// Write table rows
					final Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());
					while (tupleQueryResult.hasNext()) {
						final BindingSet bindingSet = tupleQueryResult.next();
						resultCount++;
						builder.setLength(0);
						for (String bindingName : bindingNames) {
							final Value value = bindingSet.getValue(bindingName);
							final String valueStr = getStringRepForValue(value, namespaces);
							builder.append("| ").append(valueStr);
							StringUtil.appendN(' ', columnWidth - valueStr.length(), builder);
						}
						builder.append("|");
						writeln(builder.toString());
					}
					writeln(separatorLine);
				}
				final long endTime = System.nanoTime();
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

	private void evaluateGraphQuery(final QueryLanguage queryLn, final String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}
		final RepositoryConnection con = repository.getConnection();
		try {
			writeln("Evaluating query...");
			final long startTime = System.nanoTime();
			final Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());
			final GraphQueryResult queryResult = con.prepareGraphQuery(queryLn, queryString).evaluate();
			try {
				int resultCount = 0;
				while (queryResult.hasNext()) {
					final Statement statement = queryResult.next();
					resultCount++;
					write(getStringRepForValue(statement.getSubject(), namespaces));
					write("   ");
					write(getStringRepForValue(statement.getPredicate(), namespaces));
					write("   ");
					write(getStringRepForValue(statement.getObject(), namespaces));
					writeln();
				}
				final long endTime = System.nanoTime();
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

	private void evaluateBooleanQuery(final QueryLanguage queryLn, final String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}
		final RepositoryConnection con = repository.getConnection();
		try {
			writeln("Evaluating query...");
			final long startTime = System.nanoTime();
			final boolean result = con.prepareBooleanQuery(queryLn, queryString).evaluate();
			writeln("Answer: " + result);
			final long endTime = System.nanoTime();
			writeln("Query evaluated in " + (endTime - startTime) / 1000000 + " ms");
		}
		finally {
			con.close();
		}
	}

	private void executeUpdate(final QueryLanguage queryLn, final String queryString)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		if (repository == null) {
			writeError(PLEASE_OPEN_FIRST);
			return;
		}
		final RepositoryConnection con = repository.getConnection();
		try {
			writeln("Executing update...");
			final long startTime = System.nanoTime();
			con.prepareUpdate(queryLn, queryString).execute();
			final long endTime = System.nanoTime();
			writeln("Update executed in " + (endTime - startTime) / 1000000 + " ms");
		}
		finally {
			con.close();
		}
	}

	private String getPrefixForNamespace(final String namespace, final Collection<Namespace> namespaces) {
		for (Namespace ns : namespaces) {
			if (namespace.equals(ns.getName())) {
				return ns.getPrefix();
			}
		}
		return null;
	}

	private String getStringRepForValue(final Value value, final Collection<Namespace> namespaces) {
		if (value == null) {
			return "";
		}
		else if (showPrefix && value instanceof URI) {
			final URI uri = (URI)value;
			final String prefix = getPrefixForNamespace(uri.getNamespace(), namespaces);
			if (prefix == null) {
				return NTriplesUtil.toNTriplesString(value);
			}
			else {
				return prefix + ":" + uri.getLocalName();
			}
		}
		else {
			return NTriplesUtil.toNTriplesString(value);
		}
	}

	private void printHelpSet() {
		writeln(USAGE);
		writeln("set                            Shows all parameter values");
		writeln("set width=<number>             Set the width for query result tables");
		writeln("set log=<level>                Set the logging level (none, error, warning, info or debug)");
		writeln("set showPrefix=<true|false>    Toggles use of prefixed names in query results");
		writeln("set queryPrefix=<true|false>   Toggles automatic use of known namespace prefixes in queries (warning: buggy!)");
	}

	private void setParameter(final String[] tokens) {
		if (tokens.length == 1) {
			showParameters();
		}
		else if (tokens.length == 2) {
			final String param = tokens[1];
			String key, value;
			final int eqIdx = param.indexOf('=');
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
			writeError("unknown parameter: " + key);
		}
	}

	private void setWidth(final String value) {
		if (value == null) {
			writeln("width: " + consoleWidth);
		}
		else {
			try {
				final int width = Integer.parseInt(value);
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

	private void setShowPrefix(final String value) {
		if (value == null) {
			writeln("showPrefix: " + showPrefix);
		}
		else {
			showPrefix = Boolean.parseBoolean(value);
		}
	}

	private void setQueryPrefix(final String value) {
		if (value == null) {
			writeln("queryPrefix: " + queryPrefix);
		}
		else {
			queryPrefix = Boolean.parseBoolean(value);
		}
	}

	private boolean tryToRemoveLock(final Repository repo)
		throws IOException, RepositoryException
	{
		boolean lockRemoved = false;
		final LockManager lockManager = new DirectoryLockManager(repo.getDataDir());
		if (lockManager.isLocked()
				&& askProceed("WARNING: The lock from another process on this repository needs to be removed",
						true))
		{
			repo.shutDown();
			lockRemoved = lockManager.revokeLock();
			repo.initialize();
		}
		return lockRemoved;
	}

	private boolean tryToRemoveLock(final RepositoryLockedException rle)
		throws IOException
	{
		boolean lockRemoved = false;
		if (rle.getCause() instanceof SailLockedException) {
			final SailLockedException sle = (SailLockedException)rle.getCause();
			final LockManager lockManager = sle.getLockManager();
			if (lockManager != null
					&& lockManager.isLocked()
					&& askProceed("WARNING: The lock from process '" + sle.getLockedBy()
							+ "' on this repository needs to be removed", true))
			{
				lockRemoved = lockManager.revokeLock();
			}
		}
		return lockRemoved;
	}

	private boolean askProceed(final String msg, final boolean defaultValue)
		throws IOException
	{
		final String defaultString = defaultValue ? "yes" : "no";
		while (true) {
			writeln(msg);
			write("Proceed? (yes|no) [" + defaultString + "]: ");
			final String reply = in.readLine();
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
		final StringBuilder buf = new StringBuilder(256);
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

	private String readln(final String message)
		throws IOException
	{
		if (message != null) {
			write(message + " ");
		}
		return in.readLine();
	}

	private String readPassword(final String message)
		throws IOException
	{
		// TODO: Proper password reader
		return readln(message);
	}

	private void write(final String string) {
		out.print(string);
	}

	private void writeln() {
		out.println();
	}

	private void writeln(final String string) {
		out.println(string);
	}

	private void writeError(final String errMsg) {
		writeln("ERROR: " + errMsg);
	}

	private void writeParseError(final String prefix, final int lineNo, final int colNo, final String msg) {
		final StringBuilder builder = new StringBuilder(256);
		builder.append(prefix);
		builder.append(": ");
		builder.append(msg);
		final String locationString = RDFParseException.getLocationString(lineNo, colNo);
		if (locationString.length() > 0) {
			builder.append(" ").append(locationString);
		}
		writeln(builder.toString());
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

		public void handleStatement(final Statement statement)
			throws RDFHandlerException
		{
			statements++;
		}

		public void warning(final String msg, final int lineNo, final int colNo) {
			warnings++;
			writeParseError("WARNING", lineNo, colNo, msg);
		}

		public void error(final String msg, final int lineNo, final int colNo) {
			errors++;
			writeParseError("ERROR", lineNo, colNo, msg);
		}

		public void fatalError(final String msg, final int lineNo, final int colNo) {
			errors++;
			writeParseError("FATAL ERROR", lineNo, colNo, msg);
		}
	}
}
