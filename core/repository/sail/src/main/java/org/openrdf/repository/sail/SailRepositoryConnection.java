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
package org.openrdf.repository.sail;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevel;
import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryConnectionOptimizations;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryReadOnlyException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.UnknownTransactionStateException;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.AdvancedSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionOptimizations;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;

/**
 * An implementation of the {@link RepositoryConnection} interface that wraps a
 * {@link SailConnection}.
 * 
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public class SailRepositoryConnection extends RepositoryConnectionBase implements RepositoryConnectionOptimizations {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The Sail connection wrapped by this repository connection object.
	 */
	private final SailConnection sailConnection;

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

	@Override
	public void begin()
		throws RepositoryException
	{
		try {
			sailConnection.begin(getIsolationLevel());
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void begin(IsolationLevel level)
		throws RepositoryException
	{
		try {
			sailConnection.begin(level);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void commit()
		throws RepositoryException
	{
		try {
			sailConnection.flush();
			sailConnection.prepare();
			sailConnection.commit();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void rollback()
		throws RepositoryException
	{
		try {
			sailConnection.rollback();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void close()
		throws RepositoryException
	{
		try {
			sailConnection.close();
			super.close();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public boolean isOpen()
		throws RepositoryException
	{
		try {
			return sailConnection.isOpen();
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public SailQuery prepareQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedQuery parsedQuery = QueryParserUtil.parseQuery(ql, queryString, baseURI);

		if (parsedQuery instanceof ParsedTupleQuery) {
			return new SailTupleQuery((ParsedTupleQuery)parsedQuery, this);
		}
		else if (parsedQuery instanceof ParsedGraphQuery) {
			return new SailGraphQuery((ParsedGraphQuery)parsedQuery, this);
		}
		else if (parsedQuery instanceof ParsedBooleanQuery) {
			return new SailBooleanQuery((ParsedBooleanQuery)parsedQuery, this);
		}
		else {
			throw new RuntimeException("Unexpected query type: " + parsedQuery.getClass());
		}
	}

	@Override
	public SailTupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedTupleQuery parsedQuery = QueryParserUtil.parseTupleQuery(ql, queryString, baseURI);
		return new SailTupleQuery(parsedQuery, this);
	}

	@Override
	public SailGraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedGraphQuery parsedQuery = QueryParserUtil.parseGraphQuery(ql, queryString, baseURI);
		return new SailGraphQuery(parsedQuery, this);
	}

	@Override
	public SailBooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString, String baseURI)
		throws MalformedQueryException
	{
		ParsedBooleanQuery parsedQuery = QueryParserUtil.parseBooleanQuery(ql, queryString, baseURI);
		return new SailBooleanQuery(parsedQuery, this);
	}

	@Override
	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI)
		throws RepositoryException, MalformedQueryException
	{
		ParsedUpdate parsedUpdate = QueryParserUtil.parseUpdate(ql, update, baseURI);

		return new SailUpdate(parsedUpdate, this);
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws RepositoryException
	{
		if(sailConnection instanceof AdvancedSailConnection) {
			try {
				return ((AdvancedSailConnection)sailConnection).hasStatement(subj, pred, obj, includeInferred, contexts);
			}
			catch(SailException e) {
				throw new RepositoryException("Unable to find statement in Sail", e);
			}
		}
		else {
			return super.hasStatement(subj, pred, obj, includeInferred, contexts);
		}
	}

	@Override
	public RepositoryResult<Resource> getContextIDs()
		throws RepositoryException
	{
		try {
			return createRepositoryResult(sailConnection.getContextIDs());
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get context IDs from Sail", e);
		}
	}

	@Override
	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			return createRepositoryResult(sailConnection.getStatements(subj, pred, obj, includeInferred,
					contexts));
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get statements from Sail", e);
		}
	}

	@Override
	public boolean isEmpty()
		throws RepositoryException
	{
		// The following is more efficient than "size() == 0" for Sails
		return !hasStatement(null, null, null, false);
	}

	@Override
	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		handler.startRDF();

		// Export namespace information
		CloseableIteration<? extends Namespace, RepositoryException> nsIter = getNamespaces();
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
		CloseableIteration<? extends Statement, RepositoryException> stIter = getStatements(subj, pred, obj,
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

	@Override
	public long size(Resource... contexts)
		throws RepositoryException
	{
		try {
			return sailConnection.size(contexts);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		try {
			sailConnection.addStatement(subject, predicate, object, contexts);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		try {
			sailConnection.removeStatements(subject, predicate, object, contexts);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			boolean local = startLocalTransaction();
			sailConnection.clear(contexts);
			conditionalCommit(local);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		try {
			boolean local = startLocalTransaction();
			sailConnection.setNamespace(prefix, name);
			conditionalCommit(local);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		try {
			boolean local = startLocalTransaction();
			sailConnection.removeNamespace(prefix);
			conditionalCommit(local);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void clearNamespaces()
		throws RepositoryException
	{
		try {
			boolean local = startLocalTransaction();
			sailConnection.clearNamespaces();
			conditionalCommit(local);
		}
		catch (SailReadOnlyException e) {
			throw new RepositoryReadOnlyException(e.getMessage(), e);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		try {
			return createRepositoryResult(sailConnection.getNamespaces());
		}
		catch (SailException e) {
			throw new RepositoryException("Unable to get namespaces from Sail", e);
		}
	}

	@Override
	public String getNamespace(String prefix)
		throws RepositoryException
	{
		try {
			return sailConnection.getNamespace(prefix);
		}
		catch (SailException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Wraps a CloseableIteration coming from a Sail in a RepositoryResult
	 * object, applying the required conversions
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(
			CloseableIteration<? extends E, SailException> sailIter)
	{
		return new RepositoryResult<E>(new SailCloseableIteration<E>(sailIter));
	}

	@Override
	public boolean isActive()
		throws UnknownTransactionStateException
	{
		try {
			return sailConnection.isActive();
		}
		catch (SailException e) {
			throw new UnknownTransactionStateException(e);
		}
	}

	public boolean isHasStatementOptimized()
	{
		return (sailConnection instanceof SailConnectionOptimizations) ? ((SailConnectionOptimizations)sailConnection).isHasStatementOptimized() : true;
	}

	@Override
	public String toString()
	{
		return getSailConnection().toString();
	}
}
