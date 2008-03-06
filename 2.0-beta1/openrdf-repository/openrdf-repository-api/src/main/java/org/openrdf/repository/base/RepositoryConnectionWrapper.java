/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import info.aduna.iteration.Iteration;

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
import org.openrdf.repository.DelegatingRepositoryConnection;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

public class RepositoryConnectionWrapper implements DelegatingRepositoryConnection {

	private Repository _repository;

	private RepositoryConnection _delegate;

	public RepositoryConnectionWrapper(Repository repository, RepositoryConnection delegate) {
		_repository = repository;
		_delegate = delegate;
	}

	public RepositoryConnection getDelegate()
		throws RepositoryException
	{
		return _delegate;
	}

	public Repository getRepository() {
		return _repository;
	}

	public boolean isOpen()
		throws RepositoryException
	{
		return getDelegate().isOpen();
	}

	public void close()
		throws RepositoryException
	{
		getDelegate().close();
	}

	public Query prepareQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, RepositoryException
	{
		return getDelegate().prepareQuery(ql, query);
	}

	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return getDelegate().prepareQuery(ql, query, baseURI);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, RepositoryException
	{
		return getDelegate().prepareTupleQuery(ql, query);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return getDelegate().prepareTupleQuery(ql, query, baseURI);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, RepositoryException
	{
		return getDelegate().prepareGraphQuery(ql, query);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		return getDelegate().prepareGraphQuery(ql, query, baseURI);
	}

	public void export(RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		getDelegate().export(handler, contexts);
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		getDelegate().exportStatements(subj, pred, obj, includeInferred, handler, contexts);
	}

	public RepositoryResult<Resource> getContextIDs()
		throws RepositoryException
	{
		return getDelegate().getContextIDs();
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		return getDelegate().getStatements(subj, pred, obj, includeInferred, contexts);
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws RepositoryException
	{
		return getDelegate().hasStatement(subj, pred, obj, includeInferred, contexts);
	}

	public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		return getDelegate().hasStatement(st, includeInferred, contexts);
	}

	public long size(Resource... contexts)
		throws RepositoryException
	{
		return getDelegate().size(contexts);
	}

	public boolean isEmpty()
		throws RepositoryException
	{
		return getDelegate().isEmpty();
	}

	public void setAutoCommit(boolean autoCommit)
		throws RepositoryException
	{
		getDelegate().setAutoCommit(autoCommit);
	}

	public boolean isAutoCommit()
		throws RepositoryException
	{
		return getDelegate().isAutoCommit();
	}

	public void commit()
		throws RepositoryException
	{
		getDelegate().commit();
	}

	public void rollback()
		throws RepositoryException
	{
		getDelegate().rollback();
	}

	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		getDelegate().add(in, baseURI, dataFormat, contexts);
	}

	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		getDelegate().add(reader, baseURI, dataFormat, contexts);
	}

	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		getDelegate().add(url, baseURI, dataFormat, contexts);
	}

	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		getDelegate().add(file, baseURI, dataFormat, contexts);
	}

	public void add(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().add(subject, predicate, object, contexts);
	}

	public void add(Statement st, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().add(st, contexts);
	}

	public void add(Iterable<? extends Statement> statements, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().add(statements, contexts);
	}

	public void add(Iteration<? extends Statement, RepositoryException> statementIter, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().add(statementIter, contexts);
	}

	public void clear(Resource... contexts)
		throws RepositoryException
	{
		getDelegate().clear(contexts);
	}

	public void remove(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().remove(subject, predicate, object, contexts);
	}

	public void remove(Statement st, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().remove(st, contexts);
	}

	public void remove(Iterable<? extends Statement> statements, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().remove(statements, contexts);
	}

	public void remove(Iteration<? extends Statement, RepositoryException> statementIter, Resource... contexts)
		throws RepositoryException
	{
		getDelegate().remove(statementIter, contexts);
	}

	public String getNamespace(String prefix)
		throws RepositoryException
	{
		return getDelegate().getNamespace(prefix);
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		return getDelegate().getNamespaces();
	}

	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		getDelegate().removeNamespace(prefix);
	}

	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		getDelegate().setNamespace(prefix, name);
	}
}
