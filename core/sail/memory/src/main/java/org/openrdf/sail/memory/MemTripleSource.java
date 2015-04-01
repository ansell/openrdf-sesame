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
package org.openrdf.sail.memory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;

/**
 * Implementation of the TripleSource interface from the Sail Query Model
 */
class MemTripleSource implements TripleSource {

	protected final int snapshot;

	protected final ReadMode readMode;

	protected final boolean includeInferred;
	
	protected final MemoryStore store;

	MemTripleSource(MemoryStore store, boolean includeInferred, int snapshot, ReadMode readMode) {
		this.store = store;
		this.includeInferred = includeInferred;
		this.snapshot = snapshot;
		this.readMode = readMode;
	}

	public CloseableIteration<MemStatement, QueryEvaluationException> getStatements(Resource subj,
			IRI pred, Value obj, Resource... contexts)
	{
		return store.createStatementIterator(QueryEvaluationException.class, subj, pred, obj,
				!includeInferred, snapshot, readMode, contexts);
	}

	public MemValueFactory getValueFactory() {
		return store.getValueFactory();
	}
} // end inner class MemTripleSource