/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.QueryParserUtil;
import org.openrdf.querylanguage.UnsupportedQueryLanguageException;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.RowSelection;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.querymodel.Var;
import org.openrdf.querymodel.StatementPattern.Scope;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultHandler;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.repository.helpers.RDFInserter;
import org.openrdf.repository.helpers.RDFRemover;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.log.ThreadLog;

/**
 * The default implementation of the Connection interface.
 * 
 * @author jeen
 */
class ConnectionImpl implements Connection {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The repository that this transaction belongs to.
	 */
	private Repository _repository;

	/**
	 * The Sail connection wrapped by this repository connection object.
	 */
	private SailConnection _sailConnection;

	private boolean _autoCommit;

	private static final boolean INCLUDE_INFERRED_DEFAULT = true;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new repository transaction that will wrap the supplied Sail
	 * transaction.
	 * 
	 * @throws SailException
	 */
	protected ConnectionImpl(Repository repository)
		throws SailException
	{
		_repository = repository;
		_sailConnection = repository.getSail().getConnection();

		_autoCommit = true;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Repository getRepository() {
		return _repository;
	}

	public boolean isOpen() {
		return _sailConnection != null && _sailConnection.isOpen();
	}

	public void setAutoCommit(boolean autoCommit)
		throws SailException
	{
		if (autoCommit == _autoCommit) {
			return;
		}

		_autoCommit = autoCommit;

		if (autoCommit) {
			// Connection was not in auto-commit mode, commit any uncommitted
			// transaction
			commit();
		}
	}

	public boolean isAutoCommit() {
		return _autoCommit;
	}

	public void commit()
		throws SailException
	{
		_sailConnection.commit();
	}

	public void rollback()
		throws SailException
	{
		_sailConnection.rollback();
	}

	public void close()
		throws SailException
	{
		_sailConnection.close();
		_sailConnection = null;
	}

	private void _autoCommit()
		throws SailException
	{
		if (isAutoCommit()) {
			_sailConnection.commit();
		}
	}

	protected void finalize()
		throws Throwable
	{
		if (isOpen()) {
			ThreadLog.warning("Closing dangling connection due to garbage collection");
			close();
		}

		super.finalize();
	}

	// implements Transaction.add(...)
	public void add(URL url, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_add(url, baseURI, dataFormat, null, false);
	}

	// implements Transaction.add(...)
	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_add(url, baseURI, dataFormat, context, true);
	}

	protected void _add(URL url, String baseURI, RDFFormat dataFormat, Resource context, boolean enforceContext)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		if (baseURI == null) {
			// default baseURI to url
			baseURI = url.toExternalForm();
		}

		InputStream in = url.openStream();

		try {
			_addInputStreamOrReader(in, baseURI, dataFormat, context, enforceContext);
		}
		finally {
			in.close();
		}
	}

	// implements Transaction.add(...)
	public void add(File file, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_add(file, baseURI, dataFormat, null, false);
	}

	// implements Transaction.add(...)
	public void add(File file, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_add(file, baseURI, dataFormat, context, true);
	}

	protected void _add(File file, String baseURI, RDFFormat dataFormat, Resource context,
			boolean enforceContext)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		if (baseURI == null) {
			// default baseURI to file
			baseURI = file.toURI().toString();
		}

		InputStream in = new FileInputStream(file);

		try {
			_addInputStreamOrReader(in, baseURI, dataFormat, context, enforceContext);
		}
		finally {
			in.close();
		}
	}

	// implements Transaction.add(...)
	public void add(InputStream in, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_addInputStreamOrReader(in, baseURI, dataFormat, null, false);
	}

	// implements Transaction.add(...)
	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_addInputStreamOrReader(in, baseURI, dataFormat, context, true);
	}

	// implements Transaction.add(...)
	public void add(Reader reader, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_addInputStreamOrReader(reader, baseURI, dataFormat, null, false);
	}

	// implements Transaction.add(...)
	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		_addInputStreamOrReader(reader, baseURI, dataFormat, context, true);
	}

	protected void _addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource context, boolean enforceContext)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException
	{
		RDFParser rdfParser = Rio.createParser(dataFormat, _repository.getValueFactory());

		rdfParser.setVerifyData(true);
		rdfParser.setStopAtFirstError(true);
		rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);

		RDFInserter rdfInserter = new RDFInserter(_sailConnection, _repository.getValueFactory());

		if (enforceContext) {
			rdfInserter.enforceContext(context);
		}
		rdfParser.setRDFHandler(rdfInserter);

		try {
			if (inputStreamOrReader instanceof InputStream) {
				rdfParser.parse((InputStream)inputStreamOrReader, baseURI);
			}
			else if (inputStreamOrReader instanceof Reader) {
				rdfParser.parse((Reader)inputStreamOrReader, baseURI);
			}
			else {
				throw new IllegalArgumentException(
						"inputStreamOrReader must be an InputStream or a Reader, is a: "
								+ inputStreamOrReader.getClass());
			}

			_autoCommit();
		}
		catch (RDFHandlerException e) {
			// RDFInserter only throws wrapped SailExceptions
			throw (SailException)e.getCause();
		}
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		add(ql, graphQuery, true);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		add(ql, graphQuery, _repository, preserveBNodeIDs);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, Resource context)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		add(ql, graphQuery, context, true);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, Resource context, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		add(ql, graphQuery, _repository, context, preserveBNodeIDs);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		add(ql, graphQuery, queryRepository, true);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		_add(ql, graphQuery, queryRepository, null, false, preserveBNodeIDs);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository, Resource context)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		add(ql, graphQuery, queryRepository, context, true);
	}

	// implements Transaction.add(...)
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository, Resource context,
			boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		_add(ql, graphQuery, queryRepository, context, true, preserveBNodeIDs);
	}

	protected void _add(QueryLanguage ql, String graphQuery, Repository queryRepository, Resource context,
			boolean enforceContext, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		try {
			RDFInserter rdfInserter = new RDFInserter(_sailConnection, _repository.getValueFactory());
			if (enforceContext) {
				rdfInserter.enforceContext(context);
			}
			rdfInserter.setPreserveBNodeIDs(preserveBNodeIDs);

			evaluateGraphQuery(ql, graphQuery, rdfInserter);

			_autoCommit();
		}
		catch (RDFHandlerException e) {
			// RDFInserter only throws wrapped SailException's
			throw (SailException)e.getCause();
		}
	}

	// implements Transaction.add(...)
	public void add(Resource subject, URI predicate, Value object)
		throws SailException
	{
		add(subject, predicate, object, null);
	}

	public void add(Resource subject, URI predicate, Value object, Resource context)
		throws SailException
	{
		_add(subject, predicate, object, context);
		_autoCommit();
	}

	// implements Transaction.add(...)
	public void add(Statement st)
		throws SailException
	{
		add(st, st.getContext());
	}

	// implements Transaction.add(...)
	public void add(Statement st, Resource context)
		throws SailException
	{
		_add(st, context);
		_autoCommit();
	}

	// implements Transaction.add(...)
	public void add(Statement[] statements)
		throws SailException
	{
		for (Statement st : statements) {
			_add(st);
		}

		_autoCommit();
	}

	// implements Transaction.add(...)
	public void add(Statement[] statements, Resource context)
		throws SailException
	{
		for (Statement st : statements) {
			_add(st, context);
		}

		_autoCommit();
	}

	public void add(CloseableIterator<? extends Statement> statementIter)
		throws SailException
	{
		try {
			while (statementIter.hasNext()) {
				_add(statementIter.next());
			}

			_autoCommit();
		}
		finally {
			statementIter.close();
		}
	}

	public void add(CloseableIterator<? extends Statement> statementIter, Resource context)
		throws SailException
	{
		try {
			while (statementIter.hasNext()) {
				_add(statementIter.next(), context);
			}

			_autoCommit();
		}
		finally {
			statementIter.close();
		}
	}

	public void add(GraphQueryResult result)
		throws SailException
	{
		try {
			for (Statement st : result) {
				_add(st);
			}
			_autoCommit();
		}
		finally {
			result.close();
		}
	}

	public void add(GraphQueryResult result, Resource context)
		throws SailException
	{
		try {
			for (Statement st : result) {
				_add(st, context);
			}
			_autoCommit();
		}
		finally {
			result.close();
		}
	}

	// implements Transaction.add(...)
	public void add(Collection<? extends Statement> statements)
		throws SailException
	{
		for (Statement st : statements) {
			_add(st);
		}

		_autoCommit();
	}

	// implements Transaction.add(...)
	public void add(Collection<? extends Statement> statements, Resource context)
		throws SailException
	{
		for (Statement st : statements) {
			_add(st, context);
		}

		_autoCommit();
	}

	// implements Transaction.addReification(...)
	public void addReification(Statement st, URI reificationURI)
		throws SailException
	{
		addReification(st, reificationURI, st.getContext());
	}

	// implements Transaction.addReification(...)
	public void addReification(Statement st, URI reificationURI, Resource context)
		throws SailException
	{
		_add(reificationURI, RDF.TYPE, RDF.STATEMENT, context);
		_add(reificationURI, RDF.SUBJECT, st.getSubject(), context);
		_add(reificationURI, RDF.PREDICATE, st.getPredicate(), context);
		_add(reificationURI, RDF.OBJECT, st.getObject(), context);

		_autoCommit();
	}

	private void _add(Statement st)
		throws SailException
	{
		_add(st, st.getContext());
	}

	private void _add(Statement st, Resource context)
		throws SailException
	{
		_add(st.getSubject(), st.getPredicate(), st.getObject(), context);
	}

	private void _add(Resource subject, URI predicate, Value object, Resource context)
		throws SailException
	{
		_sailConnection.addStatement(subject, predicate, object, context);
	}

	public void evaluateTupleQuery(QueryLanguage ql, String query, TupleQueryResultHandler handler)
		throws MalformedQueryException, TupleQueryResultHandlerException, UnsupportedQueryLanguageException
	{
		evaluateTupleQuery(ql, query, INCLUDE_INFERRED_DEFAULT, handler);
	}

	public void evaluateTupleQuery(QueryLanguage ql, String query, boolean includeInferred,
			TupleQueryResultHandler handler)
		throws MalformedQueryException, TupleQueryResultHandlerException, UnsupportedQueryLanguageException
	{
		TupleQuery tupleQuery = QueryParserUtil.parseTupleQuery(ql, query);
		evaluate(tupleQuery, includeInferred, handler);
	}

	public TupleQueryResult evaluateTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		return evaluateTupleQuery(ql, query, INCLUDE_INFERRED_DEFAULT);
	}

	public TupleQueryResult evaluateTupleQuery(QueryLanguage ql, String query, boolean includeInferred)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		TupleQuery tupleQuery = QueryParserUtil.parseTupleQuery(ql, query);
		return _sailConnection.evaluate(tupleQuery, includeInferred);
	}

	public void evaluate(TupleQuery query, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException
	{
		evaluate(query, INCLUDE_INFERRED_DEFAULT, handler);
	}

	public void evaluate(TupleQuery query, boolean includeInferred, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException
	{
		TupleQueryResult tupleQueryResult = _sailConnection.evaluate(query, includeInferred);

		try {
			handler.startQueryResult(tupleQueryResult.getBindingNames(), tupleQueryResult.isDistinct(),
					tupleQueryResult.isOrdered());

			for (Solution result : tupleQueryResult) {
				handler.handleSolution(result);
			}

			handler.endQueryResult();
		}
		finally {
			tupleQueryResult.close();
		}
	}

	public void evaluateGraphQuery(QueryLanguage ql, String query, RDFHandler handler)
		throws MalformedQueryException, RDFHandlerException, UnsupportedQueryLanguageException
	{
		evaluateGraphQuery(ql, query, INCLUDE_INFERRED_DEFAULT, handler);
	}

	public void evaluateGraphQuery(QueryLanguage ql, String query, boolean includeInferred, RDFHandler handler)
		throws MalformedQueryException, RDFHandlerException, UnsupportedQueryLanguageException
	{
		GraphQuery graphQuery = QueryParserUtil.parseGraphQuery(ql, query);
		evaluate(graphQuery, includeInferred, handler);
	}

	public void evaluate(GraphQuery graphQuery, RDFHandler rdfHandler)
		throws RDFHandlerException
	{
		evaluate(graphQuery, INCLUDE_INFERRED_DEFAULT, rdfHandler);
	}

	public void evaluate(GraphQuery graphQuery, boolean includeInferred, RDFHandler rdfHandler)
		throws RDFHandlerException
	{
		rdfHandler.startRDF();

		reportNamespaces(graphQuery, rdfHandler);

		GraphQueryResult result = evaluateGraphQuery(graphQuery, includeInferred);

		try {
			for (Statement st : result) {
				rdfHandler.handleStatement(st);
			}
		}
		finally {
			result.close();
		}

		rdfHandler.endRDF();
	}

	private void reportNamespaces(GraphQuery graphQuery, RDFHandler rdfHandler)
		throws RDFHandlerException
	{
		Map<String, String> nsMap = new LinkedHashMap<String, String>();

		// Fill nsMap with namespaces from sail
		CloseableIterator<? extends Namespace> nsIter = _sailConnection.getNamespaces();
		try {
			while (nsIter.hasNext()) {
				Namespace ns = nsIter.next();
				nsMap.put(ns.getPrefix(), ns.getName());
			}
		}
		finally {
			nsIter.close();
		}

		Map<String, String> queryNamespaces = graphQuery.getQueryNamespaces();

		if (queryNamespaces != null) {
			// Add the namespaces specified in the query,
			// possibly overwriting the ones from sail
			for (Map.Entry<String, String> entry : queryNamespaces.entrySet()) {
				String prefix = entry.getKey();
				String name = entry.getValue();

				// Remove any entries mapping the same namespace name
				nsMap.values().remove(name);

				// This will overwrite any existing mapping with the same namespace
				nsMap.put(prefix, name);
			}
		}

		// Report this final set to the RDFHandler
		for (Map.Entry<String, String> entry : nsMap.entrySet()) {
			String prefix = entry.getKey();
			String name = entry.getValue();

			rdfHandler.handleNamespace(prefix, name);
		}
	}

	public GraphQueryResult evaluateGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		return evaluateGraphQuery(ql, query, INCLUDE_INFERRED_DEFAULT);
	}

	public GraphQueryResult evaluateGraphQuery(QueryLanguage ql, String query, boolean includeInferred)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		GraphQuery graphQuery = QueryParserUtil.parseGraphQuery(ql, query);
		return evaluateGraphQuery(graphQuery, includeInferred);
	}

	private GraphQueryResult evaluateGraphQuery(GraphQuery graphQuery, boolean includeInferred) {
		GraphQueryResult graphQueryResult = _sailConnection.evaluate(graphQuery, includeInferred);
		return graphQueryResult;
	}

	public CloseableIterator<? extends Resource> getContextIDs() {
		return _sailConnection.getContextIDs();
	}

	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred)
	{
		return _sailConnection.getStatements(subj, pred, obj, includeInferred);
	}

	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			Resource context, boolean includeInferred)
	{
		CloseableIterator<? extends Statement> result = null;
		if (context == null) {
			result = _sailConnection.getNullContextStatements(subj, pred, obj, includeInferred);
		}
		else {
			result = _sailConnection.getNamedContextStatements(subj, pred, obj, context, includeInferred);
		}

		return result;
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler)
		throws RDFHandlerException
	{
		_exportStatements(getStatements(subj, pred, obj, includeInferred), handler);
	}

	public void exportStatements(Resource subj, URI pred, Value obj, Resource context,
			boolean includeInferred, RDFHandler handler)
		throws RDFHandlerException
	{
		_exportStatements(getStatements(subj, pred, obj, context, includeInferred), handler);
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred) {
		return _hasStatement(Scope.ALL_CONTEXTS, subj, pred, obj, null, includeInferred);
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, Resource context, boolean includeInferred)
	{
		Scope scope = (context == null) ? Scope.NULL_CONTEXT : Scope.NAMED_CONTEXTS;
		return _hasStatement(scope, subj, pred, obj, context, includeInferred);
	}

	private String _serializeVar(Var var) {
		StringBuilder result = new StringBuilder();
		Value value = var.getValue();
		if (value != null) {
			if (value instanceof URI) {
				result.append("<");
				result.append(value.toString());
				result.append(">");
			}
			else {
				result.append(value.toString());
			}
		}
		else { // return the variable name
			result.append(var.getName());
		}
		return result.toString();
	}

	private boolean _hasStatement(Scope scope, Resource subj, URI pred, Value obj, Resource context,
			boolean includeInferred)
	{
		Var subjVar = new Var("subject", subj);
		Var predVar = new Var("predicate", pred);
		Var objVar = new Var("object", obj);
		Var contextVar = new Var("context", context);

		TupleExpr tupleExpr = new StatementPattern(scope, subjVar, predVar, objVar, contextVar);

		tupleExpr = new RowSelection(tupleExpr, 0, 1);

		// create a serialization in SeRQL of the query, for use in combination
		// with HTTP stores
		StringBuilder query = new StringBuilder();
		query.append(" SELECT subject, predicate, object");
		if (Scope.NAMED_CONTEXTS.equals(scope)) {
			query.append(", context ");
			query.append("FROM CONTEXT ");
			query.append(_serializeVar(contextVar));
		}
		else {
			query.append(" FROM");
		}
		query.append(" {");
		query.append(_serializeVar(subjVar));
		query.append("} ");
		query.append(_serializeVar(predVar));
		query.append(" {");
		query.append(_serializeVar(objVar));
		query.append("}");

		if (Scope.NULL_CONTEXT.equals(scope)) {
			// We need to filter out all statements that are in a named context. A
			// MINUS is used because no query language currently supports querying
			// statements from the null context specifically.
			query.append(" MINUS ");
			query.append("SELECT subject, predicate, object ");
			query.append("FROM CONTEXT namedContext");
			query.append(" {");
			query.append(_serializeVar(subjVar));
			query.append("} ");
			query.append(_serializeVar(predVar));
			query.append(" {");
			query.append(_serializeVar(objVar));
			query.append("}");
		}

		query.append(" LIMIT 1");

		TupleQuery tq = new TupleQuery(tupleExpr);
		tq.setQueryLanguage(QueryLanguage.SERQL);
		tq.setQueryString(query.toString());

		TupleQueryResult tupleQueryResult = _sailConnection.evaluate(tq, includeInferred);

		try {
			return !tupleQueryResult.isEmpty();
		}
		finally {
			tupleQueryResult.close();
		}
	}

	public boolean hasStatement(Statement st, boolean includeInferred) {
		return hasStatement(st, st.getContext(), includeInferred);
	}

	public boolean hasStatement(Statement st, Resource context, boolean includeInferred) {
		return hasStatement(st.getSubject(), st.getPredicate(), st.getObject(), context, includeInferred);
	}

	public void export(RDFHandler handler)
		throws RDFHandlerException
	{
		_exportStatements(getStatements(null, null, null, false), handler);
	}

	public void exportContext(Resource context, RDFHandler handler)
		throws RDFHandlerException
	{
		_exportStatements(getStatements(null, null, null, context, false), handler);
	}

	/**
	 * Exports all statements contained in the supplied statement iterator and
	 * all relevant namespace information to the supplied RDFHandler.
	 */
	private void _exportStatements(CloseableIterator<? extends Statement> stIter, RDFHandler handler)
		throws RDFHandlerException
	{
		try {
			handler.startRDF();

			// Export namespace information
			CloseableIterator<? extends Namespace> nsIter = _sailConnection.getNamespaces();

			try {
				while (nsIter.hasNext()) {
					Namespace ns = nsIter.next();
					handler.handleNamespace(ns.getPrefix(), ns.getName());
				}
			}
			finally {
				nsIter.close();
			}

			// Export statemnts
			while (stIter.hasNext()) {
				handler.handleStatement(stIter.next());
			}

			handler.endRDF();
		}
		finally {
			stIter.close();
		}
	}

	public int size() {
		CloseableIterator<? extends Statement> iter = getStatements(null, null, null, false);

		try {
			int size = 0;
			while (iter.hasNext()) {
				iter.next();
				size++;
			}
			return size;
		}
		finally {
			iter.close();
		}
	}

	public int size(Resource context) {
		CloseableIterator<? extends Statement> iter = getStatements(null, null, null, context, false);

		try {
			int size = 0;
			while (iter.hasNext()) {
				iter.next();
				size++;
			}
			return size;
		}
		finally {
			iter.close();
		}
	}

	public boolean isEmpty() {
		CloseableIterator<? extends Statement> iter = getStatements(null, null, null, false);

		try {
			return !iter.hasNext();
		}
		finally {
			iter.close();
		}
	}

	public void remove(QueryLanguage ql, String graphQuery)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		_remove(ql, graphQuery, null, false);
	}

	public void remove(QueryLanguage ql, String graphQuery, Resource context)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		_remove(ql, graphQuery, context, true);
	}

	protected void _remove(QueryLanguage ql, String graphQuery, Resource context, boolean enforceContext)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException
	{
		try {
			RDFRemover rdfRemover = new RDFRemover(_sailConnection);
			if (enforceContext) {
				rdfRemover.enforceContext(context);
			}

			evaluateGraphQuery(ql, graphQuery, rdfRemover);

			_autoCommit();
		}
		catch (RDFHandlerException e) {
			// RDFRemover only throws wrapped SailException's
			throw (SailException)e.getCause();
		}
	}

	public void remove(Resource subject, URI predicate, Value object)
		throws SailException
	{
		remove(subject, predicate, object, null);
	}

	public void remove(Resource subject, URI predicate, Value object, Resource context)
		throws SailException
	{
		_remove(subject, predicate, object, context);
		_autoCommit();
	}

	public void remove(Statement st)
		throws SailException
	{
		remove(st, st.getContext());
	}

	public void remove(Statement st, Resource context)
		throws SailException
	{
		_remove(st, context);
		_autoCommit();
	}

	public void remove(Statement[] statements)
		throws SailException
	{
		for (Statement st : statements) {
			_remove(st);
		}

		_autoCommit();
	}

	public void remove(Statement[] statements, Resource context)
		throws SailException
	{
		for (Statement st : statements) {
			_remove(st, context);
		}

		_autoCommit();
	}

	public void remove(CloseableIterator<? extends Statement> stIter)
		throws SailException
	{
		try {
			while (stIter.hasNext()) {
				_remove(stIter.next());
			}

			_autoCommit();
		}
		finally {
			stIter.close();
		}
	}

	public void remove(CloseableIterator<? extends Statement> stIter, Resource context)
		throws SailException
	{
		try {
			while (stIter.hasNext()) {
				_remove(stIter.next(), context);
			}

			_autoCommit();
		}
		finally {
			stIter.close();
		}
	}

	public void remove(Collection<? extends Statement> statements)
		throws SailException
	{
		for (Statement st : statements) {
			_remove(st);
		}

		_autoCommit();
	}

	public void remove(Collection<? extends Statement> statements, Resource context)
		throws SailException
	{
		for (Statement st : statements) {
			_remove(st, context);
		}

		_autoCommit();
	}

	public void removeReification(Statement st, URI reificationURI)
		throws SailException
	{
		removeReification(st, reificationURI, st.getContext());
	}

	public void removeReification(Statement st, URI reificationURI, Resource context)
		throws SailException
	{
		_remove(reificationURI, RDF.TYPE, RDF.STATEMENT, context);
		_remove(reificationURI, RDF.SUBJECT, st.getSubject(), context);
		_remove(reificationURI, RDF.PREDICATE, st.getPredicate(), context);
		_remove(reificationURI, RDF.OBJECT, st.getObject(), context);

		_autoCommit();
	}

	private void _remove(Statement st)
		throws SailException
	{
		_remove(st, st.getContext());
	}

	private void _remove(Statement st, Resource context)
		throws SailException
	{
		_remove(st.getSubject(), st.getPredicate(), st.getObject(), context);
	}

	private void _remove(Resource subject, URI predicate, Value object, Resource context)
		throws SailException
	{
		_sailConnection.removeStatement(subject, predicate, object, context);
	}

	public void clear()
		throws SailException
	{
		_sailConnection.clear();
		_autoCommit();
	}

	public void clearContext(Resource context)
		throws SailException
	{
		_sailConnection.clearContext(context);
		_autoCommit();
	}

	public void setNamespace(String prefix, String name)
		throws SailException
	{
		_sailConnection.setNamespace(prefix, name);
		_autoCommit();
	}

	public void removeNamespace(String prefix)
		throws SailException
	{
		_sailConnection.removeNamespace(prefix);
		_autoCommit();
	}

	public CloseableIterator<? extends Namespace> getNamespaces()
		throws SailException
	{
		return _sailConnection.getNamespaces();
	}
}
