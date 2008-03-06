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
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

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
	 * SailConnection. ConnectionImpl objects are created by
	 * {@link SailRepository#getConnection}.
	 */
	SailRepositoryConnection(SailRepository repository, SailConnection sailConnection) {
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
		throws RepositoryException
	{
		try {
			sailConnection.commit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public void rollback()
		throws RepositoryException
	{
		try {
			sailConnection.rollback();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public void close()
		throws RepositoryException
	{
		try {
			sailConnection.close();
			super.close();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public Query prepareQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedQuery parsedQuery = QueryParserUtil.parseQuery(ql, queryString, baseURI);

		if (parsedQuery instanceof ParsedTupleQuery) {
			return new SailTupleQuery((ParsedTupleQuery)parsedQuery, this);
		}
		else if (parsedQuery instanceof ParsedGraphQuery) {
			return new SailGraphQuery((ParsedGraphQuery)parsedQuery, this);
		}
		else {
			throw new RuntimeException("Unexpected query type: " + parsedQuery.getClass());
		}
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedTupleQuery parsedQuery = QueryParserUtil.parseTupleQuery(ql, queryString, baseURI);
		return new SailTupleQuery(parsedQuery, this);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedGraphQuery parsedQuery = QueryParserUtil.parseGraphQuery(ql, queryString, baseURI);
		return new SailGraphQuery(parsedQuery, this);
	}

	public RepositoryResult<Resource> getContextIDs()
		throws RepositoryException
	{
		try {
			return createRepositoryResult(sailConnection.getContextIDs());
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get context IDs from Sail", e);
		}
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("getStatements({}, {}, {}, {}, {})", new Object[] {
					subj,
					pred,
					obj,
					includeInferred,
					contexts });
		}

		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			return createRepositoryResult(sailConnection.getStatements(subj, pred, obj, includeInferred,
					contexts));
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get statements from Sail", e);
		}
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		handler.startRDF();

		// Export namespace information
		CloseableIteration<? extends Namespace, RepositoryException> nsIter = getNamespaces();
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
		CloseableIteration<? extends Statement, RepositoryException> stIter = getStatements(subj, pred, obj,
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
		throws RepositoryException
	{
		CloseableIteration<? extends Statement, RepositoryException> iter = getStatements(null, null, null,
				false, contexts);

		try {
			long size = 0;
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

	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		try {
			sailConnection.addStatement(subject, predicate, object, contexts);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		try {
			sailConnection.removeStatements(subject, predicate, object, contexts);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			sailConnection.clear(contexts);
			autoCommit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		try {
			sailConnection.setNamespace(prefix, name);
			autoCommit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		try {
			sailConnection.removeNamespace(prefix);
			autoCommit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		try {
			return createRepositoryResult(sailConnection.getNamespaces());
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get namespaces from Sail", e);
		}
	}

	public String getNamespace(String prefix)
		throws RepositoryException
	{
		try {
			return sailConnection.getNamespace(prefix);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Wraps a CloseableIteration coming from a Sail in a RepositoryResult
	 * object, applying the required conversions
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(
			CloseableIteration<? extends E, SailException> sailIter)
	{
		return new RepositoryResult<E>(new SailCloseableIteration<E>(sailIter));
	}
}
