/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.http;

import static org.openrdf.protocol.rest.Protocol.*;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.protocol.rest.Protocol;
import org.openrdf.protocol.rest.ProtocolUtil;
import org.openrdf.protocol.transaction.TransactionSerializationException;
import org.openrdf.protocol.transaction.TransactionWriter;
import org.openrdf.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.protocol.transaction.operations.ClearContextOperation;
import org.openrdf.protocol.transaction.operations.ClearRepositoryOperation;
import org.openrdf.protocol.transaction.operations.OperationList;
import org.openrdf.protocol.transaction.operations.RemoveStatementOperation;
import org.openrdf.protocol.transaction.operations.TransactionOperation;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultParseException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInternalException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NamespaceImpl;
import org.openrdf.util.iterator.CloseableIterator;

/**
 * HTTPSailConnection. The Connection gathers operations to be commited to the
 * server in one HTTP POST method. On commit(), a lock is aquired and other
 * operations on the transaction will be blocked. calling rollback() or commit()
 * during a commit will also block.
 */
public class HTTPSailConnection implements SailConnection {

	/**
	 * an operation list that is synchronised for add()
	 */
	public static class SynchronisedAddOperationList extends OperationList {

		@Override
		public synchronized boolean add(TransactionOperation o)
		{
			return super.add(o);
		}
	}

	static Logger log = Logger.getLogger(HTTPSail.class.getName());

	/**
	 * this event is fired after commit
	 */
	DefaultSailChangedEvent _eventOnCommit = null;

	private HTTPSail _sail;

	private String _repositoryLocation;

	private boolean _active;

	private List<SailConnectionListener> _listeners;

	// /**
	// * lock to synchronise writes to the transaction and the final commit.
	// * Writes to the transaction use the "read" lock of this lock,
	// * commit or rollback use the "write" lock.
	// */
	// private ReentrantReadWriteLock _lock;

	/**
	 * store the order of operations (add, remove, clear)
	 */
	private SynchronisedAddOperationList _operations;

	/**
	 * 
	 */
	public HTTPSailConnection(HTTPSail httpSail) {
		_sail = httpSail;
		_repositoryLocation = Protocol.getRepositoryLocation(_sail.getServerUrl(), _sail.getRepositoryId());
		_active = true;
		_listeners = new ArrayList<SailConnectionListener>();
		_operations = new SynchronisedAddOperationList();
		_eventOnCommit = new DefaultSailChangedEvent(_sail);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.sesame.sail.Transaction#addStatement(org.openrdf.model.Resource,
	 *      org.openrdf.model.URI, org.openrdf.model.Value,
	 *      org.openrdf.model.Resource)
	 */
	public void addStatement(Resource subj, URI pred, Value obj, Resource ctx)
		throws SailException
	{
		checkActive();
		ContextStatementImpl stm = new ContextStatementImpl(subj, pred, obj, ctx);
		_operations.add(new AddStatementOperation(stm, ctx));
		_fireStatementAdded(stm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.sesame.sail.Transaction#addTransactionListener(org.openrdf.sesame.sail.TransactionListener)
	 */
	public void addConnectionListener(SailConnectionListener listener) {
		_listeners.add(listener);
	}

	/**
	 * persisting all changes in this transaction construct a Trix file with the
	 * operations stored in the Operation list. inform the SailChangedListeners.
	 * 
	 * @see org.openrdf.sesame.sail.SailConnection#commit()
	 */
	public void commit()
		throws SailException
	{
		checkActive();

		if (_operations == null || _operations.size() == 0) {
			// nothing to commit
			return;
		}
		try {
			// Get URL Connection and Socket
			// URL url = new URL(_sail.getRepositoryURI());
			// Socket socket = new Socket(url.getHost(), url.getPort());
			// socket.setSoTimeout(BestProperties.getIntProperty("vmu.url.timeout"));
			// // building request in StringBuffer
			// StringBuffer sbRequest = new StringBuffer();
			// sbRequest.setLength(0);
			// sbRequest.append("POST" + " " + url.getFile() + " HTTP/1.0\r");
			// sbRequest.append("Content-Type: text/xml\r");
			// sbRequest.append("User-Agent: Java 1.4.0\n");
			// sbRequest.append("Host: " + url.getHost() + "\n");
			// sbRequest.append("Accept: text/html,text/xml,text/plain, */*\n");
			// sbRequest.append("Cache-Control: no-cache\n");
			// sbRequest.append("Connection: Close\n");
			// sbRequest.append("Content-Length: " + stream.length() + "\n");
			// sbRequest.append("\n" + stream + "\n");
			//
			// // sending the request
			// request = new PrintStream(new
			// BufferedOutputStream(socket.getOutputStream()));
			// request.println(sbRequest.toString());
			// request.flush();
			// // getting response from client
			// response = new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));

			HttpClient client = new HttpClient();
			// construct post method
			PostMethod post = new PostMethod(_repositoryLocation);
			// set headers
			post.setRequestHeader("Content-Type", "application/x-rdftransaction");
			// set content

			// serialize
			CharArrayWriter serwriter = new CharArrayWriter();
			serializeTransaction(serwriter);
			StringRequestEntity uselessabstractions = new StringRequestEntity(serwriter.toString());
			post.setRequestEntity(uselessabstractions);

			// send method
			int http_code = client.executeMethod(post);
			// http code with 2xx is fine. Should be 200
			if (http_code != HttpURLConnection.HTTP_OK && http_code != HttpURLConnection.HTTP_NO_CONTENT) {
				throw new Exception("An error occured by sending the post method " + "(url: "
						+ _repositoryLocation + "' code: " + http_code + ")");
			}
			// inform all listeners that the sail has changed
			_informSailChangedListeners();
		}
		catch (Exception x) {
			log.log(Level.WARNING, "Cannot execute commit: " + x, x);
			throw new SailException(x);
		}
		finally {
			_operations.clear();
		}
	}

	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred)
	{
		String location = Protocol.getRepositoryLocation(_sail.getServerUrl(), _sail.getRepositoryId());
		location = appendQueryParameters(subj, pred, obj, includeInferred, location, false);
		return getStatements(location);
	}

	public CloseableIterator<? extends Statement> getNullContextStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred)
	{
		String location = Protocol.getContextLocation(_sail.getServerUrl(), _sail.getRepositoryId(), null);
		location = appendQueryParameters(subj, pred, obj, includeInferred, location, true);
		return getStatements(location);
	}

	public CloseableIterator<? extends Statement> getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context, boolean includeInferred)
	{
		String location = Protocol.getContextLocation(_sail.getServerUrl(), _sail.getRepositoryId(), context);
		location = appendQueryParameters(subj, pred, obj, includeInferred, location, true);
		return getStatements(location);
	}

	private CloseableIterator<? extends Statement> getStatements(String url) {
		ListIteratorWrapper<Statement> iterator = null;
		try {
			HttpClient client = new HttpClient();
			GetMethod get = new GetMethod(url);
			get.getParams().setParameter("Accept", "application/rdf+xml");

			int http_code = client.executeMethod(get);
			if (http_code != HttpURLConnection.HTTP_OK) {
				String msg = "HTTP Method did not return OK for url '" + _repositoryLocation + "' code: "
						+ http_code;
				log.log(Level.WARNING, msg);
				// TODO: throw a more specific exception
				throw new Exception(msg);
			}
			InputStream result = get.getResponseBodyAsStream();
			StatementCollector handler = new StatementCollector();
			RDFParser p = Rio.createParser(RDFFormat.RDFXML);
			p.setRDFHandler(handler);
			p.parse(result, _repositoryLocation);
			result.close();
			get.releaseConnection();
			iterator = new ListIteratorWrapper<Statement>(handler.getStatements().iterator());
		}
		catch (Exception e) {
			log.log(Level.WARNING, "Statements couldn't be extracted: " + e, e);
		}
		return iterator;
	}

	private String appendQueryParameters(Resource subj, URI pred, Value obj, boolean includeInferred,
			String baseLocation, boolean hasParameters)
	{
		StringBuilder result = new StringBuilder(baseLocation);

		if (subj != null) {
			appendParameterSeparator(result, hasParameters);
			result.append(SUBJECT_PARAM_NAME);
			result.append('=');
			result.append(ProtocolUtil.encodeParameterValue(subj));
			hasParameters = true;
		}

		if (pred != null) {
			appendParameterSeparator(result, hasParameters);
			result.append(PREDICATE_PARAM_NAME);
			result.append('=');
			result.append(ProtocolUtil.encodeParameterValue(pred));
			hasParameters = true;
		}

		if (obj != null) {
			appendParameterSeparator(result, hasParameters);
			result.append(OBJECT_PARAM_NAME);
			result.append('=');
			result.append(ProtocolUtil.encodeParameterValue(obj));
			hasParameters = true;
		}

		appendParameterSeparator(result, hasParameters);
		result.append(INCLUDE_INFERRED_PARAM_NAME);
		result.append('=');
		result.append(includeInferred);

		return result.toString();
	}

	private void appendParameterSeparator(StringBuilder builder, boolean hasParameters) {
		if (hasParameters) {
			builder.append('&');
		}
		else {
			builder.append('?');
		}
	}

	public CloseableIterator<? extends Resource> getContextIDs() {
		ListIteratorWrapper<URI> iterator = null;
		// construct namespace url for current repository
		String contextsLocation = Protocol.getContextsLocation(_sail.getServerUrl(), _sail.getRepositoryId());
		try {
			GetMethod get = new GetMethod(contextsLocation);
			TupleQueryResult result = _sail._getSparqlQueryResult(get);
			if (result != null) {
				// Vector of namespace/prefix-tuples
				Vector<URI> tuples = new Vector<URI>();
				// parse the result
				Iterator<Solution> solutionIter = result.iterator();
				// iterate repository list
				while (solutionIter.hasNext()) {
					Solution solution = solutionIter.next();
					tuples.add((URI)solution.getBinding("contextID").getValue());
				}
				iterator = new ListIteratorWrapper<URI>(tuples.iterator());
			}
		}
		catch (Exception e) {
			log.log(Level.WARNING, "Context ids couldn't be retrieved", e);
		}
		return iterator;
	}

	public CloseableIterator<? extends Namespace> getNamespaces() {
		// construct namespace url for current repository
		String namespacesLocation = Protocol.getNamespacesLocation(_sail.getServerUrl(),
				_sail.getRepositoryId());
		try {
			GetMethod get = new GetMethod(namespacesLocation);
			TupleQueryResult result = _sail._getSparqlQueryResult(get);
			if (result != null) {
				// Vector of namespace/prefix-tuples
				Vector<Namespace> tuples = new Vector<Namespace>();
				// parse the result
				Iterator<Solution> solutionIter = result.iterator();
				// iterate repository list
				while (solutionIter.hasNext()) {
					Solution solution = solutionIter.next();
					String prefix = solution.getBinding("prefix").toString();
					String ns = solution.getBinding("namespace").toString();
					tuples.add(new NamespaceImpl(prefix, ns));
				}
				return new ListIteratorWrapper<Namespace>(tuples.iterator());
			}
		}
		catch (Exception e) {
			log.log(Level.WARNING, "Namespaces couldn't be retrieved " + e, e);
		}
		// TODO: return null or empty list?
		return null;
	}

	public TupleQueryResult evaluate(TupleQuery query, boolean includeInferred)
		throws SailInternalException
	{
		String queryString = query.getQueryString();
		try {
			return _evaluateTupleQueryString(query.getQueryLanguage(), queryString, includeInferred);
		}
		catch (Exception e) {
			throw new SailInternalException("Couldn't evaluate query " + queryString + ": " + e, e);
		}
	}

	public GraphQueryResult evaluate(GraphQuery query, boolean includeInferred)
		throws SailInternalException
	{
		String queryString = query.getQueryString();
		try {
			return _evaluateGraphQueryString(query.getQueryLanguage(), queryString, includeInferred);
		}
		catch (Exception e) {
			throw new SailInternalException("Couldn't evaluate query " + queryString + ": " + e, e);
		}
	}

	private TupleQueryResult _evaluateTupleQueryString(QueryLanguage lang, String query,
			boolean includeInferred)
		throws HttpException, IOException, TupleQueryResultParseException, TupleQueryResultHandlerException
	{
		// query current repository
		GetMethod get = _getGetMethod(_repositoryLocation);
		// add query string (the actual query and the language)
		get.setQueryString(new NameValuePair[] {
				new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, lang.toString()),
				new NameValuePair(Protocol.QUERY_PARAM_NAME, query),
				new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, String.valueOf(includeInferred)) });
		return _sail._getSparqlQueryResult(get);
	}

	private GraphQueryResult _evaluateGraphQueryString(QueryLanguage lang, String query,
			boolean includeInferred)
		throws HttpException, IOException, TupleQueryResultParseException, TupleQueryResultHandlerException
	{

		// query current repository
		GetMethod get = new GetMethod(_repositoryLocation);
		get.setRequestHeader("Accept", RDFFormat.TURTLE.getMIMEType());

		// add query string (the actual query and the language)
		get.setQueryString(new NameValuePair[] {
				new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, lang.toString()),
				new NameValuePair(Protocol.QUERY_PARAM_NAME, query),
				new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, String.valueOf(includeInferred)) });
		return _sail._getGraphQueryResult(get, RDFFormat.TURTLE);
	}

	/**
	 * serialize the current operations of the transaction to a string, for
	 * committing to the server or testing
	 * 
	 * @return the already issued triples as xml string
	 */
	protected void serializeTransaction(Writer writer)
		throws TransactionSerializationException
	{
		TransactionWriter taWriter = new TransactionWriter();
		taWriter.serialize(_operations, writer);
	}

	public boolean isOpen() {
		return _active;
	}

	public void removeStatement(Resource subj, URI pred, Value obj, Resource ctx)
		throws SailException
	{
		checkActive();
		ContextStatementImpl stm = new ContextStatementImpl(subj, pred, obj, ctx);
		_operations.add(new RemoveStatementOperation(stm, ctx));
		_fireStatementRemoved(stm);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		_listeners.remove(listener);
	}

	public void rollback()
		throws SailException
	{
		checkActive();
		// remove all operations from the list
		_operations.clear();
		_eventOnCommit = new DefaultSailChangedEvent(_sail);
	}

	public void clear()
		throws SailException
	{
		checkActive();
		_operations.add(new ClearRepositoryOperation());
		_eventOnCommit.setStatementsRemoved(true);
	}

	protected void checkActive()
		throws SailException
	{
		if (!_active)
			throw new SailException("Transaction is not active!");
	}

	public void clearContext(Resource context)
		throws SailException
	{
		checkActive();
		_operations.add(new ClearContextOperation(context));
		_eventOnCommit.setStatementsRemoved(true);
	}

	public void setNamespace(String prefix, String name)
		throws SailException
	{
		checkActive();

	}

	public void removeNamespace(String prefix)
		throws SailException
	{
		checkActive();

	}

	// *************************************************************************
	// 
	// private helper methods
	// 
	// *************************************************************************

	private void _fireStatementAdded(Statement stm) {
		_eventOnCommit.setStatementsAdded(true);
		for (SailConnectionListener listener : _listeners) {
			listener.statementAdded(stm);
		}
	}

	private void _fireStatementRemoved(Statement stm) {
		_eventOnCommit.setStatementsRemoved(true);
		for (SailConnectionListener listener : _listeners) {
			listener.statementRemoved(stm);
		}
	}

	/**
	 * fire at least one and at most three events (add, remove, clear)
	 */
	private void _informSailChangedListeners() {
		if (_eventOnCommit != null) {
			if (_eventOnCommit.statementsAdded() || _eventOnCommit.statementsRemoved())
				_sail.fireSailChangedEvent(_eventOnCommit);
			_eventOnCommit = new DefaultSailChangedEvent(_sail);
		}
	}

	private GetMethod _getGetMethod(String url) {
		GetMethod get = new GetMethod(url);
		// result is a sparql-document
		get.setRequestHeader("Accept", "application/sparql-results+xml");
		return get;
	}

	public void close()
		throws SailException
	{
		rollback();
		_active = false;
	}
}
