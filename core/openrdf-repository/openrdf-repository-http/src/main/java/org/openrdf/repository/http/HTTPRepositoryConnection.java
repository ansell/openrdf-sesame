/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamedContextStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.AbstractRepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 */
public class HTTPRepositoryConnection extends AbstractRepositoryConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<TransactionOperation> _txn = new Vector<TransactionOperation>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepositoryConnection(HTTPRepository repository) {
		super(repository);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public HTTPRepository getRepository()
	{
		return (HTTPRepository)super.getRepository();
	}

	/**
	 * Unsupported method, throws an {@link UnsupportedOperationException}.
	 */
	public Query prepareQuery(QueryLanguage ql, String queryString, String baseURI) {
		throw new UnsupportedOperationException();
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI) {
		return new HTTPTupleQuery(this, ql, queryString, baseURI);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI) {
		return new HTTPGraphQuery(this, ql, queryString, baseURI);
	}

	public RepositoryResult<Resource> getContextIDs()
		throws RepositoryException
	{
		try {
			TupleQueryResult contextIDs = getRepository().getHTTPClient().getContextIDs();

			List<Resource> contextList = new ArrayList<Resource>();
			try {
				while (contextIDs.hasNext()) {
					BindingSet bindingSet = contextIDs.next();
					Value context = bindingSet.getValue("contextID");

					if (context instanceof Resource) {
						contextList.add((Resource)context);
					}
				}

				return new RepositoryResult<Resource>(
						new CloseableIteratorIteration<Resource, RepositoryException>(contextList.iterator()));
			}
			catch (QueryEvaluationException e) {
				throw new RepositoryException(e);
			}
			finally {
				try {
					contextIDs.close();
				}
				catch (QueryEvaluationException e) {
					throw new RepositoryException(e);
				}
			}
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		try {
			StatementCollector collector = new StatementCollector();
			exportStatements(subj, pred, obj, includeInferred, collector, contexts);
			return new RepositoryResult<Statement>(new CloseableIteratorIteration<Statement, RepositoryException>(
					collector.getStatements().iterator()));
		}
		catch (RDFHandlerException e) {
			// found a bug in StatementCollector?
			throw new RuntimeException(e);
		}
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RDFHandlerException, RepositoryException
	{
		try {
			getRepository().getHTTPClient().getStatements(subj, pred, obj, includeInferred, handler, contexts);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public long size(Resource... contexts)
		throws RepositoryException
	{
		try {
			return getRepository().getHTTPClient().size(contexts);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public void commit()
		throws RepositoryException
	{
		synchronized (_txn) {
			if (_txn.size() == 0) {
				// nothing to commit
				return;
			}

			try {
				getRepository().getHTTPClient().sendTransaction(_txn);
				_txn.clear();
			}
			catch (IOException e) {
				throw new RepositoryException(e);
			}
		}
	}

	public void rollback()
		throws RepositoryException
	{
		synchronized (_txn) {
			_txn.clear();
		}
	}

	@Override
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		HTTPClient httpClient = getRepository().getHTTPClient();
		if (inputStreamOrReader instanceof InputStream) {
			httpClient.upload(((InputStream)inputStreamOrReader), baseURI, dataFormat, false, contexts);
		}
		else if (inputStreamOrReader instanceof Reader) {
			httpClient.upload(((Reader)inputStreamOrReader), baseURI, dataFormat, false, contexts);
		}
		else {
			throw new IllegalArgumentException("inputStreamOrReader must be an InputStream or a Reader, is a: "
					+ inputStreamOrReader.getClass());
		}
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		if (contexts == null || contexts.length == 0) {
			_txn.add(new RemoveStatementsOperation(subject, predicate, object));
		}
		else {
			for (Resource context : contexts) {
				_txn.add(new RemoveNamedContextStatementsOperation(subject, predicate, object, context));
			}
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		if (contexts != null) {
			for (Resource context : contexts) {
				_txn.add(new ClearOperation(context));
			}
		}
		else {
			_txn.add(new ClearOperation(null));
		}
	}

	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		_txn.add(new RemoveNamespaceOperation(prefix));
	}

	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		_txn.add(new SetNamespaceOperation(prefix, name));
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		try {
			TupleQueryResult namespaces = getRepository().getHTTPClient().getNamespaces();

			List<Namespace> namespaceList = new ArrayList<Namespace>();
			try {
				try {
					while (namespaces.hasNext()) {
						BindingSet bindingSet = namespaces.next();
						Value prefix = bindingSet.getValue("prefix");
						Value namespace = bindingSet.getValue("namespace");

						if (prefix instanceof Literal && namespace instanceof Literal) {
							String prefixStr = ((Literal)prefix).getLabel();
							String namespaceStr = ((Literal)namespace).getLabel();
							namespaceList.add(new NamespaceImpl(prefixStr, namespaceStr));
						}
					}
				}
				catch (QueryEvaluationException e) {
					throw new RepositoryException(e);
				}

				return new RepositoryResult<Namespace>(
						new CloseableIteratorIteration<Namespace, RepositoryException>(namespaceList.iterator()));
			}
			finally {
				try {
					namespaces.close();
				}
				catch (QueryEvaluationException e) {
					throw new RepositoryException(e);
				}
			}
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public String getNamespace(String prefix)
		throws RepositoryException
	{
		try {
			return getRepository().getHTTPClient().getNamespace(prefix);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		if (contexts == null || contexts.length == 0) {
			_txn.add(new AddStatementOperation(subject, predicate, object));
		}
		else {
			for (Resource context : contexts) {
				_txn.add(new AddStatementOperation(subject, predicate, object, context));
			}
		}
	}
}
