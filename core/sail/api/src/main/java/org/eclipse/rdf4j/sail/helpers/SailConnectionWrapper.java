/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.helpers;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
import org.eclipse.rdf4j.sail.UpdateContext;

/**
 * An implementation of the SailConnection interface that wraps another
 * SailConnection object and forwards any method calls to the wrapped
 * connection.
 * 
 * @author Jeen Broekstra
 */
public class SailConnectionWrapper implements SailConnection, FederatedServiceResolverClient {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped SailConnection.
	 */
	private SailConnection wrappedCon;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public SailConnectionWrapper(SailConnection wrappedCon) {
		this.wrappedCon = wrappedCon;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the connection that is wrapped by this object.
	 * 
	 * @return The SailConnection object that was supplied to the constructor of
	 *         this class.
	 */
	public SailConnection getWrappedConnection() {
		return wrappedCon;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		if (wrappedCon instanceof FederatedServiceResolverClient) {
			((FederatedServiceResolverClient)wrappedCon).setFederatedServiceResolver(resolver);
		}
	}

	@Override
	public boolean isOpen()
		throws SailException
	{
		return wrappedCon.isOpen();
	}

	@Override
	public void close()
		throws SailException
	{
		wrappedCon.close();
	}

	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		return wrappedCon.evaluate(tupleExpr, dataset, bindings, includeInferred);
	}

	@Override
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		return wrappedCon.getContextIDs();
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		return wrappedCon.getStatements(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public long size(Resource... contexts)
		throws SailException
	{
		return wrappedCon.size(contexts);
	}

	/*
	 * Not in the API, preserving for binary compatibility. Will be removed in future.
	 * 
	 * Should use {@link #size(Resource...)} instead, which is called by this method.
	 */
	public long size(Resource context)
		throws SailException
	{
		return wrappedCon.size(context);
	}

	@Override
	public void commit()
		throws SailException
	{
		wrappedCon.commit();
	}

	@Override
	public void rollback()
		throws SailException
	{
		wrappedCon.rollback();
	}

	@Override
	public void addStatement(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.addStatement(subj, pred, obj, contexts);
	}

	@Override
	public void removeStatements(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.removeStatements(subj, pred, obj, contexts);
	}

	@Override
	public void startUpdate(UpdateContext modify)
		throws SailException
	{
		wrappedCon.startUpdate(modify);
	}

	@Override
	public void addStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.addStatement(modify, subj, pred, obj, contexts);
	}

	@Override
	public void removeStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		wrappedCon.removeStatement(modify, subj, pred, obj, contexts);
	}

	@Override
	public void endUpdate(UpdateContext modify)
		throws SailException
	{
		wrappedCon.endUpdate(modify);
	}

	@Override
	public void clear(Resource... contexts)
		throws SailException
	{
		wrappedCon.clear(contexts);
	}

	@Override
	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		return wrappedCon.getNamespaces();
	}

	@Override
	public String getNamespace(String prefix)
		throws SailException
	{
		return wrappedCon.getNamespace(prefix);
	}

	@Override
	public void setNamespace(String prefix, String name)
		throws SailException
	{
		wrappedCon.setNamespace(prefix, name);
	}

	@Override
	public void removeNamespace(String prefix)
		throws SailException
	{
		wrappedCon.removeNamespace(prefix);
	}

	@Override
	public void clearNamespaces()
		throws SailException
	{
		wrappedCon.clearNamespaces();
	}

	@Override
	public void begin()
		throws SailException
	{
		wrappedCon.begin();
	}

	@Override
	public void begin(IsolationLevel level)
		throws SailException
	{
		wrappedCon.begin(level);
	}

	@Override
	public void flush()
		throws SailException
	{
		wrappedCon.flush();
	}

	@Override
	public void prepare()
		throws SailException
	{
		wrappedCon.prepare();
	}

	@Override
	public boolean isActive()
		throws UnknownSailTransactionStateException
	{
		return wrappedCon.isActive();
	}
}
