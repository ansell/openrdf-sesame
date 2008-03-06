/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
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

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Abstract class implementing most 'convenience' methods in the
 * RepositoryConnection interface by transforming parameters and mapping the
 * methods to the basic (abstractly declared) methods.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class AbstractRepositoryConnection implements RepositoryConnection {

	private final Repository repository;

	private boolean isOpen;

	private boolean autoCommit;

	protected AbstractRepositoryConnection(Repository repository) {
		this.repository = repository;
		this.isOpen = true;
		this.autoCommit = true;
	}

	public Repository getRepository() {
		return repository;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void close()
		throws RepositoryException
	{
		isOpen = false;
	}

	public Query prepareQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, RepositoryException
	{
		return prepareQuery(ql, query, null);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, RepositoryException
	{
		return prepareTupleQuery(ql, query, null);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, RepositoryException
	{
		return prepareGraphQuery(ql, query, null);
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws RepositoryException
	{
		CloseableIteration<? extends Statement, RepositoryException> stIter = getStatements(subj, pred, obj,
				includeInferred, contexts);
		try {
			return stIter.hasNext();
		}
		finally {
			stIter.close();
		}
	}

	public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		return hasStatement(st.getSubject(), st.getPredicate(), st.getObject(), includeInferred, contexts);
	}

	public boolean isEmpty()
		throws RepositoryException
	{
		return size() == 0;
	}

	public void export(RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		exportStatements(null, null, null, false, handler, contexts);
	}

	public void setAutoCommit(boolean autoCommit)
		throws RepositoryException
	{
		if (autoCommit == this.autoCommit) {
			return;
		}

		this.autoCommit = autoCommit;

		// if we are switching from non-autocommit to autocommit mode, commit any
		// pending updates
		if (autoCommit) {
			commit();
		}
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		addInputStreamOrReader(in, baseURI, dataFormat, contexts);
	}

	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		addInputStreamOrReader(reader, baseURI, dataFormat, contexts);
	}

	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		InputStream in = url.openStream();

		try {
			add(in, baseURI, dataFormat, contexts);
		}
		finally {
			in.close();
		}
	}

	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		if (baseURI == null) {
			// default baseURI to file
			baseURI = file.toURI().toString();
		}

		InputStream in = new FileInputStream(file);

		try {
			add(in, baseURI, dataFormat, contexts);
		}
		finally {
			in.close();
		}
	}

	public void add(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		addWithoutCommit(subject, predicate, object, contexts);
		autoCommit();
	}

	public void add(Statement st, Resource... contexts)
		throws RepositoryException
	{
		add(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
	}

	public void add(Iterable<? extends Statement> statements, Resource... contexts)
		throws RepositoryException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		for (Statement st : statements) {
			add(st, contexts);
		}

		setAutoCommit(autoCommit);
	}

	public void add(Iteration<? extends Statement, RepositoryException> statementIter, Resource... contexts)
		throws RepositoryException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		while (statementIter.hasNext()) {
			add(statementIter.next(), contexts);
		}

		setAutoCommit(autoCommit);
	}

	public void remove(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		removeWithoutCommit(subject, predicate, object, contexts);
		autoCommit();
	}

	public void remove(Statement st, Resource... contexts)
		throws RepositoryException
	{
		removeWithoutCommit(st, contexts);
		autoCommit();
	}

	public void remove(Iterable<? extends Statement> statements, Resource... contexts)
		throws RepositoryException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		for (Statement st : statements) {
			remove(st, contexts);
		}

		setAutoCommit(autoCommit);

	}

	public void remove(Iteration<? extends Statement, RepositoryException> statementIter, Resource... contexts)
		throws RepositoryException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		while (statementIter.hasNext()) {
			remove(statementIter.next(), contexts);
		}

		setAutoCommit(autoCommit);
	}

	public void clear(Resource... contexts)
		throws RepositoryException
	{
		remove(null, null, null, contexts);
	}

	/**
	 * Adds the data that can be read from the supplied InputStream or Reader to
	 * this repository.
	 * 
	 * @param inputStreamOrReader
	 *        An {@link InputStream} or {@link Reader} containing RDF data that
	 *        must be added to the repository.
	 * @param baseURI
	 *        The base URI for the data.
	 * @param dataFormat
	 *        The file format of the data.
	 * @param context
	 *        The context to which the data should be added in case
	 *        <tt>enforceContext</tt> is <tt>true</tt>. The value
	 *        <tt>null</tt> indicates the null context.
	 * @param enforceContext
	 *        Flag controlling whether the data should be added to the specified
	 *        context. If <tt>true</tt> any context that is encoded in the data
	 *        itself should be ignored.
	 * @throws IOException
	 * @throws UnsupportedRDFormatException
	 * @throws RDFParseException
	 * @throws RepositoryException
	 */
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		RDFParser rdfParser = Rio.createParser(dataFormat, getRepository().getValueFactory());

		rdfParser.setVerifyData(true);
		rdfParser.setStopAtFirstError(true);
		rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);

		RDFInserter rdfInserter = new RDFInserter(this);

		if (contexts.length > 0) {
			rdfInserter.enforceContext(contexts);
		}
		rdfParser.setRDFHandler(rdfInserter);

		try {
			boolean autoCommit = isAutoCommit();
			setAutoCommit(false);

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

			setAutoCommit(autoCommit);
		}
		catch (RDFHandlerException e) {
			// RDFInserter only throws wrapped RepositoryExceptions
			throw (RepositoryException)e.getCause();
		}
	}

	/**
	 * Calls {@link #commit} when in auto-commit mode.
	 */
	protected void autoCommit()
		throws RepositoryException
	{
		if (isAutoCommit()) {
			commit();
		}
	}

	protected void addWithoutCommit(Statement st, Resource... contexts)
		throws RepositoryException
	{
		addWithoutCommit(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
	}

	protected abstract void addWithoutCommit(Resource subject, URI predicate, Value object,
			Resource... contexts)
		throws RepositoryException;

	protected void removeWithoutCommit(Statement st, Resource... contexts)
		throws RepositoryException
	{
		removeWithoutCommit(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
	}

	protected abstract void removeWithoutCommit(Resource subject, URI predicate, Value object,
			Resource... contexts)
		throws RepositoryException;
}
