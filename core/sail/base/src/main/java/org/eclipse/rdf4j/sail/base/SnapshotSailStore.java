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
package org.eclipse.rdf4j.sail.base;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.SailException;

/**
 * A {@link SailStore} wrapper that branches the backing {@link SailSource}s to
 * provide concurrent {@link IsolationLevels#SNAPSHOT_READ} isolation and
 * higher.
 * 
 * @author James Leigh
 */
public class SnapshotSailStore implements SailStore {

	/**
	 * The underlying {@link SailStore}.
	 */
	private final SailStore backingStore;

	/**
	 * {@link SailSource} of {@link SailStore#getExplicitSailSource()}
	 * .
	 */
	private final SailSourceBranch explicitAutoFlush;

	/**
	 * {@link SailSource} of {@link SailStore#getInferredSailSource()}
	 * .
	 */
	private final SailSourceBranch inferredAutoFlush;

	/**
	 * Wraps an {@link SailStore}, tracking changes in {@link SailModelFactory}
	 * instances.
	 * 
	 * @param backingStore
	 * @param modelFactory
	 */
	public SnapshotSailStore(SailStore backingStore, ModelFactory modelFactory) {
		this.backingStore = backingStore;
		explicitAutoFlush = new SailSourceBranch(backingStore.getExplicitSailSource(), modelFactory, true);
		inferredAutoFlush = new SailSourceBranch(backingStore.getInferredSailSource(), modelFactory, true);
	}

	@Override
	public void close()
		throws SailException
	{
		try {
			try {
				explicitAutoFlush.flush();
				inferredAutoFlush.flush();
			}
			finally {
				explicitAutoFlush.close();
				inferredAutoFlush.close();
			}
		}
		finally {
			backingStore.close();
		}
	}

	@Override
	public ValueFactory getValueFactory() {
		return backingStore.getValueFactory();
	}

	@Override
	public EvaluationStatistics getEvaluationStatistics() {
		return backingStore.getEvaluationStatistics();
	}

	@Override
	public SailSource getExplicitSailSource() {
		return explicitAutoFlush;
	}

	@Override
	public SailSource getInferredSailSource() {
		return inferredAutoFlush;
	}

}
