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

import org.openrdf.cursor.EmptyCursor;
import org.openrdf.cursor.IteratorCursor;
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
import org.openrdf.model.Literal;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.model.impl.NamespaceImpl;
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
import org.openrdf.repository.http.helpers.GraphQueryResultCursor;
import org.openrdf.repository.http.helpers.HTTPBNodeFactory;
import org.openrdf.repository.http.helpers.TaggingBNodeFactory;
import org.openrdf.repository.util.ModelNamespaceResult;
import org.openrdf.result.ContextResult;
import org.openrdf.result.GraphResult;
import org.openrdf.result.ModelResult;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.ContextResultImpl;
import org.openrdf.result.impl.ModelResultImpl;
import org.openrdf.result.impl.NamespaceResultImpl;
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

	private TaggingBNodeFactory bf;

	private ValueFactory vf;

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
		this.bf = new TaggingBNodeFactory(new BNodeFactoryImpl());
		URIFactory uf = repository.getURIFactory();
		LiteralFactory lf = repository.getLiteralFactory();
		// TaggingBNodeFactory for received BNodes
		client.setValueFactory(new ValueFactoryImpl(bf, uf, lf));
		// HTTPBNodeFactory for newly created BNodes
		this.vf = new ValueFactoryImpl(new HTTPBNodeFactory(client.bnodes()), uf, lf);

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

	public ValueFactory getValueFactory() {
		return vf;
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

	public ContextResult getContextIDs()
		throws StoreException
	{
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

		return new ContextResultImpl(new IteratorCursor<Resource>(contextList.iterator()));
	}

	public ModelResult match(Resource subj, URI pred, Value obj, boolean inf, Resource... ctx)
		throws StoreException
	{
		if (noMatch(subj, pred, obj, inf, ctx))
			return new ModelNamespaceResult(this, new EmptyCursor<Statement>());

		flush();
		StatementClient statements = client.statements();
		GraphResult result = statements.get(subj, pred, obj, inf, ctx);
		return new ModelResultImpl(new GraphQueryResultCursor(result));
	}

	public void exportMatch(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws RDFHandlerException, StoreException
	{
		flush();
		client.statements().get(subj, pred, obj, includeInferred, handler, contexts);
	}

	public long sizeMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (cachable(subj, pred, obj, contexts))
			return getRepository().size(subj, pred, obj, includeInferred, contexts);
		flush();
		return client.size().get(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public boolean hasMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (cachable(subj, pred, obj, contexts))
			return getRepository().hasStatement(subj, pred, obj, includeInferred, contexts);
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
		modified = !isAutoCommit();
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

	public NamespaceResult getNamespaces()
		throws StoreException
	{
		flush();
		List<Namespace> namespaceList = new ArrayList<Namespace>();

		TupleResult namespaces = client.namespaces().list();
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

		return new NamespaceResultImpl(new IteratorCursor<Namespace>(namespaceList.iterator()));
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
	private boolean cachable(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (modified)
			return false;
		if (subj instanceof BNode)
			return false;
		if (obj instanceof BNode)
			return false;
		if (contexts == null)
			return true;
		for (Resource ctx : contexts) {
			if (ctx instanceof BNode)
				return false;
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
		if (cachable(subj, pred, obj, contexts))
			return getRepository().noMatch(subj, pred, obj, includeInferred, contexts);
		if (isTaggedByAnotherConnection(subj, obj, contexts))
			return true;
		return false;
	}

	/**
	 * If any of the values are BNodes from a different HTTPRepositoryConnection.
	 */
	private boolean isTaggedByAnotherConnection(Resource subj, Value obj, Resource... contexts) {
		if (subj instanceof BNode && bf.isAlreadyTagged((BNode)subj))
			return true;
		if (obj instanceof BNode && bf.isAlreadyTagged((BNode)obj))
			return true;
		if (contexts == null)
			return false;
		for (Resource ctx : contexts) {
			if (ctx instanceof BNode && bf.isAlreadyTagged((BNode)ctx))
				return true;
		}
		return false;
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
