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

package org.openrdf.sail.memory;


import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.base.SailSourceConnection;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;

/**
 * Implementation of a Sail Connection for memory stores.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class MemoryStoreConnection extends SailSourceConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final MemoryStore sail;

	private volatile DefaultSailChangedEvent sailChangedEvent;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected MemoryStoreConnection(MemoryStore sail) {
		super(sail, sail.getSailStore(), sail.getFederatedServiceResolver());
		this.sail = sail;
		sailChangedEvent = new DefaultSailChangedEvent(sail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		if (!sail.isWritable()) {
			throw new SailReadOnlyException("Unable to start transaction: data file is locked or read-only");
		}
		super.startTransactionInternal();
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		super.commitInternal();

		sail.notifySailChanged(sailChangedEvent);

		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(sail);
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		super.rollbackInternal();
		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(sail);
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
		return new SimpleEvaluationStrategy(tripleSource, dataset, getFederatedServiceResolver(), sail.getIterationCacheSyncThreshold());
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
