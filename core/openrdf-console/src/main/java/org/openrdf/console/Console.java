/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.QueryParserUtil;
import org.openrdf.querylanguage.UnsupportedQueryLanguageException;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.Query;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.repository.Connection;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryManager;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryManagerConfig;
import org.openrdf.repository.config.RepositoryManagerConfigException;
import org.openrdf.repository.config.RepositoryManagerConfigReader;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailException;
import org.openrdf.util.StringUtil;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.log.ThreadLog;
import org.xml.sax.SAXException;

/**
 * 
 */
public class Console {

	private static RepositoryManager _server;

	private static Repository _repository;

	private static BufferedReader _in;

	private static int _consoleWidth = 100;

	public static void main(String[] args)
		throws IOException, UnsupportedQueryLanguageException
	{
		_writeln("Welcome to the Sesame console");

		ThreadLog.setDefaultLog(null, ThreadLog.WARNING);

		_server = new RepositoryManager();

		if (args.length > 0) {
			_readServerConfig(args[0]);

			if (args.length > 1) {
				_openRepository(args[1]);
			}
			else {
				_writeln("The following repositories are available:");
				_showRepositories();
			}
		}

		_writeln();
		_writeln("Commands end with '.' at the end of a line");
		_writeln("Type 'help.' for help");

		_in = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			String command = _readMultiLineInput();

			if (command == null) {
				// EOF
				break;
			}

			String[] tokens = command.split("[ \t\r\n]");

			if ("quit".equalsIgnoreCase(tokens[0]) || "exit".equalsIgnoreCase(tokens[0])) {
				break;
			}
			else if ("help".equalsIgnoreCase(tokens[0])) {
				_printHelp();
			}
			else if ("configure".equalsIgnoreCase(tokens[0])) {
				if (tokens.length < 2) {
					_writeln("Usage: configure <server-config-file>.");
				}
				else {
					_readServerConfig(tokens[1]);
				}
			}
			else if ("open".equalsIgnoreCase(tokens[0])) {
				if (tokens.length < 2) {
					_showRepositories();
				}
				else {
					_openRepository(tokens[1]);
				}
			}
			else if ("show".equalsIgnoreCase(tokens[0])) {
				if (tokens.length < 2) {
					_printHelpShow();
				}
				else if ("repositories".equalsIgnoreCase(tokens[1]) || "r".equalsIgnoreCase(tokens[1])) {
					_showRepositories();
				}
				else if ("namespaces".equalsIgnoreCase(tokens[1]) || "n".equalsIgnoreCase(tokens[1])) {
					_showNamespaces();
				}
				else if ("contexts".equalsIgnoreCase(tokens[1]) || "c".equalsIgnoreCase(tokens[1])) {
					_showContexts();
				}
				else {
					_writeError("Unknown target '" + tokens[1] + "'");
				}
			}
			else if ("load".equalsIgnoreCase(tokens[0])) {
				if (tokens.length < 2) {
					_writeln("Usage: load [-c <context-id-uri>] <data-file-or-url> [<base-uri>].");
				}
				else if ("-c".equals(tokens[1])) {
					if (tokens.length == 5) {
						_load(tokens[3], tokens[4], tokens[2]);
					}
					else {
						_load(tokens[3], null, tokens[2]);
					}
				}
				else {
					if (tokens.length == 3) {
						_load(tokens[1], tokens[2], null);
					}
					else {
						_load(tokens[1], null, null);
					}
				}
			}
			else if ("verify".equalsIgnoreCase(tokens[0])) {
				if (tokens.length != 2) {
					_writeln("Usage: verify <data-file-or-url>.");
				}
				else {
					_verify(tokens[1]);
				}
			}
			else if ("clear".equalsIgnoreCase(tokens[0])) {
				if (tokens.length >= 2 && "all".equalsIgnoreCase(tokens[1])) {
					_clear();
				}
				else if (tokens.length >= 3 && "context".equalsIgnoreCase(tokens[1])) {
					if ("null".equalsIgnoreCase(tokens[2])) {
						_clearContext(null);
					}
					else {
						_clearContext(tokens[2]);
					}
				}
				else {
					_printHelpClear();
				}
			}
			else if ("select".equalsIgnoreCase(tokens[0])) {
				// TODO: should this be removed now that the 'serql' command is supported?
				_evaluateQuery(QueryLanguage.SERQL, command);
			}
			else if ("construct".equalsIgnoreCase(tokens[0])) {
				// TODO: should this be removed now that the 'serql' command is supported?
				_evaluateQuery(QueryLanguage.SERQL, command);
			}
			else if ("serql".equalsIgnoreCase(tokens[0])) {
				_evaluateQuery(QueryLanguage.SERQL, command.substring("serql".length()));
			}
			else if ("sparql".equalsIgnoreCase(tokens[0])) {
				_evaluateQuery(QueryLanguage.SPARQL, command.substring("sparql".length()));
			}
			else if ("set".equalsIgnoreCase(tokens[0])) {
				if (tokens.length < 2) {
					_printHelpSet();
				}
				else {
					_setParameter(tokens[1]);
				}
			}
			else if (command.length() == 0) {
				// empty line, ignore
			}
			else {
				_writeError("Unknown command");
			}
		}

		if (_repository != null) {
			_writeln("Closing all opened repositories...");
			_server.clear();
			_repository = null;
		}

		_writeln("Bye");
	}

	private static void _printHelp() {
		_writeln("List of all commands");
		_writeln("help           Display this help message");
		_writeln("configure      Loads a server configuration file for this console to work with");
		_writeln("open           Opens a repository to work on, takes a repository ID as argument");
		_writeln("show           Displays an overview of various resources; run 'show.' for more info");
		_writeln("load           Loads a data file into a repository, takes a file path or URL as argument");
		_writeln("verify         Verifies the syntax of an RDF data file, takes a file path or URL as argument");
		_writeln("clear          Removes data from a repository; run 'clear.' for more info");
		_writeln("serql          Evaluates the SeRQL query, takes a query as argument");
		_writeln("sparql         Evaluates the SPARQL query, takes a query as argument");
		_writeln("set            Allows various console parameters to be set; run 'set.' for more info");
		_writeln("exit           Exit the console");
		_writeln("quit           Exit the console");
	}

	private static void _printHelpShow() {
		_writeln("Available targets for 'show' are:");
		_writeln("repositories    Shows all available repositories");
		_writeln("r               Same as 'repositories'");
		_writeln("namespaces      Shows all namespaces");
		_writeln("n               Same as 'namespaces'");
		_writeln("contexts        Shows all context identifiers");
		_writeln("c               Same as 'contexts'");
	}

	private static void _printHelpClear() {
		_writeln("Valid arguments for 'clear' are:");
		_writeln("all                 Clears the entire repository");
		_writeln("context null        Clears the null context");
		_writeln("context <context>   Clears the specified context");
	}

	private static void _printHelpSet() {
		_writeln("Valid arguments for 'set' are:");
		_writeln("width=<number>   Sets the width for query result tables");
		_writeln("log=<level>      Set the log level (none, error, warning, info or debug)");
	}

	private static void _readServerConfig(String filePath) {
		try {
			File configFile = new File(filePath);
			RepositoryManagerConfig config = RepositoryManagerConfigReader.read(configFile);
			_server.setServerConfig(config);
			_writeln("Using server configuration file: " + configFile);
		}
		catch (SAXException e) {
			_writeError("Failed to load server configuration file: " + e.getMessage());
		}
		catch (IOException e) {
			_writeError("Failed to load server configuration file: " + e.getMessage());
		}
	}

	private static void _openRepository(String id) {
		try {
			Repository newRepository = _server.getRepository(id);
			if (newRepository != null) {
				_repository = newRepository;
				_writeln("Opened repository '" + id + "'");
			}
			else {
				_writeError("Unknown repository: '" + id + "'");
			}
		}
		catch (RepositoryManagerConfigException e) {
			_writeError(e.getMessage());
		}
	}

	private static void _showRepositories() {
		Collection<RepositoryConfig> repositories = _server.getServerConfig().getRepositoryConfigs();

		if (repositories.isEmpty()) {
			_writeln("--no repositories found--");
		}
		else {
			_writeln("+----------");
			for (RepositoryConfig repConfig : _server.getServerConfig().getRepositoryConfigs()) {
				_writeln("|" + repConfig.getID() + " (\"" + repConfig.getTitle() + "\")");
			}
			_writeln("+----------");
		}
	}

	private static void _showNamespaces() {
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		Connection con;
		try {
			con = _repository.getConnection();

			try {
				CloseableIterator<? extends Namespace> namespaces = con.getNamespaces();

				try {
					if (namespaces.hasNext()) {
						_writeln("+----------");
						while (namespaces.hasNext()) {
							Namespace ns = namespaces.next();
							_writeln("|" + ns.getPrefix() + "  " + ns.getName());
						}
						_writeln("+----------");
					}
					else {
						_writeln("--no namespaces found--");
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
		catch (SailException e) {
			_writeError(e.getMessage());
		}
	}

	private static void _showContexts() {
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		Connection con;
		try {
			con = _repository.getConnection();

			try {
				CloseableIterator<? extends Resource> contexts = con.getContextIDs();

				try {
					if (contexts.hasNext()) {
						_writeln("+----------");
						while (contexts.hasNext()) {
							Resource context = contexts.next();
							_writeln("|" + context.toString());
						}
						_writeln("+----------");
					}
					else {
						_writeln("--no contexts found--");
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
		catch (SailException e) {
			_writeError(e.getMessage());
		}
	}

	private static void _verify(String dataPath) {
		String dataPathLC = dataPath.toLowerCase();

		if (!dataPathLC.startsWith("http:") && !dataPathLC.startsWith("file:")) {
			// File path specified, convert to URL
			dataPath = "file:" + dataPath;
		}

		try {
			URL dataURL = new URL(dataPath);
			RDFFormat format = RDFFormat.forFileName(dataPath, RDFFormat.RDFXML);

			_writeln("RDF Format is " + format.getName());

			RDFParser parser = Rio.createParser(format);
			VerificationListener listener = new VerificationListener();
			parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
			parser.setVerifyData(true);
			parser.setParseErrorListener(listener);
			parser.setRDFHandler(listener);

			_writeln("Verifying data...");
			InputStream in = dataURL.openStream();

			parser.parse(in, "urn://openrdf.org/RioVerifier/");

			int warnings = listener.getWarnings();
			int errors = listener.getErrors();
			int statements = listener.getStatements();

			if (warnings + errors > 0) {
				_writeln("Found " + warnings + " warnings and " + errors + " errors");
			}
			else {
				_writeln("Data verified, no errors were found");
			}

			if (errors == 0) {
				_writeln("File contains " + statements + " statements");
			}
		}
		catch (MalformedURLException e) {
			_writeError("Malformed data URL");
		}
		catch (IOException e) {
			_writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			_writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			_writeParseError(e);
		}
		catch (RDFHandlerException e) {
			_writeError("Unable to verify : " + e.getMessage());
		}
	}

	private static void _load(String dataPath, String baseURI, String context) {
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		String dataPathLC = dataPath.toLowerCase();

		if (!dataPathLC.startsWith("http:") && !dataPathLC.startsWith("file:")) {
			// File path specified, convert to URL
			dataPath = "file:" + dataPath;
		}

		try {
			Connection con = _repository.getConnection();

			URL dataURL = new URL(dataPath);
			URI contextURI = null;
			if (context != null) {
				contextURI = _repository.getValueFactory().createURI(context);
			}
			RDFFormat format = RDFFormat.forFileName(dataPath, RDFFormat.RDFXML);

			_writeln("Loading data...");

			long startTime = System.currentTimeMillis();
			con.add(dataURL, baseURI, format, contextURI);
			long endTime = System.currentTimeMillis();

			con.close();

			_writeln("Data has been added to the repository (" + (endTime - startTime) + " ms)");
		}
		catch (MalformedURLException e) {
			_writeError("Malformed data URL");
		}
		catch (IOException e) {
			_writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			_writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			_writeParseError(e);
		}
		catch (SailException e) {
			_writeError("Unable to add data to repository: " + e.getMessage());
		}
	}

	private static void _clear() {
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		_writeln("Clearing repository...");
		try {
			Connection con = _repository.getConnection();
			con.clear();
			con.close();
			_writeln("Repository cleared");
		}
		catch (SailException e) {
			_writeError("Failed to clear repository: " + e.getMessage());
		}
	}

	private static void _clearContext(String contextID) {
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		Resource context = null;
		if (contextID != null) {
			ValueFactory valueFactory = _repository.getSail().getValueFactory();

			if (contextID.startsWith("_:")) {
				context = valueFactory.createBNode(contextID.substring(2));
			}
			else {
				context = valueFactory.createURI(contextID);
			}
		}

		if (contextID == null) {
			_writeln("Clearing null context...");
		}
		else {
			_writeln("Clearing context " + contextID);
		}

		try {
			Connection con = _repository.getConnection();
			con.clearContext(context);
			con.close();
			_writeln("Context cleared");
		}
		catch (SailException e) {
			_writeError("Failed to clear context: " + e.getMessage());
		}
	}

	private static void _evaluateQuery(QueryLanguage ql, String queryString) {
		try {
			Query query = QueryParserUtil.parseQuery(ql, queryString);
			if (query instanceof TupleQuery) {
				_evaluateTupleQuery(ql, queryString);
			}
			else if (query instanceof GraphQuery) {
				_evaluateGraphQuery(ql, queryString);
			}
			else {
				_writeError("Unexpected query type");
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			_writeError("Unsupported query lanaguge: " + ql.toString());
		}
		catch (MalformedQueryException e) {
			_writeError("Malformed query: " + e.getMessage());
		}
		catch (SailException e) {
			_writeError("Failed to evaluate query: " + e.getMessage());
		}
	}

	private static void _evaluateTupleQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, SailException
	{
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		_writeln("Evaluating query...");
		long startTime = System.currentTimeMillis();

		Connection con = _repository.getConnection();

		try {
			TupleQueryResult tupleQueryResult = con.evaluateTupleQuery(ql, queryString);

			try {
				int resultCount = 0;
				List<String> bindingNames = tupleQueryResult.getBindingNames();

				if (bindingNames.isEmpty()) {
					for (Solution solution : tupleQueryResult) {
						resultCount++;
					}
				}
				else {
					int columnWidth = (_consoleWidth - 1) / bindingNames.size() - 3;

					// Build table header
					StringBuilder sb = new StringBuilder(_consoleWidth);
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
					_writeln(separatorLine);
					_writeln(header);
					_writeln(separatorLine);

					// Write table rows
					for (Solution solution : tupleQueryResult) {
						resultCount++;

						sb.setLength(0);
						for (String bindingName : bindingNames) {
							Value value = solution.getValue(bindingName);
							String valueStr = (value == null) ? "" : NTriplesUtil.toNTriplesString(value);

							sb.append("| ").append(valueStr);
							StringUtil.appendN(' ', columnWidth - valueStr.length(), sb);
						}
						sb.append("|");
						_writeln(sb.toString());
					}

					_writeln(separatorLine);
				}

				long endTime = System.currentTimeMillis();
				_writeln(resultCount + " result(s) (" + (endTime - startTime) + " ms)");
			}
			finally {
				tupleQueryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	private static void _evaluateGraphQuery(QueryLanguage ql, String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, SailException
	{
		if (_repository == null) {
			_writeError("please open a repository first");
			return;
		}

		_writeln("Evaluating query...");
		long startTime = System.currentTimeMillis();

		Connection con = _repository.getConnection();
		try {
			GraphQueryResult queryResult = con.evaluateGraphQuery(ql, queryString);

			try {
				int resultCount = 0;

				for (Statement st : queryResult) {
					resultCount++;

					_write(NTriplesUtil.toNTriplesString(st.getSubject()));
					_write("   ");
					_write(NTriplesUtil.toNTriplesString(st.getPredicate()));
					_write("   ");
					_write(NTriplesUtil.toNTriplesString(st.getObject()));
					_writeln();
				}

				long endTime = System.currentTimeMillis();
				_writeln(resultCount + " results (" + (endTime - startTime) + " ms)");
			}
			finally {
				queryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	private static void _setParameter(String param) {
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
				_writeln("width=" + _consoleWidth);
			}
			else {
				try {
					int width = Integer.parseInt(value);
					if (width > 0) {
						_consoleWidth = width;
					}
					else {
						_writeError("Width must be larger than 0");
					}
				}
				catch (NumberFormatException e) {
					_writeError("Width must be a positive number");
				}
			}
		}
		else if ("log".equals(key)) {
			if (value == null) {
				String log = null;
				switch (ThreadLog.getDefaultLogLevel()) {
					case ThreadLog.NONE:
						log = "none";
						break;
					case ThreadLog.ERROR:
						log = "error";
						break;
					case ThreadLog.WARNING:
						log = "warning";
						break;
					case ThreadLog.STATUS:
						log = "info";
						break;
					case ThreadLog.TRACE:
					case ThreadLog.ALL:
						log = "debug";
						break;
				}

				_writeln("log=" + log);
			}
			else {
				if ("none".equalsIgnoreCase(value)) {
					ThreadLog.setDefaultLog(null, ThreadLog.NONE);
				}
				else if ("error".equalsIgnoreCase(value)) {
					ThreadLog.setDefaultLog(null, ThreadLog.ERROR);
				}
				else if ("warning".equalsIgnoreCase(value)) {
					ThreadLog.setDefaultLog(null, ThreadLog.WARNING);
				}
				else if ("info".equalsIgnoreCase(value)) {
					ThreadLog.setDefaultLog(null, ThreadLog.STATUS);
				}
				else if ("debug".equalsIgnoreCase(value)) {
					ThreadLog.setDefaultLog(null, ThreadLog.TRACE);
				}
				else {
					_writeError("Unknown log level: " + value);
				}
			}
		}
		else {
			_writeError("unknown option: " + param);
		}
	}

	/**
	 * Reads multiple lines from the input until a line that ends with a '.' is
	 * read.
	 */
	private static String _readMultiLineInput()
		throws IOException
	{
		String line = _in.readLine();
		if (line == null) {
			// EOF
			return null;
		}

		StringBuilder buf = new StringBuilder(256);
		buf.append(line);

		while (line != null && !line.endsWith(".")) {
			line = _in.readLine();
			buf.append('\n');
			buf.append(line);
		}

		// Remove closing dot
		buf.setLength(buf.length() - 1);

		return buf.toString().trim();
	}

	private static void _write(String s) {
		System.out.print(s);
	}

	private static void _writeln() {
		System.out.println();
	}

	private static void _writeln(String s) {
		System.out.println(s);
	}

	private static void _writeError(String errMsg) {
		_writeln("ERROR: " + errMsg);
	}

	private static void _writeParseError(RDFParseException e) {
		StringBuilder msg = new StringBuilder(128);

		msg.append("Malformed document");

		if (e.getLineNumber() != -1) {
			msg.append(". Error at line ").append(e.getLineNumber());

			if (e.getColumnNumber() != -1) {
				msg.append(", column ").append(e.getColumnNumber());
			}
		}

		msg.append(": ").append(e.getMessage());

		_writeError(msg.toString());
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
			_writeln("WARNING: [" + lineNo + ", " + colNo + "] " + msg);
		}

		public void error(String msg, int lineNo, int colNo) {
			_errors++;
			_writeError("[" + lineNo + ", " + colNo + "] " + msg);
		}

		public void fatalError(String msg, int lineNo, int colNo) {
			_errors++;
			_writeError("(fatal) [" + lineNo + ", " + colNo + "] " + msg);
		}
	}
}
