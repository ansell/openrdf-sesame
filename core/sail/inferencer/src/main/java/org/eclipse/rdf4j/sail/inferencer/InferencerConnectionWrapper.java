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
package org.eclipse.rdf4j.sail.inferencer;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailConnectionWrapper;

/**
 * An extension of ConnectionWrapper that implements the
 * {@link InferencerConnection} interface.
 * 
 * @author Arjohn Kampman
 */
public class InferencerConnectionWrapper extends NotifyingSailConnectionWrapper implements
		InferencerConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new InferencerConnectionWrapper object that wraps the supplied
	 * transaction.
	 */
	public InferencerConnectionWrapper(InferencerConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the connection that is wrapped by this object.
	 * 
	 * @return The connection that was supplied to the constructor of this class.
	 */
	@Override
	public InferencerConnection getWrappedConnection() {
		return (InferencerConnection)super.getWrappedConnection();
	}

	public boolean addInferredStatement(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getWrappedConnection().addInferredStatement(subj, pred, obj, contexts);
	}

	public boolean removeInferredStatement(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getWrappedConnection().removeInferredStatement(subj, pred, obj, contexts);
	}

	public void clearInferred(Resource... contexts)
		throws SailException
	{
		getWrappedConnection().clearInferred(contexts);
	}

	public void flush()
		throws SailException
	{
		getWrappedConnection().flush();
		flushUpdates();
	}

	public void flushUpdates()
		throws SailException
	{
		getWrappedConnection().flushUpdates();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public void prepare()
		throws SailException
	{
		flushUpdates();
		super.prepare();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public void commit()
		throws SailException
	{
		flushUpdates();
		super.commit();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		flushUpdates();
		return super.evaluate(tupleExpr, dataset, bindings, includeInferred);
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		flushUpdates();
		return super.getContextIDs();
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred,
			Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		flushUpdates();
		return super.getStatements(subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Calls {@link #flushUpdates()} before forwarding the call to the wrapped
	 * connection.
	 */
	@Override
	public long size(Resource... contexts)
		throws SailException
	{
		flushUpdates();
		return super.size(contexts);
	}
}
