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
import org.openrdf.query.Cursor;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.GraphResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

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
		sailConnection.commit();
	}

	public void rollback()
		throws StoreException
	{
		sailConnection.rollback();
	}

	@Override
	public void close()
		throws StoreException
	{
		sailConnection.close();
		super.close();
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
		return createRepositoryResult(sailConnection.getContextIDs());
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		return createGraphResult(sailConnection.getStatements(subj, pred, obj, includeInferred,
				contexts));
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
		return sailConnection.size(contexts);
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		sailConnection.addStatement(subject, predicate, object, contexts);
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		sailConnection.removeStatements(subject, predicate, object, contexts);
	}

	@Override
	public void clear(Resource... contexts)
		throws StoreException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		sailConnection.removeStatements(null, null, null, contexts);
		autoCommit();
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		sailConnection.setNamespace(prefix, name);
		autoCommit();
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		sailConnection.removeNamespace(prefix);
		autoCommit();
	}

	public void clearNamespaces()
		throws StoreException
	{
		sailConnection.clearNamespaces();
		autoCommit();
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws StoreException
	{
		return createRepositoryResult(sailConnection.getNamespaces());
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		return sailConnection.getNamespace(prefix);
	}

	/**
	 * Wraps a CloseableIteration coming from a Sail in a RepositoryResult
	 * object, applying the required conversions
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(
			Cursor<? extends E> sailIter)
	{
		return new RepositoryResult<E>(sailIter);
	}

	/**
	 * Wraps a CloseableIteration coming from a Sail in a GraphResult
	 * object, applying the required conversions
	 */
	protected <E> GraphResult createGraphResult(
			Cursor<? extends Statement> sailIter)
	{
		return new GraphResult(sailIter);
	}
}
