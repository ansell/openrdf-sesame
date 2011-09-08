/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Rewrites some connection into other connection calls.
 * 
 * @author James Leigh
 * 
 */
public abstract class ConnectionBase implements RepositoryConnection {
	private Repository repository;
	private boolean closed;

	public ConnectionBase(Repository repository) {
		this.repository = repository;
	}

	public void close() throws RepositoryException {
		closed = true;
	}

	public void export(RDFHandler handler, Resource... contexts)
			throws RepositoryException, RDFHandlerException {
		exportStatements(null, null, null, false, handler, contexts);
	}

	public String getNamespace(String prefix) throws RepositoryException {
		return null;
	}

	public RepositoryResult<Namespace> getNamespaces()
			throws RepositoryException {
		return new RepositoryResult<Namespace>(
				new EmptyIteration<Namespace, RepositoryException>());
	}

	public Repository getRepository() {
		return repository;
	}

	public ValueFactory getValueFactory() {
		return repository.getValueFactory();
	}

	public boolean hasStatement(Statement st, boolean inf, Resource... contexts)
			throws RepositoryException {
		return hasStatement(st.getSubject(), st.getPredicate(), st.getObject(),
				inf, contexts);
	}

	public boolean isEmpty() throws RepositoryException {
		return !hasStatement(null, null, null, false);
	}

	public boolean isOpen() throws RepositoryException {
		return !closed;
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		return prepareBooleanQuery(ql, query, "");
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		return prepareGraphQuery(ql, query, "");
	}

	public Query prepareQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		return prepareQuery(ql, query, "");
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		return prepareTupleQuery(ql, query, "");
	}

	public long size(Resource... contexts) throws RepositoryException {
		RepositoryResult<Statement> stmts = getStatements(null, null, null,
				true, contexts);
		try {
			long i = 0;
			while (stmts.hasNext()) {
				stmts.next();
				i++;
			}
			return i;
		} finally {
			stmts.close();
		}
	}
}
