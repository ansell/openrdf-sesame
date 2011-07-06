/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
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
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Operation;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * RepositoryConnection that communicates with a server using the HTTP protocol.
 * Methods in this class may throw the specific RepositoryException subclasses
 * UnautorizedException and NotAllowedException, the semantics of which are
 * defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 */
class HTTPRepositoryConnection extends RepositoryConnectionBase {

	/*
	 * Note: the following debugEnabled method are private so that they can be
	 * removed when open connections no longer block other connections and they
	 * can be closed silently (just like in JDBC).
	 */
	private static boolean debugEnabled() {
		try {
			return System.getProperty("org.openrdf.repository.debug") != null;
		}
		catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example
			// when running in applets
			return false;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<TransactionOperation> txn = Collections.synchronizedList(new ArrayList<TransactionOperation>());

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private Throwable creatorTrace;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepositoryConnection(HTTPRepository repository) {
		super(repository);

		if (debugEnabled()) {
			creatorTrace = new Throwable();
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public HTTPRepository getRepository() {
		return (HTTPRepository)super.getRepository();
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		try {
			if (isOpen()) {
				if (creatorTrace != null) {
					logger.warn("Closing connection due to garbage collection, connection was create in:",
							creatorTrace);
				}
				close();
			}
		}
		finally {
			super.finalize();
		}
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

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString, String baseURI) {
		return new HTTPBooleanQuery(this, ql, queryString, baseURI);
	}

	public RepositoryResult<Resource> getContextIDs()
		throws RepositoryException
	{
		try {
			List<Resource> contextList = new ArrayList<Resource>();

			TupleQueryResult contextIDs = getRepository().getHTTPClient().getContextIDs();
			try {
				while (contextIDs.hasNext()) {
					BindingSet bindingSet = contextIDs.next();
					Value context = bindingSet.getValue("contextID");

					if (context instanceof Resource) {
						contextList.add((Resource)context);
					}
				}
			}
			finally {
				contextIDs.close();
			}

			return createRepositoryResult(contextList);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
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
			return createRepositoryResult(collector.getStatements());
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
		catch (QueryInterruptedException e) {
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
		synchronized (txn) {
			if (txn.size() > 0) {
				try {
					getRepository().getHTTPClient().sendTransaction(txn);
					txn.clear();
				}
				catch (IOException e) {
					throw new RepositoryException(e);
				}
			}
		}
	}

	public void rollback() {
		txn.clear();
	}

	@Override
	public void close()
		throws RepositoryException
	{
		if (txn.size() > 0) {
			logger.warn("Rolling back transaction due to connection close", new Throwable());
			rollback();
		}

		super.close();
	}

	@Override
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		if (isAutoCommit()) {
			// Send bytes directly to the server
			HTTPClient httpClient = getRepository().getHTTPClient();
			if (inputStreamOrReader instanceof InputStream) {
				httpClient.upload(((InputStream)inputStreamOrReader), baseURI, dataFormat, false, contexts);
			}
			else if (inputStreamOrReader instanceof Reader) {
				httpClient.upload(((Reader)inputStreamOrReader), baseURI, dataFormat, false, contexts);
			}
			else {
				throw new IllegalArgumentException(
						"inputStreamOrReader must be an InputStream or a Reader, is a: "
								+ inputStreamOrReader.getClass());
			}
		}
		else {
			// Parse files locally
			super.addInputStreamOrReader(inputStreamOrReader, baseURI, dataFormat, contexts);
		}
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		txn.add(new AddStatementOperation(subject, predicate, object, contexts));
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		txn.add(new RemoveStatementsOperation(subject, predicate, object, contexts));
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		txn.add(new ClearOperation(contexts));
		autoCommit();
	}

	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		txn.add(new RemoveNamespaceOperation(prefix));
		autoCommit();
	}

	public void clearNamespaces()
		throws RepositoryException
	{
		txn.add(new ClearNamespacesOperation());
		autoCommit();
	}

	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		if (name == null) {
			throw new NullPointerException("name must not be null");
		}
		txn.add(new SetNamespaceOperation(prefix, name));
		autoCommit();
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		try {
			List<Namespace> namespaceList = new ArrayList<Namespace>();

			TupleQueryResult namespaces = getRepository().getHTTPClient().getNamespaces();
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
			finally {
				namespaces.close();
			}

			return createRepositoryResult(namespaceList);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public String getNamespace(String prefix)
		throws RepositoryException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		try {
			return getRepository().getHTTPClient().getNamespace(prefix);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Creates a RepositoryResult for the supplied element set.
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(Iterable<? extends E> elements) {
		return new RepositoryResult<E>(new CloseableIteratorIteration<E, RepositoryException>(
				elements.iterator()));
	}

	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI)
		throws RepositoryException, MalformedQueryException
	{
		return new HTTPUpdate(this, ql, update, baseURI);
	}
}
