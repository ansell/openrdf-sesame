/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.UnsupportedQueryLanguageException;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultHandler;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailException;
import org.openrdf.util.iterator.CloseableIterator;

/**
 * Main interface for updating data in and performing queries on a Sesame
 * repository. By default, a Connection is in autoCommit mode, meaning that each
 * operation corresponds to a single transaction on the underlying store.
 * autoCommit can be switched off in which case it is up to the user to handle
 * transaction commit/rollback. Note that care should be taking to always
 * properly close a Connection after one is finished with it, to free up
 * resources and avoid unnecessary locks.
 * 
 * @author jeen
 */
public interface Connection {

	/**
	 * Returns the Repository object to which this connection belongs.
	 */
	public Repository getRepository();

	/**
	 * Checks whether this connection is open. A connection is open from the
	 * moment it is created until it is closed.
	 * 
	 * @see #close()
	 */
	public boolean isOpen();

	/**
	 * Closes the connection, freeing resources. If the connection is not in
	 * autoCommit mode, all non-committed operations will be lost.
	 */
	public void close()
		throws SailException;

	/**
	 * Enables or disables auto-commit mode for the connection.
	 * 
	 * @see #commit
	 */
	public void setAutoCommit(boolean autoCommit)
		throws SailException;

	/**
	 * Checks whether the connection is in auto-commit mode.
	 */
	public boolean isAutoCommit();

	/**
	 * Commits all updates that have been performed as part of this connection
	 * sofar.
	 * 
	 * @throws SailException
	 *         If the connection could not be committed.
	 */
	public void commit()
		throws SailException;

	/**
	 * Rolls back all updates that have been performed as part of this connection
	 * sofar.
	 * 
	 * @throws SailException
	 *         If the connection could not be rolled back.
	 */
	public void rollback()
		throws SailException;

	/**
	 * Adds the RDF data that can be found at the specified URL to the
	 * repository. If the RDF data does not contain any context information by
	 * itself, the data will be added to the null context in the repository.
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
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(URL url, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds the RDF data that can be found at the specified URL to a specific
	 * context in the repository. Any context information contained in the RDF
	 * data itself will be ignored in favor of the context specified for this
	 * method.
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
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the URL.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds RDF data from the specified file to the repository. If the RDF data
	 * does not contain any context information by itself, the data will be added
	 * to the null context in the repository.
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
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(File file, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds RDF data from the specified file to a specific context in the
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
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the file.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(File file, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds RDF data from an InputStream to the repository. If the RDF data does
	 * not contain any context information by itself, the data will be added to
	 * the null context in the repository.
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
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(InputStream in, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds RDF data from an InputStream to a specific context in the repository.
	 * 
	 * @param in
	 *        An InputStream from which RDF data can be read.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the input stream.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds RDF data from a Reader to the repository. If the RDF data does not
	 * contain any context information by itself, the data will be added to the
	 * null context in the repository.
	 * <p>
	 * <b>Note: using a Reader to upload byte-based data means that you have to
	 * be careful not to destroy the data's character encoding by enforcing a
	 * default character encoding upon the bytes. If possible, adding such data
	 * using an InputStream is to be preferred.</b>
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
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(Reader reader, String baseURI, RDFFormat dataFormat)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds RDF data from a Reader to a specific context in the repository.
	 * <b>Note: using a Reader to upload byte-based data means that you have to
	 * be careful not to destroy the data's character encoding by enforcing a
	 * default character encoding upon the bytes. If possible, adding such data
	 * using an InputStream is to be preferred.</b>
	 * 
	 * @param reader
	 *        A Reader from which RDF data can be read.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the reader.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource context)
		throws IOException, UnsupportedRDFormatException, RDFParseException, SailException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on this
	 * repository to the null context in this repository. Blank nodes in the data
	 * generated by the query are reinserted as-is, keeping their IDs intact.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on this
	 * repository to the null context in this repository. Blank node IDs can
	 * either be preserved, or can be mapped to new blank node IDs.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param preserveBNodeIDs
	 *        Flag indicating whether blank node IDs should be preserved.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on this
	 * repository to a specific context in this repository. Blank nodes in the
	 * data generated by the query are reinserted as-is.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, Resource context)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on this
	 * repository to a specific context in this repository. Blank node IDs can
	 * either be preserved, or can be mapped to new blank node IDs.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @param preserveBNodeIDs
	 *        Flag indicating whether blank node IDs should be preserved.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, Resource context, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on the
	 * specified query-repository to the null context in this repository. Blank
	 * nodes in the data generated by the query are reinserted as-is.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param queryRepository
	 *        The repository to evaluate the query on.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on the
	 * specified query-repository to the null context in this repository. Blank
	 * node IDs can either be preserved, or can be mapped to new blank node IDs.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param queryRepository
	 *        The repository to evaluate the query on.
	 * @param preserveBNodeIDs
	 *        Flag indicating whether blank node IDs should be preserved.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository, boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on the
	 * specified query-repository to a specific context in this repository. Blank
	 * nodes in the data generated by the query are reinserted as-is.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param queryRepository
	 *        The repository to evaluate the query on.
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository, Resource context)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds the RDF data generated by evaluating the supplied graph query on the
	 * specified query-repository to a specific context in this repository. Blank
	 * node IDs can either be preserved, or can be mapped to new blank node IDs.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param queryRepository
	 *        The repository to evaluate the query on.
	 * @param context
	 *        A resource identifying the named context to add the data to, or
	 *        <tt>null</tt> to add the data to the null context.
	 * @param preserveBNodeIDs
	 *        Flag indicating whether blank node IDs should be preserved.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(QueryLanguage ql, String graphQuery, Repository queryRepository, Resource context,
			boolean preserveBNodeIDs)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Adds a statement with the specified subject, predicate and object to the
	 * null context in this repository.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(Resource subject, URI predicate, Value object)
		throws SailException;

	/**
	 * Adds a statement with the specified subject, predicate and object to a
	 * specific context in this repository.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @param context
	 *        A resource identifying the named context to add the statement to,
	 *        or <tt>null</tt> to add the statement to the null context.
	 * @throws SailException
	 *         If the data could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(Resource subject, URI predicate, Value object, Resource context)
		throws SailException;

	/**
	 * Adds the supplied statement to the repository, using the context
	 * information carried by the statement.
	 * 
	 * @param st
	 *        The statement to add.
	 * @throws SailException
	 *         If the statement could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(Statement st)
		throws SailException;

	/**
	 * Adds the supplied statement to a specific context in this repository,
	 * ignoring any context information carried by the statement itself.
	 * 
	 * @param st
	 *        The statement to add.
	 * @param context
	 *        A resource identifying the named context to add the statement to,
	 *        or <tt>null</tt> to add the statement to the null context.
	 * @throws SailException
	 *         If the statement could not be added to the repository, for example
	 *         because the repository is not writable.
	 */
	public void add(Statement st, Resource context)
		throws SailException;

	/**
	 * Adds the supplied statements to the repository, using the context
	 * information carried by the statements.
	 * 
	 * @param statements
	 *        The statements to add.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 */
	public void add(Statement[] statements)
		throws SailException;

	/**
	 * Adds the supplied statements to a specific context in this repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param statements
	 *        The statements to add.
	 * @param context
	 *        A resource identifying the named context to add the statements to,
	 *        or <tt>null</tt> to add the statements to the null context.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 */
	public void add(Statement[] statements, Resource context)
		throws SailException;

	/**
	 * Adds the supplied statements to the repository, using the context
	 * information carried by the statements.
	 * 
	 * @param statementIter
	 *        The statements to add. The iterator will be closed by the method
	 *        after its contents have been consumed.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 */
	public void add(CloseableIterator<? extends Statement> statementIter)
		throws SailException;

	/**
	 * Adds the supplied statements to a specific context in this repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param statementIter
	 *        The statements to add. The iterator will be closed by the method
	 *        after its contents have been consumed.
	 * @param context
	 *        A resource identifying the named context to add the statements to,
	 *        or <tt>null</tt> to add the statements to the null context.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 */
	public void add(CloseableIterator<? extends Statement> statementIter, Resource context)
		throws SailException;

	/**
	 * Adds the supplied statements to the repository, using the context
	 * information carried by the statements.
	 * 
	 * @param statements
	 *        A Collection containing the Statement objects that should be added.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @throws ClassCastException
	 *         If the supplied collection contains non-Statement objects.
	 */
	public void add(Collection<? extends Statement> statements)
		throws SailException;

	/**
	 * Adds the supplied statements to a specific context in this repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param statements
	 *        A Collection containing the Statement objects that should be added.
	 * @param context
	 *        A resource identifying the named context to add the statements to,
	 *        or <tt>null</tt> to add the statements to the null context.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @throws ClassCastException
	 *         If the supplied collection contains non-Statement objects.
	 */
	public void add(Collection<? extends Statement> statements, Resource context)
		throws SailException;

	/**
	 * Adds the supplied statements to this repository, ignoring any context
	 * information carried by the statements themselves.
	 * 
	 * @param result
	 *        A GraphQueryResult containing the Statement objects that should be
	 *        added. The GraphQueryResult will be closed after all statements
	 *        have been consumed.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @throws ClassCastException
	 *         If the supplied collection contains non-Statement objects.
	 */
	public void add(GraphQueryResult result)
		throws SailException;

	/**
	 * Adds the supplied statements to the specified context to this repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param result
	 *        A GraphQueryResult containing the Statement objects that should be
	 *        added. The GraphQueryResult will be closed after all statements
	 *        have been consumed.
	 * @param context
	 *        A resource identifying the named context to add the statements to,
	 *        or <tt>null</tt> to add the statements to the null context.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 * @throws ClassCastException
	 *         If the supplied collection contains non-Statement objects.
	 */
	public void add(GraphQueryResult result, Resource context)
		throws SailException;

	/**
	 * Adds a reification of the supplied statement to the repository, using the
	 * context information carried by the stamtent. The statement will be reified
	 * using the supplied reification URI. Calling this method is equivalent to
	 * adding the following four statements separately:<br>
	 * <i>reificationURI</i> <tt>rdf:type</tt> <tt>rdf:Statement</tt><br>
	 * <i>reificationURI</i> <tt>rdf:subject</tt> <i>subject</i><br>
	 * <i>reificationURI</i> <tt>rdf:predicate</tt> <i>predicate</i><br>
	 * <i>reificationURI</i> <tt>rdf:object</tt> <i>object</i><br>
	 * 
	 * @param st
	 *        The statement to reify.
	 * @param reificationURI
	 *        The URI for the reification.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 */
	public void addReification(Statement st, URI reificationURI)
		throws SailException;

	/**
	 * Adds a reification of the supplied statement to a specific context in the
	 * repository, ignoring any context information carried by the statement
	 * itself. The statement will be reified using the supplied reification URI.
	 * Calling this method is equivalent to adding the following four statements
	 * separately:<br>
	 * <i>reificationURI</i> <tt>rdf:type</tt> <tt>rdf:Statement</tt><br>
	 * <i>reificationURI</i> <tt>rdf:subject</tt> <i>subject</i><br>
	 * <i>reificationURI</i> <tt>rdf:predicate</tt> <i>predicate</i><br>
	 * <i>reificationURI</i> <tt>rdf:object</tt> <i>object</i><br>
	 * 
	 * @param st
	 *        The statement to reify.
	 * @param reificationURI
	 *        The URI for the reification.
	 * @param context
	 *        A resource identifying the named context to add the reification to,
	 *        or <tt>null</tt> to add the reification to the null context.
	 * @throws SailException
	 *         If the statements could not be added to the repository, for
	 *         example because the repository is not writable.
	 */
	public void addReification(Statement st, URI reificationURI, Resource context)
		throws SailException;

	/**
	 * Evaluates queries that produce sets of value tuples and reports these
	 * values to the supplied handler.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param handler
	 *        The handler that will handle the resulting value tuples.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a tuple query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 * @throws TupleQueryResultHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void evaluateTupleQuery(QueryLanguage ql, String query, TupleQueryResultHandler handler)
		throws MalformedQueryException, TupleQueryResultHandlerException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates queries that produce sets of value tuples and reports these
	 * values to the supplied handler.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param includeInferred
	 *        If false, only results from explicit statements will be returned.
	 *        If true, results from any inferred triples (if available in the
	 *        store) will be returned as well. The default is true.
	 * @param handler
	 *        The handler that will handle the resulting value tuples.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a tuple query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 * @throws TupleQueryResultHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void evaluateTupleQuery(QueryLanguage ql, String query, boolean includeInferred,
			TupleQueryResultHandler handler)
		throws MalformedQueryException, TupleQueryResultHandlerException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates queries that produce sets of value tuples.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a tuple query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 */
	public TupleQueryResult evaluateTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates queries that produce sets of value tuples.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param includeInferred
	 *        If false, only results from explicit statements will be returned.
	 *        If true, results from any inferred triples (if available in the
	 *        store) will be returned as well. The default is true.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a tuple query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 */
	public TupleQueryResult evaluateTupleQuery(QueryLanguage ql, String query, boolean includeInferred)
		throws MalformedQueryException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates this tuple set query against the supplied Sail and reports the
	 * query results to the supplied TupleQueryResultHandler.
	 * 
	 * @param query
	 *        an object representation of the tuple query to be evaluated
	 * @param handler
	 *        The TupleQueryResultHandler to which query results are reported.
	 */
	public void evaluate(TupleQuery query, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException;

	/**
	 * Evaluates this tuple set query against the supplied Sail and reports the
	 * query results to the supplied TupleQueryResultHandler. *
	 * 
	 * @param query
	 *        an object representation of the tuple query to be evaluated
	 * @param includeInferred
	 *        if false, only results from explicit statements will be returned.
	 *        If true, results from any inferred statements will also be included
	 *        (if available in the store). The default is true.
	 * @param handler
	 *        The TupleQueryResultHandler to which query results are reported.
	 */
	public void evaluate(TupleQuery query, boolean includeInferred, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException;

	/**
	 * Evaluates queries that produce RDF graphs and reports the statements of
	 * these graphs to the specified listener.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param handler
	 *        The handler that will handle the statements.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a graph query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void evaluateGraphQuery(QueryLanguage ql, String query, RDFHandler handler)
		throws MalformedQueryException, RDFHandlerException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates queries that produce RDF graphs and reports the statements of
	 * these graphs to the specified listener.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param includeInferred
	 *        if false, only explicit statements will be returned. If true,
	 *        inferred statements will also be included (if available in the
	 *        store). The default is true.
	 * @param handler
	 *        The handler that will handle the statements.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a graph query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void evaluateGraphQuery(QueryLanguage ql, String query, boolean includeInferred, RDFHandler handler)
		throws MalformedQueryException, RDFHandlerException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates queries that produce RDF graphs and returns these as a
	 * GraphQueryResult.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @return A GraphQueryResult iterable object containing the set of query
	 *         results in the form of RDF statements.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a graph query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 */
	public GraphQueryResult evaluateGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates queries that produce RDF graphs and returns these as a
	 * GraphQueryResult.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param includeInferred
	 *        if false, only explicit statements will be returned. If true,
	 *        inferred statements will also be included (if available in the
	 *        store). The default is true.
	 * @return A GraphQueryResult iterable object containing the set of query
	 *         results in the form of RDF statements.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a graph query.
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed.
	 */
	public GraphQueryResult evaluateGraphQuery(QueryLanguage ql, String query, boolean includeInferred)
		throws MalformedQueryException, UnsupportedQueryLanguageException;

	/**
	 * Evaluates this graph query against the supplied Sail and reports the query
	 * results to the supplied RDFHandler.
	 * 
	 * @param query
	 *        an object representation of the graph query to be evaluated
	 * @param rdfHandler
	 *        An RDFHandler object to which the query results are reported.
	 */
	public void evaluate(GraphQuery query, RDFHandler rdfHandler)
		throws RDFHandlerException;

	/**
	 * Evaluates this graph query against the supplied Sail and reports the query
	 * results to the supplied RDFHandler.
	 * 
	 * @param query
	 *        an object representation of the graph query to be evaluated
	 * @param includeInferred
	 *        if false, only explicit statements will be returned. If true,
	 *        inferred statements will also be included (if available in the
	 *        store). The default is true.
	 * @param rdfHandler
	 *        An RDFHandler object to which the query results are reported.
	 */
	public void evaluate(GraphQuery query, boolean includeInferred, RDFHandler rdfHandler)
		throws RDFHandlerException;

	/**
	 * Gets all resources that are used as content identifiers. Care should be
	 * taken that the returned iterator is closed to free any resources that it
	 * keeps hold of.
	 * 
	 * @return An iterator over the context identifiers.
	 */
	public CloseableIterator<? extends Resource> getContextIDs();

	/**
	 * Gets all statements with a specific subject, predicate and/or object from
	 * the repository. All three parameters may be <tt>null</tt> to indicate
	 * wildcards.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @return The statements matching te specified pattern.
	 */
	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred);

	/**
	 * Gets all statements with a specific subject, predicate and/or object from
	 * the specified context in the repository.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param context
	 *        A resource identifying the named context to get the statements
	 *        from, or <tt>null</tt> to get the statements from the null
	 *        context.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @return The statements matching te specified pattern.
	 */
	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			Resource context, boolean includeInferred);

	/**
	 * Gets all statements with a specific subject, predicate and/or object from
	 * the repository. All three parameters may be <tt>null</tt> to indicate
	 * wildcards.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param handler
	 *        The handler that will handle the RDF data.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler)
		throws RDFHandlerException;

	/**
	 * Gets all statements with a specific subject, predicate and/or object from
	 * the specified context in the repository.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param context
	 *        A resource identifying the named context to get the statements
	 *        from, or <tt>null</tt> to get the statements from the null
	 *        context.
	 * @param handler
	 *        The handler that will handle the RDF data.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void exportStatements(Resource subj, URI pred, Value obj, Resource context,
			boolean includeInferred, RDFHandler hander)
		throws RDFHandlerException;

	/**
	 * Checks whether the repository contains statements with a specific subject,
	 * predicate and/or object. All three parameters may be <tt>null</tt> to
	 * indicate wildcards.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param includeInferred
	 *        if false, no inferred statements are considered; if true, inferred
	 *        statements are considered if available
	 * @return true If a matching statement is in the repository, false
	 *         otherwise.
	 */
	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred);

	/**
	 * Checks whether the repository contains statements with a specific subject,
	 * predicate and/or object in the specified context.
	 * 
	 * @param subj
	 *        The subject, or null if the subject doesn't matter.
	 * @param pred
	 *        The predicate, or null if the predicate doesn't matter.
	 * @param obj
	 *        The object, or null if the object doesn't matter.
	 * @param context
	 *        A resource identifying the named context to get the statements
	 *        from, or <tt>null</tt> to get the statements from the null
	 *        context.
	 * @param includeInferred
	 *        if false, no inferred statements are considered; if true, inferred
	 *        statements are considered if available
	 * @return true If a matching statement is in the repository in the specified
	 *         context, false otherwise.
	 */
	public boolean hasStatement(Resource subj, URI pred, Value obj, Resource context, boolean includeInferred);

	/**
	 * Checks whether the repository contains the specified statement, using the
	 * context information carried by the statement.
	 * 
	 * @param st
	 *        The statement to look for.
	 * @param includeInferred
	 *        if false, no inferred statements are considered; if true, inferred
	 *        statements are considered if available
	 * @return true If the repository contains the specified statement, false
	 *         otherwise.
	 */
	public boolean hasStatement(Statement st, boolean includeInferred);

	/**
	 * Checks whether the repository contains the specified statement in the
	 * specified context, ignoring any context information carried by the
	 * statement itself.
	 * 
	 * @param st
	 *        The statement to look for.
	 * @param context
	 *        A resource identifying the named context to look in, or
	 *        <tt>null</tt> to look in the null context.
	 * @param includeInferred
	 *        if false, no inferred statements are considered; if true, inferred
	 *        statements are considered if available
	 * @return true If the repository contains the specified statement, false
	 *         otherwise.
	 */
	public boolean hasStatement(Statement st, Resource context, boolean includeInferred);

	/**
	 * Exports all RDF data contained in this repository to the supplied
	 * RDHHandler. Note that only explicit statements are exported.
	 * 
	 * @param handler
	 *        The handler that will handle the RDF data.
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void export(RDFHandler handler)
		throws RDFHandlerException;

	/**
	 * Exports all RDF data from a specific context in this repository to the
	 * supplied RDFHandler. Note that only explicit statements are exported.
	 * 
	 * @param context
	 *        A resource identifying the named context to get the statements
	 *        from, or <tt>null</tt> to get the statements from the null
	 *        context.
	 * @param handler
	 *        The handler that will handle the statements.
	 * @throws RDFHandlerException
	 *         If the handler encounters an unrecoverable error.
	 */
	public void exportContext(Resource context, RDFHandler handler)
		throws RDFHandlerException;

	/**
	 * Returns the number of (explicit) statements in this repository. Note that
	 * currently the size is determined by iterating over all explicit statements
	 * in the repository, which <em>can</em> be quite expensive on large
	 * repositories.
	 * 
	 * @return The number of explicit statements in this repository.
	 */
	public int size();

	/**
	 * Returns the number of (explicit) statements that are in a specific context
	 * in this repository. Note that currently the size is determined by
	 * iterating over all explicit statements from the context in the repository,
	 * which <em>can</em> be quite expensive on large repositories.
	 * 
	 * @param context
	 *        A resource identifying the named context to count the statements
	 *        from, or <tt>null</tt> to count the statements from the null
	 *        context.
	 * @return The number of explicit statements from the specified context in
	 *         this repository.
	 */
	public int size(Resource context);

	/**
	 * Returns <tt>true</tt> if this repository does not contain any (explicit)
	 * statements.
	 * 
	 * @return <tt>true</tt> if this repository is empty, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isEmpty();

	/**
	 * Removes the RDF data generated by evaluating the supplied graph query on
	 * this repository from the null context in the repository.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(QueryLanguage ql, String graphQuery)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Removes the RDF data generated by evaluating the supplied graph query on
	 * this repository from a specific context in the repository.
	 * 
	 * @param ql
	 *        The query language in which the query is formulated.
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param context
	 *        A resource identifying the named context to remove the data from,
	 *        or <tt>null</tt> to remove the data from the null context.
	 * @throws MalformedQueryException
	 *         If the supplied query is incorrect.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(QueryLanguage ql, String graphQuery, Resource context)
		throws MalformedQueryException, SailException, UnsupportedQueryLanguageException;

	/**
	 * Removes the statement with the specified subject, predicate and object
	 * from the null context in the repository.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @throws SailException
	 *         If the statement could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(Resource subject, URI predicate, Value object)
		throws SailException;

	/**
	 * Removes the statement with the specified subject, predicate and object
	 * from a specific context in the repository.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @param context
	 *        A resource identifying the named context to remove the statement
	 *        from, or <tt>null</tt> to remove the statement from the null
	 *        context.
	 * @throws SailException
	 *         If the statement could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(Resource subject, URI predicate, Value object, Resource context)
		throws SailException;

	/**
	 * Removes the supplied statement from the repository, using the context
	 * information carried by the statement itself.
	 * 
	 * @param st
	 *        The statement to remove.
	 * @throws SailException
	 *         If the statement could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(Statement st)
		throws SailException;

	/**
	 * Removes the supplied statement from a specific context in the repository,
	 * ignoring any context information carried by the statement itself.
	 * 
	 * @param st
	 *        The statement to remove.
	 * @param context
	 *        A resource identifying the named context to remove the statement
	 *        from, or <tt>null</tt> to remove the statement from the null
	 *        context.
	 * @throws SailException
	 *         If the statement could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(Statement st, Resource context)
		throws SailException;

	/**
	 * Removes the supplied statements from the repository, using the context
	 * information carried by the statements themselves.
	 * 
	 * @param statements
	 *        The statements to remove.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(Statement[] statements)
		throws SailException;

	/**
	 * Removes the supplied statements from a specific context in the repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param statements
	 *        The statements to remove.
	 * @param context
	 *        A resource identifying the named context to remove the statements
	 *        from, or <tt>null</tt> to remove the statements from the null
	 *        context.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(Statement[] statements, Resource context)
		throws SailException;

	/**
	 * Removes the supplied statements from the repository, using the context
	 * information carried by the statements themselves.
	 * 
	 * @param statementIter
	 *        The statements to remove. The iterator will be closed by the method
	 *        after consuming its contents.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(CloseableIterator<? extends Statement> statementIter)
		throws SailException;

	/**
	 * Removes the supplied statements from a specific context in the repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param statementIter
	 *        The statements to remove. The iterator will be closed by the method
	 *        after consuming its contents.
	 * @param context
	 *        A resource identifying the named context to remove the statements
	 *        from, or <tt>null</tt> to remove the statements from the null
	 *        context.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void remove(CloseableIterator<? extends Statement> statementIter, Resource context)
		throws SailException;

	/**
	 * Removes the supplied statements from the repository, using the context
	 * information carried by the statements themselves.
	 * 
	 * @param statements
	 *        A Collection containing the Statement objects that should be
	 *        removed.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 * @throws ClassCastException
	 *         If the supplied collection contains non-Statement objects.
	 */
	public void remove(Collection<? extends Statement> statements)
		throws SailException;

	/**
	 * Removes the supplied statements from a specific context in the repository,
	 * ignoring any context information carried by the statements themselves.
	 * 
	 * @param statements
	 *        A Collection containing the Statement objects that should be
	 *        removed.
	 * @param context
	 *        A resource identifying the named context to remove the statements
	 *        from, or <tt>null</tt> to remove the statements from the null
	 *        context.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 * @throws ClassCastException
	 *         If the supplied collection contains non-Statement objects.
	 */
	public void remove(Collection<? extends Statement> statements, Resource context)
		throws SailException;

	/**
	 * Removes a reification of the supplied statement from the repository, using
	 * the context information carried by the statement itself. The supplied
	 * reification URI indicates the URI of the reification. Calling this method
	 * is equivalent to removing the following four statements separately:<br>
	 * <i>reificationURI</i> <tt>rdf:type</tt> <tt>rdf:Statement</tt><br>
	 * <i>reificationURI</i> <tt>rdf:subject</tt> <i>subject</i><br>
	 * <i>reificationURI</i> <tt>rdf:predicate</tt> <i>predicate</i><br>
	 * <i>reificationURI</i> <tt>rdf:object</tt> <i>object</i><br>
	 * 
	 * @param st
	 *        The statement to remove the reification for.
	 * @param reificationURI
	 *        The URI of the reification.
	 * @throws SailException
	 *         If the reification could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void removeReification(Statement st, URI reificationURI)
		throws SailException;

	/**
	 * Removes a reification of the supplied statement from a specific context in
	 * the repository, ignoring any context information carried by the statement
	 * itself. The supplied reification URI indicates the URI of the reification.
	 * Calling this method is equivalent to removing the following four
	 * statements separately:<br>
	 * <i>reificationURI</i> <tt>rdf:type</tt> <tt>rdf:Statement</tt><br>
	 * <i>reificationURI</i> <tt>rdf:subject</tt> <i>subject</i><br>
	 * <i>reificationURI</i> <tt>rdf:predicate</tt> <i>predicate</i><br>
	 * <i>reificationURI</i> <tt>rdf:object</tt> <i>object</i><br>
	 * 
	 * @param st
	 *        The statement to remove the reification for.
	 * @param reificationURI
	 *        The URI of the reification.
	 * @param context
	 *        A resource identifying the named context to remove the reification
	 *        from, or <tt>null</tt> to remove the reification from the null
	 *        context.
	 * @throws SailException
	 *         If the reification could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void removeReification(Statement st, URI reificationURI, Resource context)
		throws SailException;

	/**
	 * Removes all statements from the repository.
	 * 
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void clear()
		throws SailException;

	/**
	 * Removes all statements from a specific context.
	 * 
	 * @param context
	 *        A resource identifying the named context to remove the statements
	 *        from, or <tt>null</tt> to remove the statements from the null
	 *        context.
	 * @throws SailException
	 *         If the statements could not be removed from the repository, for
	 *         example because the repository is not writable.
	 */
	public void clearContext(Resource context)
		throws SailException;

	/**
	 * Sets the prefix of a namespace. The new prefix must be unique; it is not
	 * allowed to be associated with any other namespace.
	 * 
	 * @param prefix
	 *        The new prefix.
	 * @param name
	 *        The namespace name for which the associated prefix should be set.
	 * @throws SailException
	 *         If the namespace prefix could not be changed, possibly because the
	 *         prefix is already used for another namespace.
	 */
	public void setNamespace(String prefix, String name)
		throws SailException;

	/**
	 * Gets all declared namespaces as a CloseableIterator of {@link Namespace}
	 * objects. Each Namespace object consists of a prefix and a namespace name.
	 * 
	 * @return A CloseableIterator of Namespace objects.
	 */
	public CloseableIterator<? extends Namespace> getNamespaces()
		throws SailException;

	/**
	 * Removes a namespace declaration by removing the association between a
	 * prefix and a namespace name.
	 * 
	 * @param prefix
	 *        The namespace prefix of which the assocation with a namespace name
	 *        is to be removed.
	 * @throws SailException
	 *         If the namespace prefix could not be removed.
	 */
	public void removeNamespace(String prefix)
		throws SailException;

}
