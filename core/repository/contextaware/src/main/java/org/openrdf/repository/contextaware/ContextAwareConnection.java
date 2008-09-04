/*
 * Copyright James Leigh (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.contextaware;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Allows contexts to be specified at the connection level or the method level.
 * 
 * @author James Leigh
 */
public class ContextAwareConnection extends RepositoryConnectionWrapper {

	private static final URI[] ALL_CONTEXTS = new URI[0];

	private boolean includeInferred = true;

	private QueryLanguage ql = QueryLanguage.SPARQL;

	private URI[] readContexts = ALL_CONTEXTS;

	private URI[] addContexts = ALL_CONTEXTS;

	private URI[] removeContexts = ALL_CONTEXTS;

	private URI[] archiveContexts = ALL_CONTEXTS;

	public ContextAwareConnection(Repository repository)
		throws StoreException
	{
		this(repository, repository.getConnection());
	}

	public ContextAwareConnection(Repository repository, RepositoryConnection connection) {
		super(repository, connection);
	}

	@Override
	protected boolean isDelegatingRemove() {
		return archiveContexts.length == 0;
	}

	/**
	 * if false, no inferred statements are considered; if true, inferred
	 * statements are considered if available
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}


	/**
	 * if false, no inferred statements are considered; if true, inferred
	 * statements are considered if available
	 */
	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	public QueryLanguage getQueryLanguage() {
		return ql;
	}

	public void setQueryLanguage(QueryLanguage ql) {
		this.ql = ql;
	}

	/**
	 * The context(s) to get the data from. Note that this parameter is a vararg
	 * and as such is optional. If no contexts are supplied the method operates
	 * on the entire repository.
	 */
	public URI[] getReadContexts() {
		return readContexts;
	}

	/**
	 * The context(s) to get the data from. Note that this parameter is a vararg
	 * and as such is optional. If no contexts are supplied the method operates
	 * on the entire repository.
	 */
	public void setReadContexts(URI... readContexts) {
		this.readContexts = readContexts;
	}

	/**
	 * The contexts to add the statements to. Note that this parameter is a
	 * vararg and as such is optional. If no contexts are specified, each
	 * statement is added to any context specified in the statement, or if the
	 * statement contains no context, it is added without a context. If one or
	 * more contexts are specified each statement is added to these contexts,
	 * ignoring any context information in the statement itself.
	 */
	public URI[] getAddContexts() {
		return addContexts;
	}

	/**
	 * The contexts to add the statements to. Note that this parameter is a
	 * vararg and as such is optional. If no contexts are specified, each
	 * statement is added to any context specified in the statement, or if the
	 * statement contains no context, it is added without a context. If one or
	 * more contexts are specified each statement is added to these contexts,
	 * ignoring any context information in the statement itself.
	 */
	public void setAddContexts(URI... addContexts) {
		this.addContexts = addContexts;
	}

	/**
	 * The context(s) to remove the data from. Note that this parameter is a
	 * vararg and as such is optional. If no contexts are supplied the method
	 * operates on the contexts associated with the statement itself, and if no
	 * context is associated with the statement, on the entire repository.
	 */
	public URI[] getRemoveContexts() {
		return removeContexts;
	}

	/**
	 * The context(s) to remove the data from. Note that this parameter is a
	 * vararg and as such is optional. If no contexts are supplied the method
	 * operates on the contexts associated with the statement itself, and if no
	 * context is associated with the statement, on the entire repository.
	 */
	public void setRemoveContexts(URI... removeContexts) {
		this.removeContexts = removeContexts;
	}

	/**
	 * Before Statements are removed, they are first copied to these contexts.
	 */
	public URI[] getArchiveContexts() {
		return archiveContexts;
	}

	/**
	 * Before Statements are removed, they are first copied to these contexts.
	 */

	public void setArchiveContexts(URI... archiveContexts) {
		this.archiveContexts = archiveContexts;
	}


	/**
	 * Adds RDF data from the specified file to a specific contexts in the
	 * repository.
	 * 
	 * @param file
	 *        A file containing RDF data.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against. This defaults to the value of
	 *        {@link java.io.File#toURI() file.toURI()} if the value is set to
	 *        <tt>null</tt>.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the file.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws StoreException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(File file, String baseURI, RDFFormat dataFormat)
		throws IOException, RDFParseException, StoreException
	{
		super.add(file, baseURI, dataFormat, addContexts);
	}

	/**
	 * Adds RDF data from an InputStream to the repository, optionally to one or
	 * more named contexts.
	 * 
	 * @param in
	 *        An InputStream from which RDF data can be read.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the input stream.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws StoreException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(InputStream in, String baseURI, RDFFormat dataFormat)
		throws IOException, RDFParseException, StoreException
	{
		super.add(in, baseURI, dataFormat, addContexts);
	}

	/**
	 * Adds the supplied statements to this repository, optionally to one or more
	 * named contexts.
	 * 
	 * @param statements
	 *        The statements that should be added.
	 * @throws StoreException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(Iterable<? extends Statement> statements)
		throws StoreException
	{
		super.add(statements, addContexts);
	}

	/**
	 * Adds the supplied statements to this repository, optionally to one or more
	 * named contexts.
	 * 
	 * @param statementIter
	 *        The statements to add. In case the iterator is a
	 *        {@link CloseableIteration}, it will be closed before this method
	 *        returns.
	 * @throws StoreException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(Iteration<? extends Statement, StoreException> statementIter)
		throws StoreException
	{
		super.add(statementIter, addContexts);
	}

	/**
	 * Adds RDF data from a Reader to the repository, optionally to one or more
	 * named contexts. <b>Note: using a Reader to upload byte-based data means
	 * that you have to be careful not to destroy the data's character encoding
	 * by enforcing a default character encoding upon the bytes. If possible,
	 * adding such data using an InputStream is to be preferred.</b>
	 * 
	 * @param reader
	 *        A Reader from which RDF data can be read.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the reader.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws StoreException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(Reader reader, String baseURI, RDFFormat dataFormat)
		throws IOException, RDFParseException, StoreException
	{
		super.add(reader, baseURI, dataFormat, addContexts);
	}

	/**
	 * Adds a statement with the specified subject, predicate and object to this
	 * repository, optionally to one or more named contexts.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @throws StoreException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(Resource subject, URI predicate, Value object)
		throws StoreException
	{
		super.add(subject, predicate, object, addContexts);
	}

	/**
	 * Adds the supplied statement to this repository, optionally to one or more
	 * named contexts.
	 * 
	 * @param st
	 *        The statement to add.
	 * @throws StoreException
	 *         If the statement could not be added to the repository, for example
	 *         because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(Statement st)
		throws StoreException
	{
		super.add(st, addContexts);
	}

	/**
	 * Adds the RDF data that can be found at the specified URL to the
	 * repository, optionally to one or more named contexts.
	 * 
	 * @param url
	 *        The URL of the RDF data.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against. This defaults to the value of {@link
	 *        java.net.URL#toExternalForm() url.toExternalForm()} if the value is
	 *        set to <tt>null</tt>.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the URL.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws StoreException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 * @see #getAddContexts()
	 */
	public void add(URL url, String baseURI, RDFFormat dataFormat)
		throws IOException, RDFParseException, StoreException
	{
		super.add(url, baseURI, dataFormat, addContexts);
	}

	/**
	 * Removes all statements from a specific contexts in the repository.
	 * 
	 * @throws StoreException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 * @see #getRemoveContexts()
	 */
	public void clear()
		throws StoreException
	{
		super.clear(removeContexts);
	}

	/**
	 * Exports all explicit statements in the specified contexts to the supplied
	 * RDFHandler.
	 * 
	 * @param handler
	 *        The handler that will handle the RDF data.
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 * @see #getReadContexts()
	 */
	public void export(RDFHandler handler)
		throws StoreException, RDFHandlerException
	{
		super.export(handler, readContexts);
	}

	/**
	 * Exports all statements with a specific subject, predicate and/or object
	 * from the repository, optionally from the specified contexts.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param handler
	 *        The handler that will handle the RDF data.
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 * @see #getReadContexts()
	 * @see #isIncludeInferred()
	 */
	public void exportStatements(Resource subj, URI pred, Value obj, RDFHandler hander)
		throws StoreException, RDFHandlerException
	{
		super.exportStatements(subj, pred, obj, includeInferred, hander, readContexts);
	}

	/**
	 * Gets all statements with a specific subject, predicate and/or object from
	 * the repository. The result is optionally restricted to the specified set
	 * of named contexts.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @return The statements matching the specified pattern. The result object
	 *         is a {@link RepositoryResult} object, a lazy Iterator-like object
	 *         containing {@link Statement}s and optionally throwing a
	 *         {@link StoreException} when an error when a problem occurs
	 *         during retrieval.
	 * @see #getReadContexts()
	 * @see #isIncludeInferred()
	 */
	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj)
		throws StoreException
	{
		return super.getStatements(subj, pred, obj, includeInferred, readContexts);
	}

	/**
	 * Checks whether the repository contains statements with a specific subject,
	 * predicate and/or object, optionally in the specified contexts.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @return true If a matching statement is in the repository in the specified
	 *         context, false otherwise.
	 * @see #getReadContexts()
	 * @see #isIncludeInferred()
	 */
	public boolean hasStatement(Resource subj, URI pred, Value obj)
		throws StoreException
	{
		return super.hasStatement(subj, pred, obj, includeInferred, readContexts);
	}

	/**
	 * Checks whether the repository contains the specified statement, optionally
	 * in the specified contexts.
	 * 
	 * @param st
	 *        The statement to look for. Context information in the statement is
	 *        ignored.
	 * @return true If the repository contains the specified statement, false
	 *         otherwise.
	 * @see #getReadContexts()
	 * @see #isIncludeInferred()
	 */
	public boolean hasStatement(Statement st)
		throws StoreException
	{
		return super.hasStatement(st, includeInferred, readContexts);
	}

	public GraphQuery prepareGraphQueryWithinContext(String query)
		throws MalformedQueryException, StoreException
	{
		GraphQuery preparedQuery;
		if (readContexts.length == 0) {
			preparedQuery = super.prepareGraphQuery(ql, query);
		} else {
			preparedQuery = super.prepareGraphQuery(ql, query);
			DatasetImpl ds = new DatasetImpl();
			for (URI graph : readContexts) {
				ds.addDefaultGraph(graph);
			}
			preparedQuery.setDataset(ds);
		}
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public Query prepareQueryWithinContext(String query)
		throws MalformedQueryException, StoreException
	{
		Query preparedQuery;
		if (readContexts.length == 0) {
			preparedQuery = super.prepareQuery(ql, query);
		} else {
			preparedQuery = super.prepareQuery(ql, query);
			DatasetImpl ds = new DatasetImpl();
			for (URI graph : readContexts) {
				ds.addDefaultGraph(graph);
			}
			preparedQuery.setDataset(ds);
		}
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public TupleQuery prepareTupleQueryWithinContext(String query)
		throws MalformedQueryException, StoreException
	{
		TupleQuery preparedQuery;
		if (readContexts.length == 0) {
			preparedQuery = super.prepareTupleQuery(ql, query);
		} else {
			preparedQuery = super.prepareTupleQuery(ql, query);
			DatasetImpl ds = new DatasetImpl();
			for (URI graph : readContexts) {
				ds.addDefaultGraph(graph);
			}
			preparedQuery.setDataset(ds);
		}
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public GraphQuery prepareGraphQueryWithinContext(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		GraphQuery preparedQuery;
		if (readContexts.length == 0) {
			preparedQuery = super.prepareGraphQuery(ql, query, baseURI);
		} else {
			preparedQuery = super.prepareGraphQuery(ql, query);
			DatasetImpl ds = new DatasetImpl();
			for (URI graph : readContexts) {
				ds.addDefaultGraph(graph);
			}
			preparedQuery.setDataset(ds);
		}
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public Query prepareQueryWithinContext(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		Query preparedQuery;
		if (readContexts.length == 0) {
			preparedQuery = super.prepareQuery(ql, query, baseURI);
		} else {
			preparedQuery = super.prepareQuery(ql, query);
			DatasetImpl ds = new DatasetImpl();
			for (URI graph : readContexts) {
				ds.addDefaultGraph(graph);
			}
			preparedQuery.setDataset(ds);
		}
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public TupleQuery prepareTupleQueryWithinContext(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		TupleQuery preparedQuery;
		if (readContexts.length == 0) {
			preparedQuery = super.prepareTupleQuery(ql, query, baseURI);
		} else {
			preparedQuery = super.prepareTupleQuery(ql, query);
			DatasetImpl ds = new DatasetImpl();
			for (URI graph : readContexts) {
				ds.addDefaultGraph(graph);
			}
			preparedQuery.setDataset(ds);
		}
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public GraphQuery prepareGraphQuery(String query)
		throws MalformedQueryException, StoreException
	{
		GraphQuery preparedQuery = super.prepareGraphQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public Query prepareQuery(String query)
		throws MalformedQueryException, StoreException
	{
		Query preparedQuery = super.prepareQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	public TupleQuery prepareTupleQuery(String query)
		throws MalformedQueryException, StoreException
	{
		TupleQuery preparedQuery = super.prepareTupleQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		GraphQuery preparedQuery = super.prepareGraphQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		Query preparedQuery = super.prepareQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		TupleQuery preparedQuery = super.prepareTupleQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		BooleanQuery preparedQuery = super.prepareBooleanQuery(ql, query);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		GraphQuery preparedQuery = super.prepareGraphQuery(ql, query, baseURI);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		Query preparedQuery = super.prepareQuery(ql, query, baseURI);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		TupleQuery preparedQuery = super.prepareTupleQuery(ql, query, baseURI);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		BooleanQuery preparedQuery = super.prepareBooleanQuery(ql, query, baseURI);
		preparedQuery.setIncludeInferred(includeInferred);
		return preparedQuery;
	}
	
	/**
	 * Removes the supplied statements from the specified contexts in this
	 * repository.
	 * 
	 * @param statements
	 *        The statements that should be added.
	 * @throws StoreException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @see #getRemoveContexts()
	 */
	public void remove(Iterable<? extends Statement> statements)
		throws StoreException
	{
		super.remove(statements, removeContexts);
	}

	/**
	 * Removes the supplied statements from a specific context in this
	 * repository, ignoring any context information carried by the statements
	 * themselves.
	 * 
	 * @param statementIter
	 *        The statements to remove. In case the iterator is a
	 *        {@link CloseableIteration}, it will be closed before this method
	 *        returns.
	 * @throws StoreException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 * @see #getRemoveContexts()
	 */
	public void remove(Iteration<? extends Statement, StoreException> statementIter)
		throws StoreException
	{
		super.remove(statementIter, removeContexts);
	}

	/**
	 * Removes the statement with the specified subject, predicate and object
	 * from the repository, optionally restricted to the specified contexts.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @throws StoreException
	 *         If the statement could not be removed from the repository, for
	 *         example because the repository is not writable.
	 * @see #getRemoveContexts()
	 */
	public void remove(Resource subject, URI predicate, Value object)
		throws StoreException
	{
		super.remove(subject, predicate, object, removeContexts);
	}

	/**
	 * Removes the supplied statement from the specified contexts in the
	 * repository.
	 * 
	 * @param st
	 *        The statement to remove.
	 * @throws StoreException
	 *         If the statement could not be removed from the repository, for
	 *         example because the repository is not writable.
	 * @see #getRemoveContexts()
	 */
	public void remove(Statement st)
		throws StoreException
	{
		super.remove(st, removeContexts);
	}

	/**
	 * Returns the number of (explicit) statements that are in the specified
	 * contexts in this repository.
	 * 
	 * @return The number of explicit statements from the specified contexts in
	 *         this repository.
	 * @see #getReadContexts()
	 */
	public long size()
		throws StoreException
	{
		return super.size(readContexts);
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts) throws StoreException {
		RDFHandler handler = new RDFInserter(getDelegate());
		try {
			getDelegate().exportStatements(subject, predicate, object, true,
					handler, archiveContexts);
		} catch (RDFHandlerException e) {
			if (e.getCause() instanceof StoreException)
				throw (StoreException) e.getCause();
			throw new AssertionError(e);
		}
		getDelegate().remove(subject, predicate, object, contexts);
	}

}
