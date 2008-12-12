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
import java.util.List;

import org.openrdf.http.client.BooleanQueryClient;
import org.openrdf.http.client.ConnectionClient;
import org.openrdf.http.client.GraphQueryClient;
import org.openrdf.http.client.QueryClient;
import org.openrdf.http.client.RepositoryClient;
import org.openrdf.http.client.StatementClient;
import org.openrdf.http.client.TupleQueryClient;
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
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.EmptyCursor;
import org.openrdf.query.impl.IteratorCursor;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.repository.http.exceptions.IllegalStatementException;
import org.openrdf.repository.http.helpers.GraphQueryResultCursor;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

/**
 * RepositoryConnection that communicates with a server using the HTTP protocol.
 * Methods in this class may throw the specific StoreException subclasses
 * UnautorizedException and NotAllowedException, the semantics of which are
 * defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 * @author James Leigh
 */
class HTTPRepositoryConnection extends RepositoryConnectionBase {

	private static final int MAX_TRAN_QUEUE = 1024;

	@SuppressWarnings("unchecked")
	private static <T> RepositoryResult<T> emptyRepositoryResult() {
		return new RepositoryResult(EmptyCursor.emptyCursor());
	}

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

	private ConnectionClient client;

	private volatile boolean closed;

	/** If connection cannot use shared repository cache. */
	private boolean modified;

	private List<TransactionOperation> txn = new ArrayList<TransactionOperation>(MAX_TRAN_QUEUE / 2);

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private Throwable creatorTrace;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepositoryConnection(HTTPRepository repository, ConnectionClient client) {
		super(repository);
		this.client = client;

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
					logger.warn("Closing connection due to garbage collection, connection was created in:",
							creatorTrace);
				}
				close();
			}
		}
		finally {
			super.finalize();
		}
	}

	public Query prepareQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		QueryClient query = client.queries().postQuery(ql, qry, baseURI);
		if (query instanceof GraphQueryClient)
			return new HTTPGraphQuery(qry, (GraphQueryClient)query);
		if (query instanceof BooleanQueryClient)
			return new HTTPBooleanQuery(qry, (BooleanQueryClient)query);
		if (query instanceof TupleQueryClient)
			return new HTTPTupleQuery(qry, (TupleQueryClient)query);
		throw new StoreException("Unsupported query type: " + query);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		TupleQueryClient query = client.queries().postTupleQuery(ql, qry, baseURI);
		return new HTTPTupleQuery(qry, query);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		GraphQueryClient query = client.queries().postGraphQuery(ql, qry, baseURI);
		return new HTTPGraphQuery(qry, query);
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		BooleanQueryClient query = client.queries().postBooleanQuery(ql, qry, baseURI);
		return new HTTPBooleanQuery(qry, query);
	}

	public RepositoryResult<Resource> getContextIDs()
		throws StoreException
	{
		flush();
		List<Resource> contextList = new ArrayList<Resource>();

		TupleQueryResult contextIDs = client.contexts().list();
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

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj, boolean inf,
			Resource... ctx)
		throws StoreException
	{
		if (noMatch(subj, pred, obj, inf, ctx))
			return emptyRepositoryResult();

		flush();
		StatementClient statements = client.statements();
		GraphQueryResult result = statements.get(subj, pred, obj, inf, ctx);
		return createRepositoryResult(result);
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RDFHandlerException, StoreException
	{
		flush();
		client.statements().get(subj, pred, obj, includeInferred, handler, contexts);
	}

	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (!modified)
			return getRepository().size(subj, pred, obj, includeInferred, contexts);
		flush();
		return client.size().get(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		if (!modified)
			return getRepository().hasStatement(subj, pred, obj, includeInferred, contexts);
		flush();
		StatementClient statements = client.statements();
		statements.setLimit(1);
		GraphQueryResult result = statements.get(subj, pred, obj, includeInferred, contexts);
		try {
			return result.hasNext();
		}
		finally {
			result.close();
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit)
		throws StoreException
	{
		boolean currently = super.isAutoCommit();
		if (!autoCommit && currently) {
			client.begin();
		}
		else if (autoCommit && !currently) {
			client.commit();
			modified = false;
		}
		super.setAutoCommit(autoCommit);
	}

	public void commit()
		throws StoreException
	{
		flush();
		client.commit();
		getRepository().modified();
		modified = false;
		if (!isAutoCommit()) {
			client.begin();
		}
	}

	public void rollback()
		throws StoreException
	{
		synchronized (txn) {
			txn.clear();
		}
		client.rollback();
		modified = false;
		client.begin();
	}

	@Override
	public void close()
		throws StoreException
	{
		if (!closed) {
			closed = true;
			if (modified) {
				logger.warn("Rolling back transaction due to connection close", new Throwable());
				rollback();
			}
			client.close();
		}
		super.close();
	}

	@Override
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		// Send bytes directly to the server
		modified = true;
		flush();
		StatementClient httpClient = client.statements();
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
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		if (getRepository().isIllegal(subject, predicate, object, contexts))
			throw new IllegalStatementException();
		add(new AddStatementOperation(subject, predicate, object, contexts));
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		if (!noMatch(subject, predicate, object, true, contexts)) {
			add(new RemoveStatementsOperation(subject, predicate, object, contexts));
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws StoreException
	{
		add(new ClearOperation(contexts));
		autoCommit();
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		add(new RemoveNamespaceOperation(prefix));
		autoCommit();
	}

	public void clearNamespaces()
		throws StoreException
	{
		add(new ClearNamespacesOperation());
		autoCommit();
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		add(new SetNamespaceOperation(prefix, name));
		autoCommit();
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws StoreException
	{
		flush();
		List<Namespace> namespaceList = new ArrayList<Namespace>();

		TupleQueryResult namespaces = client.namespaces().list();
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

	public String getNamespace(String prefix)
		throws StoreException
	{
		flush();
		return client.namespaces().get(prefix);
	}

	@Override
	public String toString() {
		return getRepository().toString() + " Connection";
	}

	/**
	 * Creates a RepositoryResult for the supplied element set.
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(Iterable<? extends E> elements) {
		return new RepositoryResult<E>(new IteratorCursor<E>(elements.iterator()));
	}

	/**
	 * Creates a RepositoryResult for the supplied element set.
	 */
	protected RepositoryResult<Statement> createRepositoryResult(GraphQueryResult result) {
		return new RepositoryResult<Statement>(new GraphQueryResultCursor(result));
	}

	protected RepositoryClient getClient()
		throws StoreException
	{
		flush();
		return client;
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern (or super set) has no matches.
	 */
	private boolean noMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (modified)
			return false;
		return getRepository().noMatch(subj, pred, obj, includeInferred, contexts);
	}

	private void add(TransactionOperation operation)
		throws StoreException
	{
		modified = true;
		synchronized (txn) {
			txn.add(operation);
			if (txn.size() >= MAX_TRAN_QUEUE) {
				flush();
			}
		}
	}

	private void flush()
		throws StoreException
	{
		synchronized (txn) {
			if (txn.size() > 0) {
				client.statements().post(txn);
				txn.clear();
			}
		}
	}
}
