/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.parser.BooleanQueryModel;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.repository.util.ModelNamespaceResult;
import org.openrdf.result.ContextResult;
import org.openrdf.result.ModelResult;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.impl.ContextResultImpl;
import org.openrdf.result.impl.NamespaceResultImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.Isolation;
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
	private final SailConnection sailConnection;

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

	public ValueFactory getValueFactory() {
		return sailConnection.getValueFactory();
	}

	public boolean isOpen()
		throws StoreException
	{
		return sailConnection.isOpen();
	}

	public void close()
		throws StoreException
	{
		sailConnection.close();
	}

	public Isolation getTransactionIsolation()
		throws StoreException
	{
		return sailConnection.getTransactionIsolation();
	}

	public void setTransactionIsolation(Isolation isolation)
		throws StoreException
	{
		sailConnection.setTransactionIsolation(isolation);
	}

	public boolean isReadOnly()
		throws StoreException
	{
		return sailConnection.isReadOnly();
	}

	public void setReadOnly(boolean readOnly)
		throws StoreException
	{
		sailConnection.setReadOnly(readOnly);
	}

	public boolean isAutoCommit()
		throws StoreException
	{
		return sailConnection.isAutoCommit();
	}

	public void begin()
		throws StoreException
	{
		sailConnection.begin();
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

	public SailQuery prepareQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		QueryModel parsedQuery = QueryParserUtil.parseQuery(ql, queryString, baseURI);

		if (parsedQuery instanceof TupleQueryModel) {
			return new SailTupleQuery((TupleQueryModel)parsedQuery, this);
		}
		else if (parsedQuery instanceof GraphQueryModel) {
			return new SailGraphQuery((GraphQueryModel)parsedQuery, this);
		}
		else if (parsedQuery instanceof BooleanQueryModel) {
			return new SailBooleanQuery((BooleanQueryModel)parsedQuery, this);
		}
		else {
			throw new RuntimeException("Unexpected query type: " + parsedQuery.getClass());
		}
	}

	public SailTupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		TupleQueryModel parsedQuery = QueryParserUtil.parseTupleQuery(ql, queryString, baseURI);
		return new SailTupleQuery(parsedQuery, this);
	}

	public SailGraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		GraphQueryModel parsedQuery = QueryParserUtil.parseGraphQuery(ql, queryString, baseURI);
		return new SailGraphQuery(parsedQuery, this);
	}

	public SailBooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		BooleanQueryModel parsedQuery = QueryParserUtil.parseBooleanQuery(ql, queryString, baseURI);
		return new SailBooleanQuery(parsedQuery, this);
	}

	public ContextResult getContextIDs()
		throws StoreException
	{
		return new ContextResultImpl(sailConnection.getContextIDs());
	}

	public ModelResult match(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return new ModelNamespaceResult(this, sailConnection.getStatements(subj, pred, obj, includeInferred,
				contexts));
	}

	public <H extends RDFHandler> H exportMatch(Resource subj, URI pred, Value obj, boolean includeInferred,
			H handler, Resource... contexts)
		throws StoreException, RDFHandlerException
	{
		handler.startRDF();

		// Export namespace information
		NamespaceResult nsIter = getNamespaces();
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
		ModelResult stIter = match(subj, pred, obj, includeInferred, contexts);

		try {
			while (stIter.hasNext()) {
				handler.handleStatement(stIter.next());
			}
		}
		finally {
			stIter.close();
		}

		handler.endRDF();
		return handler;
	}

	public long sizeMatch(Resource subject, URI predicate, Value object, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		return sailConnection.size(subject, predicate, object, includeInferred, contexts);
	}

	public void add(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		sailConnection.addStatement(subject, predicate, object, contexts);
	}

	public void removeMatch(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		sailConnection.removeStatements(subject, predicate, object, contexts);
	}

	@Override
	public void clear(Resource... contexts)
		throws StoreException
	{
		sailConnection.removeStatements(null, null, null, contexts);
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		sailConnection.setNamespace(prefix, name);
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		sailConnection.removeNamespace(prefix);
	}

	public void clearNamespaces()
		throws StoreException
	{
		sailConnection.clearNamespaces();
	}

	public NamespaceResult getNamespaces()
		throws StoreException
	{
		return new NamespaceResultImpl(sailConnection.getNamespaces());
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		return sailConnection.getNamespace(prefix);
	}
}
