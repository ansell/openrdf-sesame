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
package org.openrdf.sail.base;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.sail.SailException;

/**
 * A {@link SailStore} wrapper that branches the backing {@link SailSource}s to
 * provide concurrent {@link IsolationLevels#SNAPSHOT_READ} isolation and
 * higher.
 * 
 * @author James Leigh
 */
public class SnapshotSailStore implements SailStore {

	private static final IsolationLevels NONE = IsolationLevels.NONE;

	/**
	 * The underlying {@link SailStore}.
	 */
	private final SailStore backingStore;

	/**
	 * {@link SailBranch} of {@link SailStore#getExplicitSailSource(IsolationLevel)}
	 * .
	 */
	private final DerivedSailBranch explicitAutoFlush;

	/**
	 * {@link SailBranch} of {@link SailStore#getInferredSailSource(IsolationLevel)}
	 * .
	 */
	private final DerivedSailBranch inferredAutoFlush;

	/**
	 * Wraps an {@link SailStore}, tracking changes in {@link SailModelFactory}
	 * instances.
	 * 
	 * @param backingStore
	 * @param modelFactory
	 */
	public SnapshotSailStore(SailStore backingStore, SailModelFactory modelFactory) {
		this.backingStore = backingStore;
		explicitAutoFlush = new DerivedSailBranch(backingStore.getExplicitSailSource(NONE), modelFactory, true);
		inferredAutoFlush = new DerivedSailBranch(backingStore.getInferredSailSource(NONE), modelFactory, true);
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
	public SailSource getExplicitSailSource(IsolationLevel level) {
		if (NONE.isCompatibleWith(level) && !explicitAutoFlush.isChanged()) {
			return backingStore.getExplicitSailSource(level);
		}
		else {
			return explicitAutoFlush;
		}
	}

	@Override
	public SailSource getInferredSailSource(IsolationLevel level) {
		if (NONE.isCompatibleWith(level) && !inferredAutoFlush.isChanged()) {
			return backingStore.getInferredSailSource(level);
		}
		else {
			return inferredAutoFlush;
		}
	}

}
