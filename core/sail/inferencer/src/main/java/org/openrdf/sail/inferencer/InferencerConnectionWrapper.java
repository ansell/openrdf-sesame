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
package org.openrdf.sail.inferencer;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;

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
