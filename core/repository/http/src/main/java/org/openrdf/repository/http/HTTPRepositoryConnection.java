/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.cursor.CollectionCursor;
import org.openrdf.cursor.EmptyCursor;
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
import org.openrdf.model.BNode;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.repository.http.exceptions.IllegalStatementException;
import org.openrdf.repository.http.exceptions.RepositoryReadOnlyException;
import org.openrdf.repository.http.helpers.GraphQueryResultCursor;
import org.openrdf.repository.http.helpers.HTTPBNodeFactory;
import org.openrdf.repository.util.ModelNamespaceResult;
import org.openrdf.result.ContextResult;
import org.openrdf.result.GraphResult;
import org.openrdf.result.ModelResult;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.ContextResultImpl;
import org.openrdf.result.impl.ModelResultImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.Isolation;
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

	private final HTTPRepository repository;

	private final ConnectionClient client;

	private final ValueFactory vf;

	private final List<TransactionOperation> txn = new ArrayList<TransactionOperation>(MAX_TRAN_QUEUE / 2);

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private final Throwable creatorTrace;

	private volatile boolean isOpen = true;

	private volatile boolean autoCommit = true;

	/** If connection cannot use shared repository cache. */
	private volatile boolean modified = false;

	private volatile Isolation level = Isolation.READ_COMMITTED;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepositoryConnection(HTTPRepository repository, ConnectionClient client) {
		super(repository);
		this.repository = repository;
		this.client = client;

		URIFactory uf = repository.getURIFactory();
		LiteralFactory lf = repository.getLiteralFactory();
		HTTPBNodeFactory bf = new HTTPBNodeFactory(client.bnodes());
		this.vf = new ValueFactoryImpl(bf, uf, lf);

		this.creatorTrace = debugEnabled() ? new Throwable() : null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueFactory getValueFactory() {
		return vf;
	}

	/**
	 * Verifies that the connection is open, throws a {@link StoreException} if
	 * it isn't.
	 */
	protected void verifyIsOpen()
		throws StoreException
	{
		if (!isOpen()) {
			throw new StoreException("Connection has been closed");
		}
	}

	/**
	 * Verifies that the connection is not in read-only mode, throws a
	 * {@link StoreException} if it is.
	 */
	protected void verifyNotReadOnly()
		throws StoreException
	{
		// if (isReadOnly()) {
		// throw new StoreException("Connection is in read-only mode");
		// }
	}

	/**
	 * Verifies that the connection has an active transaction, throws a
	 * {@link StoreException} if it hasn't.
	 */
	protected void verifyTxnActive()
		throws StoreException
	{
		if (isAutoCommit()) {
			throw new StoreException("Connection does not have an active transaction");
		}
	}

	/**
	 * Verifies that the connection does not have an active transaction, throws a
	 * {@link StoreException} if the connection is it has.
	 */
	protected void verifyNotTxnActive(String msg)
		throws StoreException
	{
		if (!isAutoCommit()) {
			throw new StoreException(msg);
		}
	}

	public boolean isOpen()
		throws StoreException
	{
		return isOpen;
	}

	public void close()
		throws StoreException
	{
		if (isOpen()) {
			flush();
			isOpen = false;
			if (modified) {
				logger.warn("Rolling back transaction due to connection close", new Throwable());
				rollback();
			}
			client.close();
		}
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

	public Isolation getTransactionIsolation()
		throws StoreException
	{
		// TODO read from remote store
		return level;
	}

	public void setTransactionIsolation(Isolation level)
		throws StoreException
	{
		verifyNotTxnActive("transaction isolation level cannot be changed during a transaction");
		this.level = level;
	}

	public boolean isAutoCommit()
		throws StoreException
	{
		return autoCommit;
	}

	public void begin()
		throws StoreException
	{
		verifyIsOpen();
		verifyNotTxnActive("Connection already has an active transaction");

		client.begin();
		autoCommit = false;
	}

	public void commit()
		throws StoreException
	{
		verifyIsOpen();
		verifyTxnActive();

		flush();
		client.commit();
		repository.modified();
		client.begin();
		modified = false;
		autoCommit = true;
	}

	public void rollback()
		throws StoreException
	{
		verifyIsOpen();
		verifyTxnActive();

		synchronized (txn) {
			txn.clear();
		}
		client.rollback();
		client.begin();
		modified = false;
		autoCommit = true;
	}

	public Query prepareQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		verifyIsOpen();

		flush();
		QueryClient query = client.queries().postQuery(ql, qry, baseURI);
		if (query instanceof GraphQueryClient) {
			return new HTTPGraphQuery(qry, (GraphQueryClient)query);
		}
		if (query instanceof BooleanQueryClient) {
			return new HTTPBooleanQuery(qry, (BooleanQueryClient)query);
		}
		if (query instanceof TupleQueryClient) {
			return new HTTPTupleQuery(qry, (TupleQueryClient)query);
		}
		throw new StoreException("Unsupported query type: " + query);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		verifyIsOpen();

		flush();
		TupleQueryClient query = client.queries().postTupleQuery(ql, qry, baseURI);
		return new HTTPTupleQuery(qry, query);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		verifyIsOpen();

		flush();
		GraphQueryClient query = client.queries().postGraphQuery(ql, qry, baseURI);
		return new HTTPGraphQuery(qry, query);
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String qry, String baseURI)
		throws StoreException, MalformedQueryException
	{
		verifyIsOpen();

		flush();
		BooleanQueryClient query = client.queries().postBooleanQuery(ql, qry, baseURI);
		return new HTTPBooleanQuery(qry, query);
	}

	public ContextResult getContextIDs()
		throws StoreException
	{
		verifyIsOpen();

		flush();
		List<Resource> contextList = new ArrayList<Resource>();

		TupleResult contextIDs = client.contexts().list();
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

		return new ContextResultImpl(new CollectionCursor<Resource>(contextList));
	}

	public ModelResult match(Resource subj, URI pred, Value obj, boolean inf, Resource... ctx)
		throws StoreException
	{
		verifyIsOpen();

		if (noMatch(subj, pred, obj, inf, ctx)) {
			return new ModelNamespaceResult(this, new EmptyCursor<Statement>());
		}

		flush();
		StatementClient statements = client.statements();
		GraphResult result = statements.get(subj, pred, obj, inf, ctx);
		return new ModelResultImpl(new GraphQueryResultCursor(result));
	}

	public <H extends RDFHandler> H exportMatch(Resource subj, URI pred, Value obj, boolean includeInferred,
			H handler, Resource... contexts)
		throws RDFHandlerException, StoreException
	{
		verifyIsOpen();

		flush();
		client.statements().get(subj, pred, obj, includeInferred, handler, contexts);
		return handler;
	}

	public long sizeMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();

		if (cachable(subj, pred, obj, contexts)) {
			return repository.size(subj, pred, obj, includeInferred, contexts);
		}
		flush();
		return client.size().get(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public boolean hasMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();

		if (cachable(subj, pred, obj, contexts)) {
			return repository.hasStatement(subj, pred, obj, includeInferred, contexts);
		}
		flush();
		StatementClient statements = client.statements();
		statements.setLimit(1);
		GraphResult result = statements.get(subj, pred, obj, includeInferred, contexts);
		try {
			return result.hasNext();
		}
		finally {
			result.close();
		}
	}

	@Override
	protected void addInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			Resource... contexts)
		throws IOException, RDFParseException, StoreException
	{
		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		// Send bytes directly to the server
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
		if (isAutoCommit()) {
			repository.modified();
		}
		else {
			modified = true;
		}
	}

	public void add(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();

		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		if (repository.isIllegal(subject, predicate, object, contexts)) {
			throw new IllegalStatementException();
		}
		add(new AddStatementOperation(subject, predicate, object, contexts));
	}

	public void removeMatch(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();

		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		if (!noMatch(subject, predicate, object, true, contexts)) {
			add(new RemoveStatementsOperation(subject, predicate, object, contexts));
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();

		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		add(new ClearOperation(contexts));
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();

		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		add(new RemoveNamespaceOperation(prefix));
	}

	public void clearNamespaces()
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();

		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		add(new ClearNamespacesOperation());
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		verifyIsOpen();
		verifyNotReadOnly();

		if (repository.isReadOnly()) {
			throw new RepositoryReadOnlyException();
		}
		add(new SetNamespaceOperation(prefix, name));
	}

	public NamespaceResult getNamespaces()
		throws StoreException
	{
		verifyIsOpen();

		if (!modified && isAutoCommit()) {
			return repository.getNamespaces();
		}
		flush();
		return client.namespaces().list();
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		verifyIsOpen();

		if (!modified && isAutoCommit()) {
			return repository.getNamespace(prefix);
		}
		flush();
		return client.namespaces().get(prefix);
	}

	@Override
	public String toString() {
		return repository.toString() + " Connection";
	}

	protected RepositoryClient getClient()
		throws StoreException
	{
		flush();
		return client;
	}

	/**
	 * If this connection has not modified any statements and the pattern does
	 * not contains connection specific BNodes.
	 */
	private boolean cachable(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (modified || !isAutoCommit()) {
			return false;
		}
		if (subj instanceof BNode) {
			return false;
		}
		if (obj instanceof BNode) {
			return false;
		}
		if (contexts == null) {
			return true;
		}
		for (Resource ctx : contexts) {
			if (ctx instanceof BNode) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern (or super set) has no matches.
	 */
	private boolean noMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (cachable(subj, pred, obj, contexts)) {
			return repository.noMatch(subj, pred, obj, includeInferred, contexts);
		}
		return false;
	}

	private void add(TransactionOperation operation)
		throws StoreException
	{
		modified = true;
		synchronized (txn) {
			txn.add(operation);
			if (isAutoCommit() || txn.size() >= MAX_TRAN_QUEUE) {
				flush();
			}
		}
	}

	private void flush()
		throws StoreException
	{
		synchronized (txn) {
			if (!txn.isEmpty()) {
				client.statements().post(txn);
				txn.clear();
				if (isAutoCommit()) {
					repository.modified();
					modified = false;
				}
			}
		}
	}
}
