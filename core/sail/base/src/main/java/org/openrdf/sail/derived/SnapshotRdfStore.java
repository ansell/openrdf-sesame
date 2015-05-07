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
package org.openrdf.sail.derived;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.sail.SailException;

/**
 *
 * @author James Leigh
 */
public class SnapshotRdfStore implements RdfStore {
	private final RdfStore backingStore;

	private final DerivedRdfBranch explicitAutoFlush;

	private final DerivedRdfBranch inferredAutoFlush;

	public SnapshotRdfStore(RdfStore backingStore, RdfModelFactory modelFactory) {
		this.backingStore = backingStore;
		explicitAutoFlush = new DerivedRdfBranch(backingStore.getExplicitRdfSource(IsolationLevels.NONE), modelFactory, true);
		inferredAutoFlush = new DerivedRdfBranch(backingStore.getInferredRdfSource(IsolationLevels.NONE), modelFactory, true);
	}

	@Override
	public void close()
		throws SailException
	{
		try {
			try {
				explicitAutoFlush.flush();
				inferredAutoFlush.flush();
			} finally {
				explicitAutoFlush.close();
				inferredAutoFlush.close();
			}
		} finally {
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

	public RdfSource getExplicitRdfSource(IsolationLevel level) {
		if (IsolationLevels.NONE.isCompatibleWith(level) && !explicitAutoFlush.isChanged()) {
			return backingStore.getExplicitRdfSource(level);
		} else {
			return explicitAutoFlush;
		}
	}

	public RdfSource getInferredRdfSource(IsolationLevel level) {
		if (IsolationLevels.NONE.isCompatibleWith(level) && !inferredAutoFlush.isChanged()) {
			return backingStore.getInferredRdfSource(level);
		} else {
			return inferredAutoFlush;
		}
	}

}
