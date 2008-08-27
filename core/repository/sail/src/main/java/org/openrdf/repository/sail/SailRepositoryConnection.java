/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.GraphResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;
import org.openrdf.repository.RepositoryReadOnlyException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.StoreException;
import org.openrdf.sail.SailReadOnlyException;

/**
 * An implementation of the {@link RepositoryConnection} interface that wraps a
 * {@link SailConnection}.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public class SailRepositoryConnection extends RepositoryConnectionBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The Sail connection wrapped by this repository connection object.
	 */
	private SailConnection sailConnection;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new repository connection that will wrap the supplied
	 * SailConnection. SailRepositoryConnection objects are created by
	 * {@link SailRepository#getConnection}.
	 */
	protected SailRepositoryConnection(SailRepository repository, SailConnection sailConnection) {
		super(repository);
		this.sailConnection = sailConnection;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns the underlying SailConnection.
	 */
	public SailConnection getSailConnection() {
		return sailConnection;
	}

	public void commit()
		throws StoreException
	{
		try {
			sailConnection.commit();
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	public void rollback()
		throws StoreException
	{
		try {
			sailConnection.rollback();
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public void close()
		throws StoreException
	{
		try {
			sailConnection.close();
			super.close();
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	public SailQuery prepareQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedQuery parsedQuery = QueryParserUtil.parseQuery(ql, queryString, baseURI);

		if (parsedQuery instanceof ParsedTupleQuery) {
			return new SailTupleQuery((ParsedTupleQuery)parsedQuery, this);
		}
		else if (parsedQuery instanceof ParsedGraphQuery) {
			return new SailGraphQuery((ParsedGraphQuery)parsedQuery, this);
		}
		else if (parsedQuery instanceof ParsedBooleanQuery) {
			return new SailBooleanQuery((ParsedBooleanQuery)parsedQuery, this);
		}
		else {
			throw new RuntimeException("Unexpected query type: " + parsedQuery.getClass());
		}
	}

	public SailTupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedTupleQuery parsedQuery = QueryParserUtil.parseTupleQuery(ql, queryString, baseURI);
		return new SailTupleQuery(parsedQuery, this);
	}

	public SailGraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedGraphQuery parsedQuery = QueryParserUtil.parseGraphQuery(ql, queryString, baseURI);
		return new SailGraphQuery(parsedQuery, this);
	}

	public SailBooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedBooleanQuery parsedQuery = QueryParserUtil.parseBooleanQuery(ql, queryString, baseURI);
		return new SailBooleanQuery(parsedQuery, this);
	}

	public RepositoryResult<Resource> getContextIDs()
		throws StoreException
	{
		try {
			return createRepositoryResult(sailConnection.getContextIDs());
		}
		catch (StoreException e) {
			throw new StoreException("Unable to get context IDs from Sail", e);
		}
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			return createGraphResult(sailConnection.getStatements(subj, pred, obj, includeInferred,
					contexts));
		}
		catch (StoreException e) {
			throw new StoreException("Unable to get statements from Sail", e);
		}
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws StoreException, RDFHandlerException
	{
		handler.startRDF();

		// Export namespace information
		CloseableIteration<? extends Namespace, StoreException> nsIter = getNamespaces();
		try {
			while (nsIter.hasNext()) {
				Namespace ns = nsIter.next();
				handler.handleNamespace(ns.getPrefix(), ns.getName());
			}
		}
		finally {
			nsIter.close();
		}

		// Export statements
		CloseableIteration<? extends Statement, StoreException> stIter = getStatements(subj, pred, obj,
				includeInferred, contexts);

		try {
			while (stIter.hasNext()) {
				handler.handleStatement(stIter.next());
			}
		}
		finally {
			stIter.close();
		}

		handler.endRDF();
	}

	public long size(Resource... contexts)
		throws StoreException
	{
		try {
			return sailConnection.size(contexts);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		try {
			sailConnection.addStatement(subject, predicate, object, contexts);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		try {
			sailConnection.removeStatements(subject, predicate, object, contexts);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws StoreException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			sailConnection.clear(contexts);
			autoCommit();
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		try {
			sailConnection.setNamespace(prefix, name);
			autoCommit();
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		try {
			sailConnection.removeNamespace(prefix);
			autoCommit();
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	public void clearNamespaces()
		throws StoreException
	{
		try {
			sailConnection.clearNamespaces();
			autoCommit();
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws StoreException
	{
		try {
			return createRepositoryResult(sailConnection.getNamespaces());
		}
		catch (StoreException e) {
			throw new StoreException("Unable to get namespaces from Sail", e);
		}
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		try {
			return sailConnection.getNamespace(prefix);
		}
		catch (StoreException e) {
			throw new StoreException(e);
		}
	}

	/**
	 * Wraps a CloseableIteration coming from a Sail in a RepositoryResult
	 * object, applying the required conversions
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(
			CloseableIteration<? extends E, StoreException> sailIter)
	{
		return new RepositoryResult<E>(new SailCloseableIteration<E>(sailIter));
	}

	/**
	 * Wraps a CloseableIteration coming from a Sail in a GraphResult
	 * object, applying the required conversions
	 */
	protected <E> GraphResult createGraphResult(
			CloseableIteration<? extends Statement, StoreException> sailIter)
	{
		return new GraphResult(new SailCloseableIteration<Statement>(sailIter));
	}
}
