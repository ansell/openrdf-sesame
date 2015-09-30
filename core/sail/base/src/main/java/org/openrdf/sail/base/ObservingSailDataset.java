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
package org.openrdf.sail.base;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevels;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 * A {@link IsolationLevels#SERIALIZABLE} {@link SailDataset} that tracks the
 * observed statement patterns to an
 * {@link SailSink#observe(Resource, IRI, Value, Resource...)} to check
 * consistency.
 * 
 * @author James Leigh
 */
class ObservingSailDataset extends DelegatingSailDataset {

	/**
	 * The {@link SailSink} that is tracking the statement patterns.
	 */
	private final SailSink observer;

	/**
	 * Creates a {@link IsolationLevels#SERIALIZABLE} {@link SailDataset} that
	 * tracks consistency.
	 * 
	 * @param delegate
	 *        to be {@link SailDataset#close()} when this {@link SailDataset} is
	 *        closed.
	 * @param observer
	 *        to be {@link SailSink#flush()} and {@link SailSink#close()} when this
	 *        {@link SailDataset} is closed.
	 */
	public ObservingSailDataset(SailDataset delegate, SailSink observer) {
		super(delegate);
		this.observer = observer;
	}

	@Override
	public void close()
		throws SailException
	{
		super.close();
		// flush observer regardless of consistency
		observer.flush();
		observer.close();
	}

	@Override
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		observer.observe(null, null, null);
		return super.getContextIDs();
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		observer.observe(subj, pred, obj, contexts);
		return super.getStatements(subj, pred, obj, contexts);
	}

}
