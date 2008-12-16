/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.GZipUtil;
import info.aduna.io.ZipUtil;

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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.results.Cursor;
import org.openrdf.results.ModelResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * Abstract class implementing most 'convenience' methods in the
 * RepositoryConnection interface by transforming parameters and mapping the
 * methods to the basic (abstractly declared) methods.
 * <p>
 * Open connections are automatically closed when being garbage collected. A
 * warning message will be logged when the system property
 * <tt>org.openrdf.repository.debug</tt> has been set to a non-<tt>null</tt>
 * value.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class RepositoryConnectionBase implements RepositoryConnection {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Repository repository;

	private boolean isOpen;

	private boolean autoCommit;

	protected RepositoryConnectionBase(Repository repository) {
		this.repository = repository;
		this.isOpen = true;
		this.autoCommit = true;
	}

	public Repository getRepository() {
		return repository;
	}

	public boolean isOpen()
		throws StoreException
	{
		return isOpen;
	}

	public void close()
		throws StoreException
	{
		isOpen = false;
	}

	public Query prepareQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		return prepareQuery(ql, query, null);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		return prepareTupleQuery(ql, query, null);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		return prepareGraphQuery(ql, query, null);
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, StoreException
	{
		return prepareBooleanQuery(ql, query, null);
	}

	/**
	 * @deprecated Use {@link #hasMatch(Resource,URI,Value,boolean,Resource...)} instead
	 */
	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		return hasMatch(subj, pred, obj, includeInferred, contexts);
	}

	public boolean hasMatch(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		ModelResult stIter = match(subj, pred, obj, includeInferred, contexts);
		try {
			return stIter.hasNext();
		}
		finally {
			stIter.close();
		}
	}

	public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return hasMatch(st.getSubject(), st.getPredicate(), st.getObject(), includeInferred, contexts);
	}

	public boolean isEmpty()
		throws StoreException
	{
		return size() == 0;
	}

	public void export(RDFHandler handler, Resource... contexts)
		throws StoreException, RDFHandlerException
	{
		exportMatch(null, null, null, false, handler, contexts);
	}

	public void setAutoCommit(boolean autoCommit)
		throws StoreException
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

	public boolean isAutoCommit()
		throws StoreException
	{
		return autoCommit;
	}

	/**
	 * Calls {@link #commit} when in auto-commit mode.
	 */
	protected void autoCommit()
		throws StoreException
	{
		if (isAutoCommit()) {
			commit();
		}
	}

	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		if (baseURI == null) {
			// default baseURI to file
			baseURI = file.toURI().toString();
		}
		if (dataFormat == null) {
			dataFormat = Rio.getParserFormatForFileName(file.getName());
		}

		InputStream in = new FileInputStream(file);
		try {
			add(in, baseURI, dataFormat, contexts);
		}
		finally {
			in.close();
		}
	}

	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}
		if (dataFormat == null) {
			dataFormat = Rio.getParserFormatForFileName(url.getPath());
		}

		InputStream in = url.openStream();
		try {
			add(in, baseURI, dataFormat, contexts);
		}
		finally {
			in.close();
		}
	}

	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		if (!in.markSupported()) {
			in = new BufferedInputStream(in, 1024);
		}

		if (ZipUtil.isZipStream(in)) {
			addZip(in, baseURI, dataFormat, contexts);
		}
		else if (GZipUtil.isGZipStream(in)) {
			add(new GZIPInputStream(in), baseURI, dataFormat, contexts);
		}
		else {
			addInputStreamOrReader(in, baseURI, dataFormat, contexts);
		}
	}

	private void addZip(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		try {
			ZipInputStream zipIn = new ZipInputStream(in);

			try {
				for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
					if (entry.isDirectory()) {
						continue;
					}

					RDFFormat format = Rio.getParserFormatForFileName(entry.getName(), dataFormat);

					try {
						// Prevent parser (Xerces) from closing the input stream
						FilterInputStream wrapper = new FilterInputStream(zipIn) {

							public void close() {
							}
						};
						add(wrapper, baseURI, format, contexts);
					}
					catch (RDFParseException e) {
						if (autoCommit) {
							rollback();
						}

						String msg = e.getMessage() + " in " + entry.getName();
						RDFParseException pe = new RDFParseException(msg, e.getLineNumber(), e.getColumnNumber());
						pe.initCause(e);
						throw pe;
					}
					finally {
						zipIn.closeEntry();
					}
				}
			}
			finally {
				zipIn.close();
			}
		}
		catch (IOException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		catch (StoreException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			setAutoCommit(autoCommit);
		}
	}

	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		addInputStreamOrReader(reader, baseURI, dataFormat, contexts);
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
	 *        <tt>enforceContext</tt> is <tt>true</tt>. The value <tt>null</tt>
	 *        indicates the null context.
	 * @throws IOException
	 * @throws UnsupportedRDFormatException
	 * @throws RDFParseException
	 * @throws StoreException
	 */
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		RDFParser rdfParser = Rio.createParser(dataFormat, getValueFactory());

		rdfParser.setVerifyData(true);
		rdfParser.setStopAtFirstError(true);
		rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

		RDFInserter rdfInserter = new RDFInserter(this);
		rdfInserter.enforceContext(contexts);
		rdfParser.setRDFHandler(rdfInserter);

		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

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
		}
		catch (RDFHandlerException e) {
			if (autoCommit) {
				rollback();
			}
			// RDFInserter only throws wrapped StoreExceptions
			throw (StoreException)e.getCause();
		}
		catch (RuntimeException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			setAutoCommit(autoCommit);
		}
	}

	public void add(Iterable<? extends Statement> statements, Resource... contexts)
		throws StoreException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		try {
			for (Statement st : statements) {
				addWithoutCommit(st, contexts);
			}
		}
		catch (StoreException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		catch (RuntimeException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			setAutoCommit(autoCommit);
		}
	}

	public void add(Cursor<? extends Statement> statementIter, Resource... contexts)
		throws StoreException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		try {
			Statement st;
			while ((st = statementIter.next()) != null) {
				addWithoutCommit(st, contexts);
			}
		}
		catch (StoreException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		catch (RuntimeException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			statementIter.close();
			setAutoCommit(autoCommit);
		}
	}

	public void add(Statement st, Resource... contexts)
		throws StoreException
	{
		addWithoutCommit(st, contexts);
		autoCommit();
	}

	public void add(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		addWithoutCommit(subject, predicate, object, contexts);
		autoCommit();
	}

	public void remove(Iterable<? extends Statement> statements, Resource... contexts)
		throws StoreException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		try {
			for (Statement st : statements) {
				remove(st, contexts);
			}
		}
		catch (StoreException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		catch (RuntimeException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			setAutoCommit(autoCommit);
		}
	}

	public void remove(Cursor<? extends Statement> statementIter, Resource... contexts)
		throws StoreException
	{
		boolean autoCommit = isAutoCommit();
		setAutoCommit(false);

		try {
			Statement st;
			while ((st = statementIter.next()) != null) {
				remove(st, contexts);
			}
		}
		catch (StoreException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		catch (RuntimeException e) {
			if (autoCommit) {
				rollback();
			}
			throw e;
		}
		finally {
			statementIter.close();
			setAutoCommit(autoCommit);
		}
	}

	public void remove(Statement st, Resource... contexts)
		throws StoreException
	{
		removeWithoutCommit(st, contexts);
		autoCommit();
	}

	@Deprecated
	public final void remove(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		removeMatch(subject, predicate, object, contexts);
	}

	public void removeMatch(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		removeWithoutCommit(subject, predicate, object, contexts);
		autoCommit();
	}

	public void clear(Resource... contexts)
		throws StoreException
	{
		removeMatch(null, null, null, contexts);
	}

	public long size(Resource... contexts)
		throws StoreException
	{
		return sizeMatch(null, null, null, false, contexts);
	}

	protected void addWithoutCommit(Statement st, Resource... contexts)
		throws StoreException
	{
		if (contexts != null && contexts.length == 0 && st.getContext() != null) {
			contexts = new Resource[] { st.getContext() };
		}

		addWithoutCommit(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
	}

	protected abstract void addWithoutCommit(Resource subject, URI predicate, Value object,
			Resource... contexts)
		throws StoreException;

	protected void removeWithoutCommit(Statement st, Resource... contexts)
		throws StoreException
	{
		if (contexts != null && contexts.length == 0 && st.getContext() != null) {
			contexts = new Resource[] { st.getContext() };
		}

		removeWithoutCommit(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
	}

	protected abstract void removeWithoutCommit(Resource subject, URI predicate, Value object,
			Resource... contexts)
		throws StoreException;
}
