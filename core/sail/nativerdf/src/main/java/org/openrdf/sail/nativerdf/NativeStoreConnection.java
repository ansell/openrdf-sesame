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
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.base.SailSourceConnection;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;

/**
 * @author Arjohn Kampman
 */
public class NativeStoreConnection extends SailSourceConnection {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final NativeStore nativeStore;

	/*-----------*
	 * Variables *
	 *-----------*/

	private volatile DefaultSailChangedEvent sailChangedEvent;

	/**
	 * The transaction lock held by this connection during transactions.
	 */
	private volatile Lock txnLock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeStoreConnection(NativeStore sail)
		throws IOException
	{
		super(sail, sail.getSailStore(), sail.getFederatedServiceResolver());
		this.nativeStore = sail;
		sailChangedEvent = new DefaultSailChangedEvent(sail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		if (!nativeStore.isWritable()) {
			throw new SailReadOnlyException("Unable to start transaction: data file is locked or read-only");
		}
		boolean releaseLock = true;
		try {
			if (txnLock == null || !txnLock.isActive()) {
				txnLock = nativeStore.getTransactionLock(getTransactionIsolation());
			}
			super.startTransactionInternal();
		}
		finally {
			if (releaseLock && txnLock != null) {
				txnLock.release();
			}
		}
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		try {
			super.commitInternal();
		}
		finally {
			if (txnLock != null) {
				txnLock.release();
			}
		}

		nativeStore.notifySailChanged(sailChangedEvent);

		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		try {
			super.rollbackInternal();
		}
		finally {
			if (txnLock != null) {
				txnLock.release();
			}
		}
		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	@Override
	protected void addStatementInternal(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		// assume the triple is not yet present in the triple store
		sailChangedEvent.setStatementsAdded(true);
	}

	public boolean addInferredStatement(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		boolean ret = super.addInferredStatement(subj, pred, obj, contexts);
		// assume the triple is not yet present in the triple store
		sailChangedEvent.setStatementsAdded(true);
		return ret;
	}

	@Override
	protected void removeStatementsInternal(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		sailChangedEvent.setStatementsRemoved(true);
	}

	public boolean removeInferredStatement(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException
	{
		boolean ret = super.removeInferredStatement(subj, pred, obj, contexts);
		sailChangedEvent.setStatementsRemoved(true);
		return ret;
	}

	@Override
	protected EvaluationStrategy getEvaluationStrategy(Dataset dataset, TripleSource tripleSource) {
		return new EvaluationStrategyImpl(tripleSource, dataset, getFederatedServiceResolver(),
				nativeStore.getIterationCacheSyncThreshold());
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		super.clearInternal(contexts);
		sailChangedEvent.setStatementsRemoved(true);
	}

	public void clearInferred(Resource... contexts)
		throws SailException
	{
		super.clearInferred(contexts);
		sailChangedEvent.setStatementsRemoved(true);
	}

}
