/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.OpenRDFException;
import org.openrdf.http.client.SesameSession;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.Protocol.Action;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.UnknownTransactionStateException;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
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

	// private List<TransactionOperation> txn = Collections.synchronizedList(new
	// ArrayList<TransactionOperation>());

	private final SesameSession client;

	private boolean active;

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private Throwable creatorTrace;

	private Model toAdd;

	private Model toRemove;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPRepositoryConnection(HTTPRepository repository, SesameSession client) {
		super(repository);

		this.client = client;

		// parser used for locally processing input data to be sent to the server
		// should be strict, and should preserve bnode ids.
		setParserConfig(new ParserConfig());
		getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

		if (debugEnabled()) {
			creatorTrace = new Throwable();
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setParserConfig(ParserConfig parserConfig) {
		super.setParserConfig(parserConfig);
	}

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

	public void begin()
		throws RepositoryException
	{
		verifyIsOpen();
		verifyNotTxnActive("Connection already has an active transaction");
		try {
			client.beginTransaction(this.getIsolationLevel());
			active = true;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Prepares a {@Link Query} for evaluation on this repository. Note
	 * that the preferred way of preparing queries is to use the more specific
	 * {@link #prepareTupleQuery(QueryLanguage, String, String)},
	 * {@link #prepareBooleanQuery(QueryLanguage, String, String)}, or
	 * {@link #prepareGraphQuery(QueryLanguage, String, String)} methods instead.
	 * 
	 * @throws UnsupportedOperationException
	 *         if the method is not supported for the supplied query language.
	 */
	public Query prepareQuery(QueryLanguage ql, String queryString, String baseURI) {
		if (QueryLanguage.SPARQL.equals(ql)) {
			String strippedQuery = QueryParserUtil.removeSPARQLQueryProlog(queryString).toUpperCase();
			if (strippedQuery.startsWith("SELECT")) {
				return prepareTupleQuery(ql, queryString, baseURI);
			}
			else if (strippedQuery.startsWith("ASK")) {
				return prepareBooleanQuery(ql, queryString, baseURI);
			}
			else {
				return prepareGraphQuery(ql, queryString, baseURI);
			}
		}
		else if (QueryLanguage.SERQL.equals(ql)) {
			String strippedQuery = queryString;

			// remove all opening brackets
			strippedQuery = strippedQuery.replace('(', ' ');
			strippedQuery = strippedQuery.trim();

			if (strippedQuery.toUpperCase().startsWith("SELECT")) {
				return prepareTupleQuery(ql, queryString, baseURI);
			}
			else {
				return prepareGraphQuery(ql, queryString, baseURI);
			}
		}
		else {
			throw new UnsupportedOperationException("Operation not supported for query language " + ql);
		}
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

			TupleQueryResult contextIDs = client.getContextIDs();
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
		flushTransactionState(Action.GET);
		try {
			client.getStatements(subj, pred, obj, includeInferred, handler, contexts);
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
		// TODO fix transaction state flush for size request
		try {
			return client.size(contexts);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public void commit()
		throws RepositoryException
	{
		flushTransactionState(Action.COMMIT);
		try {
			client.commitTransaction();
			active = false;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public void rollback()
		throws RepositoryException
	{
		flushTransactionState(Action.ROLLBACK);
		try {
			client.rollbackTransaction();
			active = false;
		}
		catch (OpenRDFException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void close()
		throws RepositoryException
	{
		if (isActive()) {
			logger.warn("Rolling back transaction due to connection close", new Throwable());
			rollback();
		}

		super.close();
	}

	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		if (baseURI == null) {
			// default baseURI to file
			baseURI = file.toURI().toString();
		}
		if (dataFormat == null) {
			dataFormat = Rio.getParserFormatForFileName(file.getName());
		}

		InputStream in = new FileInputStream(file);
		try {
			add(in, baseURI, dataFormat, contexts);
		}
		finally {
			in.close();
		}
	}

	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		URLConnection con = url.openConnection();

		// Set appropriate Accept headers
		if (dataFormat != null) {
			for (String mimeType : dataFormat.getMIMETypes()) {
				con.addRequestProperty("Accept", mimeType);
			}
		}
		else {
			Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
			List<String> acceptParams = RDFFormat.getAcceptParams(rdfFormats, true, null);
			for (String acceptParam : acceptParams) {
				con.addRequestProperty("Accept", acceptParam);
			}
		}

		InputStream in = con.getInputStream();

		if (dataFormat == null) {
			// Try to determine the data's MIME type
			String mimeType = con.getContentType();
			int semiColonIdx = mimeType.indexOf(';');
			if (semiColonIdx >= 0) {
				mimeType = mimeType.substring(0, semiColonIdx);
			}
			dataFormat = Rio.getParserFormatForMIMEType(mimeType);

			// Fall back to using file name extensions
			if (dataFormat == null) {
				dataFormat = Rio.getParserFormatForFileName(url.getPath());
			}
		}

		try {
			add(in, baseURI, dataFormat, contexts);
		}
		finally {
			in.close();
		}
	}

	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		flushTransactionState(Action.ADD);
		// Send bytes directly to the server
		client.upload(in, baseURI, dataFormat, false, contexts);
	}

	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		flushTransactionState(Action.ADD);
		client.upload(reader, baseURI, dataFormat, false, contexts);
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		flushTransactionState(Protocol.Action.ADD);

		if (toAdd == null) {
			toAdd = new LinkedHashModel();
		}
		toAdd.add(subject, predicate, object, contexts);
	}

	private void addModel(Model m)
		throws RepositoryException
	{
		// TODO we should dynamically pick a format from the available writers perhaps?
		RDFFormat format = RDFFormat.BINARY;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Rio.write(m, out, format);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			client.addData(in, null, format);

		}
		catch (RDFHandlerException e) {
			throw new RepositoryException("error while writing statement", e);
		}
		catch (RDFParseException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	private void removeModel(Model m)
		throws RepositoryException
	{
		RDFFormat format = RDFFormat.BINARY;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Rio.write(m, out, format);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			client.removeData(in, null, format);

		}
		catch (RDFHandlerException e) {
			throw new RepositoryException("error while writing statement", e);
		}
		catch (RDFParseException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	protected void flushTransactionState(Action action)
		throws RepositoryException
	{
		if (isActive()) {
			switch (action) {
				case ADD:
					if (toRemove != null) {
						removeModel(toRemove);
						toRemove = null;
					}
					break;
				case DELETE:
					if (toAdd != null) {
						addModel(toAdd);
						toAdd = null;
					}
					break;
				case GET:
				case UPDATE:
				case COMMIT:
				case QUERY:
					if (toAdd != null) {
						addModel(toAdd);
						toAdd = null;
					}
					if (toRemove != null) {
						removeModel(toRemove);
						toRemove = null;
					}
					break;
				case ROLLBACK:
					toAdd = null;
					toRemove = null;
					break;

			}
		}
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		flushTransactionState(Protocol.Action.DELETE);

		if (toRemove == null) {
			toRemove = new LinkedHashModel();
		}
		if (subject == null) {
			subject = SESAME.WILDCARD;
		}
		if (predicate == null) {
			predicate = SESAME.WILDCARD;
		}
		if (object == null) {
			object = SESAME.WILDCARD;
		}
		toRemove.add(subject, predicate, object, contexts);
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		boolean localTransaction = startLocalTransaction();

		remove(null, null, null, contexts);

		conditionalCommit(localTransaction);
	}

	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}

		boolean localTransaction = startLocalTransaction();

		try {
			client.removeNamespacePrefix(prefix);
			conditionalCommit(localTransaction);
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw new RepositoryException(e);
		}

	}

	public void clearNamespaces()
		throws RepositoryException
	{
		boolean localTransaction = startLocalTransaction();
		try {
			client.clearNamespaces();
			conditionalCommit(localTransaction);
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw new RepositoryException(e);
		}
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

		boolean localTransaction = startLocalTransaction();
		try {
			client.setNamespacePrefix(prefix, name);
			conditionalCommit(localTransaction);
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		try {
			List<Namespace> namespaceList = new ArrayList<Namespace>();

			TupleQueryResult namespaces = client.getNamespaces();
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
			return client.getNamespace(prefix);
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

	/**
	 * Verifies that the connection is open, throws a {@link StoreException} if
	 * it isn't.
	 */
	protected void verifyIsOpen()
		throws RepositoryException
	{
		if (!isOpen()) {
			throw new RepositoryException("Connection has been closed");
		}
	}

	/**
	 * Verifies that the connection has an active transaction, throws a
	 * {@link StoreException} if it hasn't.
	 */
	protected void verifyTxnActive()
		throws RepositoryException
	{
		if (!isActive()) {
			throw new RepositoryException("Connection does not have an active transaction");
		}
	}

	/**
	 * Verifies that the connection does not have an active transaction, throws a
	 * {@link RepositoryException} if it has.
	 */
	protected void verifyNotTxnActive(String msg)
		throws RepositoryException
	{
		if (isActive()) {
			throw new RepositoryException(msg);
		}
	}

	public boolean isActive()
		throws UnknownTransactionStateException, RepositoryException
	{
		return active;
	}

	/**
	 * @return
	 */
	protected SesameSession getSesameSession() {
		return client;
	}
}
