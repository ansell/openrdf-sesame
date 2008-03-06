/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

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
import org.openrdf.repository.AbstractRepositoryConnection;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
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
public class SailRepositoryConnection extends AbstractRepositoryConnection {

	final Logger logger = LoggerFactory.getLogger(SailRepositoryConnection.class);

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The Sail connection wrapped by this repository connection object.
	 */
	private SailConnection _sailConnection;

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
		_sailConnection = sailConnection;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns the underlying SailConnection.
	 */
	public SailConnection getSailConnection() {
		return _sailConnection;
	}

	public void commit()
		throws RepositoryException
	{
		try {
			_sailConnection.commit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public void rollback()
		throws RepositoryException
	{
		try {
			_sailConnection.rollback();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public void close()
		throws RepositoryException
	{
		try {
			_sailConnection.close();
			super.close();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	protected void finalize()
		throws Throwable
	{
		if (isOpen()) {
			logger.warn("Closing dangling connection due to garbage collection");
			close();
		}

		super.finalize();
	}

	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		try {
			_sailConnection.addStatement(subject, predicate, object, contexts);
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
		CloseableIteration<? extends Resource, RepositoryException> result = null;
		try {
			result = new SailCloseableIteration<Resource>(_sailConnection.getContextIDs());
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get context IDs from Sail", e);
		}
		return new RepositoryResult<Resource>(result);
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		RepositoryResult<Statement> result = null;

		if (logger.isDebugEnabled()) {
			logger.debug("getStatements({}, {}, {}, {}, {})", new Object[] {
					subj,
					pred,
					obj,
					includeInferred,
					contexts });
		}

		try {
			result = new RepositoryResult<Statement>(new SailCloseableIteration<Statement>(_sailConnection.getStatements(subj, pred, obj, includeInferred, contexts)));
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get statements from Sail", e);
		}

		return result;
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		exportStatements(getStatements(subj, pred, obj, includeInferred, contexts), handler);
	}

	/**
	 * Exports all statements contained in the supplied statement iterator and
	 * all relevant namespace information to the supplied RDFHandler.
	 */
	private void exportStatements(CloseableIteration<? extends Statement, RepositoryException> stIter, RDFHandler handler)
		throws RepositoryException, RDFHandlerException
	{
		try {
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

	public long size(Resource... contexts)
		throws RepositoryException
	{
		CloseableIteration<? extends Statement, RepositoryException> iter = getStatements(null, null, null, false, contexts);

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

	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		try {
			_sailConnection.removeStatements(subject, predicate, object, contexts);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		try {
			_sailConnection.clear(contexts);
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
			_sailConnection.setNamespace(prefix, name);
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
			_sailConnection.removeNamespace(prefix);
			autoCommit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		CloseableIteration<? extends Namespace, RepositoryException> result = null;
		try {
			result = new SailCloseableIteration<Namespace>(_sailConnection.getNamespaces());
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get namespaces from Sail", e);
		}
		return new RepositoryResult<Namespace>(result);
	}

	public String getNamespace(String prefix)
		throws RepositoryException
	{
		try {
			return _sailConnection.getNamespace(prefix);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}
}
