/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;
import info.aduna.io.IOUtil;
import info.aduna.iteration.CloseableIteration;
import info.aduna.text.StringUtil;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
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
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.serql.SeRQLUtil;
import org.openrdf.query.parser.sparql.SPARQLUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.SystemRepository;
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

/**
 * The Sesame Command Console is a simple command-line application for
 * interacting with Sesame. It reads commands from standard input and prints
 * feedback to standard output. Available options include loading and querying
 * of data in repositories, on-the-fly repository creation and verification of
 * RDF files.
 * <p>
 * Usage (UNIX):
 * <code>[SESAME_DIR]/bin/start-console.sh [-d &lt;datadir&gt; [repository-id]]</code>
 * <br>
 * Usage (Windows):
 * <code>[SESAME_DIR]/bin/start-console.bat [-d &lt;datadir&gt; [repository-id]]</code>
 *
 * @author jeen
 * @author Arjohn Kampman
 */
public class Console {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final AppVersion VERSION = new AppVersion(2, 0, "beta4");

	private static final String APP_NAME = "OpenRDF Sesame console";

	private static final AppConfiguration appConfig = new AppConfiguration(APP_NAME, APP_NAME, VERSION);

	private static final String TEMPLATES_DIR = "templates";

	/**
	 * Query that produces the list of the IDs and (optionally) titles of
	 * configured repositories.
	 */
	public static final String REPOSITORY_LIST_QUERY;

	/**
	 * Query that yields the context of a specific repository configuration.
	 */
	public static final String REPOSITORY_CONTEXT_QUERY;

	public static final Map<String, Level> LOG_LEVELS;

	static {
		StringBuilder query = new StringBuilder(256);
		query.append("SELECT ID, Title ");
		query.append("FROM {} rdf:type {sys:Repository};");
		query.append("        sys:repositoryID {ID};");
		query.append("        [rdfs:label {Title} where isLiteral(Title)] ");
		query.append("WHERE isLiteral(ID) ");
		query.append("USING NAMESPACE sys = <http://www.openrdf.org/config/repository#>");
		REPOSITORY_LIST_QUERY = query.toString();

		query.setLength(0);
		query.append("SELECT C ");
		query.append("FROM CONTEXT C ");
		query.append("   {} rdf:type {sys:Repository};");
		query.append("      sys:repositoryID {ID} ");
		query.append("USING NAMESPACE sys = <http://www.openrdf.org/config/repository#>");
		REPOSITORY_CONTEXT_QUERY = query.toString();

		Map<String, Level> logLevels = new LinkedHashMap<String, Level>();
		logLevels.put("none", Level.OFF);
		logLevels.put("error", Level.SEVERE);
		logLevels.put("warning", Level.WARNING);
		logLevels.put("info", Level.INFO);
		logLevels.put("debug", Level.FINE);
		LOG_LEVELS = Collections.unmodifiableMap(logLevels);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private static Logger rootLogger;

	private static RepositoryManager manager;

	private static Repository repository;

	private static String repositoryID;

	private static BufferedReader in;

	private static int consoleWidth = 100;

	private static boolean usePrefixedNames = true;

	private static boolean appendNamespaces = true;

	/*--------*
	 * Metods *
	 *--------*/

	public static void main(String[] args)
		throws IOException
	{
		// Set log level to WARNING by default
		rootLogger = Logger.getLogger("");
		rootLogger.setLevel(Level.WARNING);

		String repIDArg = null;

		if (args.length >= 2) {
			if (args[0].equals("-d")) {
				appConfig.setDataDirName(args[1]);

				if (args.length >= 3) {
					repIDArg = args[2];
				}
			}
			else {
				printUsage();
				return;
			}
		}

		appConfig.init();

		writeln(appConfig.getFullName());
		writeln("Using data dir: " + appConfig.getDataDir());
		writeln();

		try {
			manager = new RepositoryManager(appConfig.getDataDir());

			if (repIDArg != null) {
				openRepository(repIDArg);
			}
			else {
				writeln("The following repositories are available:");
				showRepositories();
			}
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
		}

		writeln();
		writeln("Commands end with '.' at the end of a line");
		writeln("Type 'help.' for help");

		in = new BufferedReader(new InputStreamReader(System.in));

		boolean exitFlag = false;
		while (!exitFlag) {
			String command = readMultiLineInput();

			if (command == null) {
				// EOF
				break;
			}

			exitFlag = executeCommand(command);
		}

		if (repository != null) {
			closeRepository();

			writeln("Shutting down...");
			manager.shutDown();
		}

		writeln("Bye");
	}

	private static boolean executeCommand(String command)
		throws IOException
	{
		boolean exit = false;
		String[] tokens = command.split("[ \t\r\n]");

		if ("quit".equalsIgnoreCase(tokens[0]) || "exit".equalsIgnoreCase(tokens[0])) {
			exit = true;
		}
		else if ("help".equalsIgnoreCase(tokens[0])) {
			printHelp(tokens);
		}
		else if ("create".equalsIgnoreCase(tokens[0])) {
			if (tokens.length < 2) {
				printHelpCreate();
			}
			else {
				createRepository(tokens);
			}
		}
		else if ("drop".equalsIgnoreCase(tokens[0])) {
			if (tokens.length < 2) {
				printHelpDrop();
			}
			else {
				dropRepository(tokens[1]);
			}
		}
		else if ("open".equalsIgnoreCase(tokens[0])) {
			if (tokens.length < 2) {
				showRepositories();
			}
			else {
				openRepository(tokens[1]);
			}
		}
		else if ("close".equalsIgnoreCase(tokens[0])) {
			closeRepository();
		}
		else if ("show".equalsIgnoreCase(tokens[0])) {
			if (tokens.length < 2) {
				printHelpShow();
			}
			else if ("repositories".equalsIgnoreCase(tokens[1]) || "r".equalsIgnoreCase(tokens[1])) {
				showRepositories();
			}
			else if ("namespaces".equalsIgnoreCase(tokens[1]) || "n".equalsIgnoreCase(tokens[1])) {
				showNamespaces();
			}
			else if ("contexts".equalsIgnoreCase(tokens[1]) || "c".equalsIgnoreCase(tokens[1])) {
				showContexts();
			}
			else {
				writeError("Unknown target '" + tokens[1] + "'");
			}
		}
		else if ("load".equalsIgnoreCase(tokens[0])) {
			if (tokens.length >= 2 && "-c".equals(tokens[1])) {
				if (tokens.length < 4) {
					writeln("Usage: load [-c <context-id-uri>] <data-file-or-url> [<base-uri>].");
				}
				else if (tokens.length == 4) {
					load(tokens[2], tokens[3], null);
				}
				else {
					load(tokens[2], tokens[3], tokens[4]);
				}
			}
			else {
				if (tokens.length < 2) {
					writeln("Usage: load [-c <context-id-uri>] <data-file-or-url> [<base-uri>].");
				}
				else if (tokens.length == 2) {
					load(null, tokens[1], null);
				}
				else {
					load(null, tokens[1], tokens[2]);
				}
			}
		}
		else if ("verify".equalsIgnoreCase(tokens[0])) {
			if (tokens.length != 2) {
				writeln("Usage: verify <data-file-or-url>.");
			}
			else {
				verify(tokens[1]);
			}
		}
		else if ("clear".equalsIgnoreCase(tokens[0])) {
			if (tokens.length >= 2 && "all".equalsIgnoreCase(tokens[1])) {
				clear();
			}
			else if (tokens.length >= 3 && "context".equalsIgnoreCase(tokens[1])) {
				if ("null".equalsIgnoreCase(tokens[2])) {
					clearContext(null);
				}
				else {
					clearContext(tokens[2]);
				}
			}
			else {
				printHelpClear();
			}
		}
		else if ("select".equalsIgnoreCase(tokens[0])) {
			// TODO: should this be removed now that the 'serql' command is
			// supported?
			evaluateQuery(QueryLanguage.SERQL, command);
		}
		else if ("construct".equalsIgnoreCase(tokens[0])) {
			// TODO: should this be removed now that the 'serql' command is
			// supported?
			evaluateQuery(QueryLanguage.SERQL, command);
		}
		else if ("serql".equalsIgnoreCase(tokens[0])) {
			evaluateQuery(QueryLanguage.SERQL, command.substring("serql".length()));
		}
		else if ("sparql".equalsIgnoreCase(tokens[0])) {
			evaluateQuery(QueryLanguage.SPARQL, command.substring("sparql".length()));
		}
		else if ("set".equalsIgnoreCase(tokens[0])) {
			if (tokens.length < 2) {
				printHelpSet();
			}
			else {
				setParameter(tokens[1]);
			}
		}
		else if (command.length() == 0) {
			// empty line, ignore
		}
		else {
			writeError("Unknown command");
		}

		return exit;
	}

	private static void printUsage() {
		writeln("Usage:");
		writeln("start-console [-d datadir [rep-id]]");
	}

	private static void printHelp(String[] tokens) {
		if (tokens.length < 2) {
			printHelpHelp();
		}
		else {
			if ("show".equals(tokens[1])) {
				printHelpShow();
			}
			else if ("create".equals(tokens[1])) {
				printHelpCreate();
			}
			else if ("drop".equals(tokens[1])) {
				printHelpDrop();
			}
			else if ("clear".equals(tokens[1])) {
				printHelpClear();
			}
			else if ("set".equals(tokens[1])) {
				printHelpSet();
			}
			else {
				writeln("No info available for command " + tokens[1]);
			}
		}
	}

	private static void printHelpHelp() {
		writeln("For more information on a specific command, try 'help <command>.'");
		writeln("List of all commands:");
		writeln("help     Displays this help message");
		writeln("create   Creates a new repository");
		writeln("drop     Drops a repository");
		writeln("open     Opens a repository to work on, takes a repository ID as argument");
		writeln("close    Closes the current repository");
		writeln("show     Displays an overview of various resources");
		writeln("load     Loads a data file into a repository, takes a file path or URL as argument");
		writeln("verify   Verifies the syntax of an RDF data file, takes a file path or URL as argument");
		writeln("clear    Removes data from a repository");
		writeln("serql    Evaluates the SeRQL query, takes a query as argument");
		writeln("sparql   Evaluates the SPARQL query, takes a query as argument");
		writeln("set      Allows various console parameters to be set");
		writeln("exit     Exit the console");
		writeln("quit     Exit the console");
	}

	private static void printHelpShow() {
		writeln("Available targets for 'show' are:");
		writeln("repositories    Shows all available repositories");
		writeln("r               Same as 'repositories'");
		writeln("namespaces      Shows all namespaces");
		writeln("n               Same as 'namespaces'");
		writeln("contexts        Shows all context identifiers");
		writeln("c               Same as 'contexts'");
	}

	private static void printHelpCreate() {
		writeln("Valid arguments for 'create' are:");
		writeln("<template-name>    The name of a repository configuration template");
	}

	private static void printHelpDrop() {
		writeln("Valid arguments for 'drop' are:");
		writeln("<repository-id>    The id of the repository");
	}

	private static void printHelpClear() {
		writeln("Valid arguments for 'clear' are:");
		writeln("all                 Clears the entire repository");
		writeln("context null        Clears the null context");
		writeln("context <context>   Clears the specified context");
	}

	private static void printHelpSet() {
		writeln("Valid arguments for 'set' are:");
		writeln("width=<number>          Set the width for query result tables");
		writeln("log=<level>             Set the logging level (none, error, warning, info or debug)");
		writeln("prefixed=<true|false>   Set use of prefixed names in query results (default is true)");
		writeln("appendNamespaces=(true|false)");
		writeln("                        Set use of appending known namespaces to query (default is true)");
	}

	private static void createRepository(String[] tokens)
		throws IOException
	{
		Repository repository = Console.repository;
		String repositoryID = Console.repositoryID;

		if (repository == null) {
			// Use default system repository
			repository = manager.getSystemRepository();
			repositoryID = SystemRepository.ID;
		}
		else if (!SystemRepository.ID.equals(repositoryID)) {
			boolean proceed = askProceed(
					"WARNING: You are about to add a repository configuration to repository " + repositoryID, true);

			if (!proceed) {
				writeln("Create aborted");
				return;
			}
		}

		try {
			String templateName = tokens[1];

			// FIXME: remove assumption of .ttl extension
			String templateFileName = templateName + ".ttl";

			File templatesDir = manager.resolvePath(TEMPLATES_DIR);

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

			String template = IOUtil.readString(new InputStreamReader(templateStream, "UTF-8"));
			templateStream.close();

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

			ValueFactory vf = repository.getValueFactory();

			Graph graph = new GraphImpl(vf);

			RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
			rdfParser.setRDFHandler(new StatementCollector(graph));
			rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);

			Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE,
					RepositoryConfigSchema.REPOSITORY);
			RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
			repConfig.validate();

			if (RepositoryConfigUtil.hasRepositoryConfig(repository, repConfig.getID())) {
				boolean proceed = askProceed(
						"WARNING: you are about to overwrite the configuration of an existing repository!", false);

				if (!proceed) {
					writeln("Create aborted");
					return;
				}
			}

			RepositoryConfigUtil.updateRepositoryConfigs(repository, repConfig);
			writeln("Repository created");
		}
		catch (Exception e) {
			writeError(e.getMessage());
			return;
		}
	}

	private static void dropRepository(String id)
		throws IOException
	{
		Repository repository = Console.repository;
		String repositoryID = Console.repositoryID;

		if (repository == null) {
			// Use default system repository
			repository = manager.getSystemRepository();
			repositoryID = SystemRepository.ID;
		}
		else if (!SystemRepository.ID.equals(repositoryID)) {
			boolean proceed = askProceed(
					"WARNING: You are about to remove a repository configuration from a non-SYSTEM repository",
					false);

			if (!proceed) {
				writeln("Drop aborted");
				return;
			}
		}

		try {
			ValueFactory vf = repository.getValueFactory();

			RepositoryConnection con = repository.getConnection();

			try {
				Resource context;
				TupleQuery query = con.prepareTupleQuery(QueryLanguage.SERQL, REPOSITORY_CONTEXT_QUERY);
				query.setBinding("ID", vf.createLiteral(id));
				TupleQueryResult queryResult = query.evaluate();

				try {
					if (!queryResult.hasNext()) {
						writeError("Unable to find context information for repository '" + id + "'");
						return;
					}

					BindingSet bindings = queryResult.next();
					context = (Resource)bindings.getValue("C");

					if (queryResult.hasNext()) {
						writeError("Multiple contexts found for repository '" + id + "'");
						return;
					}
				}
				finally {
					queryResult.close();
				}

				boolean proceed = askProceed("WARNING: you are about to drop repository '" + id + "'.", true);
				if (proceed) {
					if (id.equals(repositoryID)) {
						closeRepository();
					}
					con.clear(context);
					writeln("Dropped repository '" + id + "'");
				}
				else {
					writeln("Drop aborted");
				}
			}
			catch (MalformedQueryException e) {
				writeError("Internal error: malformed preconfigured query");
			}
			catch (QueryEvaluationException e) {
				throw new RepositoryException(e);
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to drop repository: " + e.getMessage());
		}
	}

	private static void openRepository(String id) {
		try {
			Repository newRepository = manager.getRepository(id);

			if (newRepository != null) {
				if (repositoryID != null) {
					// Close current repository
					closeRepository();
				}

				repository = newRepository;
				repositoryID = id;
				writeln("Opened repository '" + id + "'");
			}
			else {
				writeError("Unknown repository: '" + id + "'");
			}
		}
		catch (RepositoryConfigException e) {
			writeError(e.getMessage());
		}
		catch (RepositoryException e) {
			writeError(e.getMessage());
		}
	}

	private static void closeRepository() {
		if (repository == null) {
			writeln("There are no open repositories that can be closed");
		}
		else {
			writeln("Closed repository '" + repositoryID + "'");
			repository = null;
			repositoryID = null;
		}
	}

	private static void showRepositories() {
		try {
			Repository sysRep = manager.getSystemRepository();
			RepositoryConnection con = sysRep.getConnection();
			try {
				TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, REPOSITORY_LIST_QUERY).evaluate();
				try {
					if (!queryResult.hasNext()) {
						writeln("--no repositories found--");
					}
					else {
						writeln("+----------");
						while (queryResult.hasNext()) {
							BindingSet bindings = queryResult.next();
							Literal idLiteral = (Literal)bindings.getValue("ID");
							Literal titleLiteral = (Literal)bindings.getValue("Title");

							write("|" + idLiteral.getLabel());
							if (titleLiteral != null) {
								write(" (\"" + titleLiteral.getLabel() + "\")");
							}
							writeln();
						}
						writeln("+----------");
					}
				}
				finally {
					queryResult.close();
				}
			}
			catch (MalformedQueryException e) {
				writeError("Internal error: malformed preconfigured query");
			}
			catch (QueryEvaluationException e) {
				throw new RepositoryException(e);
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			writeError("Failed to get repository list: " + e.getMessage());
		}
	}

	private static void showNamespaces() {
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
		}
	}

	private static void showContexts() {
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
		}
	}

	private static void verify(String dataPath) {
		String dataPathLC = dataPath.toLowerCase();

		if (!dataPathLC.startsWith("http:") && !dataPathLC.startsWith("file:")) {
			// File path specified, convert to URL
			dataPath = "file:" + dataPath;
		}

		try {
			URL dataURL = new URL(dataPath);
			RDFFormat format = RDFFormat.forFileName(dataPath, RDFFormat.RDFXML);

			writeln("RDF Format is " + format.getName());

			RDFParser parser = Rio.createParser(format);
			VerificationListener listener = new VerificationListener();
			parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
			parser.setVerifyData(true);
			parser.setParseErrorListener(listener);
			parser.setRDFHandler(listener);

			writeln("Verifying data...");
			InputStream in = dataURL.openStream();

			parser.parse(in, "urn://openrdf.org/RioVerifier/");

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
			writeError("Malformed data URL");
		}
		catch (IOException e) {
			writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			writeParseError(e);
		}
		catch (RDFHandlerException e) {
			writeError("Unable to verify : " + e.getMessage());
		}
	}

	private static void load(String context, String dataPath, String baseURI) {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		String dataPathLC = dataPath.toLowerCase();

		if (!dataPathLC.startsWith("http:") && !dataPathLC.startsWith("file:")) {
			// File path specified, convert to URL
			dataPath = "file:" + dataPath;
		}

		try {
			RepositoryConnection con = repository.getConnection();

			URL dataURL = new URL(dataPath);
			URI contextURI = null;
			if (context != null) {
				contextURI = repository.getValueFactory().createURI(context);
			}
			RDFFormat format = RDFFormat.forFileName(dataPath, RDFFormat.RDFXML);

			writeln("Loading data...");

			long startTime = System.currentTimeMillis();
			if (context == null) {
				con.add(dataURL, baseURI, format);
			}
			else {
				con.add(dataURL, baseURI, format, contextURI);
			}
			long endTime = System.currentTimeMillis();

			con.close();

			writeln("Data has been added to the repository (" + (endTime - startTime) + " ms)");
		}
		catch (MalformedURLException e) {
			writeError("Malformed data URL");
		}
		catch (IOException e) {
			writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			writeParseError(e);
		}
		catch (RepositoryException e) {
			writeError("Unable to add data to repository: " + e.getMessage());
		}
	}

	private static void clear() {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		writeln("Clearing repository...");
		try {
			RepositoryConnection con = repository.getConnection();
			con.clear();
			con.close();
			writeln("Repository cleared");
		}
		catch (RepositoryException e) {
			writeError("Failed to clear repository: " + e.getMessage());
		}
	}

	private static void clearContext(String contextID) {
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		Resource context = null;
		if (contextID != null) {
			ValueFactory valueFactory = repository.getValueFactory();

			if (contextID.startsWith("_:")) {
				context = valueFactory.createBNode(contextID.substring(2));
			}
			else {
				context = valueFactory.createURI(contextID);
			}
		}

		if (contextID == null) {
			writeln("Clearing null context...");
		}
		else {
			writeln("Clearing context " + contextID);
		}

		try {
			RepositoryConnection con = repository.getConnection();
			con.clear(context);
			con.close();
			writeln("Context cleared");
		}
		catch (RepositoryException e) {
			writeError("Failed to clear context: " + e.getMessage());
		}
	}

	private static void evaluateQuery(QueryLanguage ql, String queryString) {
		try {
			queryString = appendNamespaces(ql, queryString);
			ParsedQuery query = QueryParserUtil.parseQuery(ql, queryString, null);
			if (query instanceof ParsedTupleQuery) {
				evaluateTupleQuery(ql, queryString);
			}
			else if (query instanceof ParsedGraphQuery) {
				evaluateGraphQuery(ql, queryString);
			}
			else {
				writeError("Unexpected query type");
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			writeError("Unsupported query lanaguge: " + ql.toString());
		}
		catch (MalformedQueryException e) {
			writeError("Malformed query: " + e.getMessage());
		}
		catch (QueryEvaluationException e) {
			writeError("Query evaluation error: " + e.getMessage());
		}
		catch (RepositoryException e) {
			writeError("Failed to evaluate query: " + e.getMessage());
		}
	}

	private static String appendNamespaces(QueryLanguage ql, String queryString) {
		String result = queryString;

		if (repository != null && appendNamespaces) {
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
				}
			}
		} // end if appendNamespaces

		return result;
	}

	private static void evaluateTupleQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		writeln("Evaluating query...");
		long startTime = System.currentTimeMillis();

		RepositoryConnection con = repository.getConnection();

		Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());

		try {
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

				long endTime = System.currentTimeMillis();
				writeln(resultCount + " result(s) (" + (endTime - startTime) + " ms)");
			}
			finally {
				tupleQueryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	private static void evaluateGraphQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		if (repository == null) {
			writeError("please open a repository first");
			return;
		}

		writeln("Evaluating query...");
		long startTime = System.currentTimeMillis();

		RepositoryConnection con = repository.getConnection();

		Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());
		try {
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

				long endTime = System.currentTimeMillis();
				writeln(resultCount + " results (" + (endTime - startTime) + " ms)");
			}
			finally {
				queryResult.close();
			}
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
	private static String getPrefixForNamespace(String namespace, Collection<Namespace> namespaces) {
		for (Namespace ns : namespaces) {
			if (namespace.equals(ns.getName())) {
				return ns.getPrefix();
			}
		}
		return null;
	}

	private static String getStringRepForValue(Value value, Collection<Namespace> namespaces) {

		String valueStr;

		if (value == null) {
			valueStr = "";
		}
		else if (usePrefixedNames && value instanceof URI) {

			String namespace = ((URI)value).getNamespace();

			String prefix = getPrefixForNamespace(namespace, namespaces);

			if (prefix != null) {
				valueStr = prefix + ":" + ((URI)value).getLocalName();
			}
			else {
				valueStr = NTriplesUtil.toNTriplesString(value);
			}
		}
		else {
			valueStr = NTriplesUtil.toNTriplesString(value);
		}

		return valueStr;
	}

	private static void setParameter(String param) {
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

		if ("width".equals(key)) {
			if (value == null) {
				writeln("width=" + consoleWidth);
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
		else if ("log".equals(key)) {
			if (value == null) {
				Level currentLevel = rootLogger.getLevel();
				String levelString = currentLevel.getName();

				for (Map.Entry<String, Level> entry : LOG_LEVELS.entrySet()) {
					if (entry.getValue().equals(currentLevel)) {
						levelString = entry.getKey();
						break;
					}
				}

				writeln("logging level: " + levelString);
			}
			else {
				Level logLevel = LOG_LEVELS.get(value.toLowerCase());

				if (logLevel != null) {
					rootLogger.setLevel(logLevel);
					writeln("Logging level set to " + value.toLowerCase());
				}
				else {
					writeError("unknown logging level: " + value);
				}
			}
		}
		else if ("prefixed".equals(key)) {
			if (value == null) {
				writeln("prefixed: " + usePrefixedNames);
			}
			else {
				usePrefixedNames = Boolean.parseBoolean(value);
				writeln("prefixed = " + usePrefixedNames);
			}
		}
		else if ("appendNamespaces".equals(key)) {
			if (value == null) {
				writeln("appendNamespaces: " + appendNamespaces);
			}
			else {
				appendNamespaces = Boolean.parseBoolean(value);
				writeln("appendNamespaces = " + appendNamespaces);
			}
		}
		else {
			writeError("unknown option: " + param);
		}
	}

	private static boolean askProceed(String msg, boolean defaultValue)
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
	private static String readMultiLineInput()
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

	private static void write(String s) {
		System.out.print(s);
	}

	private static void writeln() {
		System.out.println();
	}

	private static void writeln(String s) {
		System.out.println(s);
	}

	private static void writeError(String errMsg) {
		writeln("ERROR: " + errMsg);
	}

	private static void writeParseError(RDFParseException e) {
		StringBuilder msg = new StringBuilder(128);

		msg.append("Malformed document");

		if (e.getLineNumber() != -1) {
			msg.append(". Error at line ").append(e.getLineNumber());

			if (e.getColumnNumber() != -1) {
				msg.append(", column ").append(e.getColumnNumber());
			}
		}

		msg.append(": ").append(e.getMessage());

		writeError(msg.toString());
	}

	static class VerificationListener extends RDFHandlerBase implements ParseErrorListener {

		private int _warnings;

		private int _errors;

		private int _statements;

		public int getWarnings() {
			return _warnings;
		}

		public int getErrors() {
			return _errors;
		}

		public int getStatements() {
			return _statements;
		}

		public void handleStatement(Statement st)
			throws RDFHandlerException
		{
			_statements++;
		}

		public void warning(String msg, int lineNo, int colNo) {
			_warnings++;
			writeln("WARNING: [" + lineNo + ", " + colNo + "] " + msg);
		}

		public void error(String msg, int lineNo, int colNo) {
			_errors++;
			writeError("[" + lineNo + ", " + colNo + "] " + msg);
		}

		public void fatalError(String msg, int lineNo, int colNo) {
			_errors++;
			writeError("(fatal) [" + lineNo + ", " + colNo + "] " + msg);
		}
	}
}
