/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.parser.BooleanQueryModel;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.repository.util.ModelNamespaceResult;
import org.openrdf.results.ContextResult;
import org.openrdf.results.ModelResult;
import org.openrdf.results.NamespaceResult;
import org.openrdf.results.impl.ContextResultImpl;
import org.openrdf.results.impl.NamespaceResultImpl;
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

	public ModelResult getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return new ModelNamespaceResult(this, sailConnection.getStatements(subj, pred, obj, includeInferred,
		contexts));
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws StoreException, RDFHandlerException
	{
		handler.startRDF();

		// Export namespace information
		RepositoryResult<? extends Namespace> nsIter = getNamespaces();
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
		RepositoryResult<? extends Statement> stIter = getStatements(subj, pred, obj,
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

	public long size(Resource subject, URI predicate, Value object, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return sailConnection.size(subject, predicate, object, includeInferred, contexts);
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
