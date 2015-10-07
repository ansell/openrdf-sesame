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
package org.openrdf.sail.inferencer.fc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.InferencerConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DedupingInferencerConnection extends InferencerConnectionWrapper {

	private static final int MAX_SIZE = 10000;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ValueFactory valueFactory;
	private Set<Statement> addedStmts;
	private int dedupCount;

	public DedupingInferencerConnection(InferencerConnection con, ValueFactory vf) {
		super(con);
		this.valueFactory = vf;
		this.addedStmts = createDedupBuffer();
	}

	private Set<Statement> createDedupBuffer() {
		return Collections.newSetFromMap(new LRUMap<Statement,Boolean>(MAX_SIZE));
	}

	@Override
	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		if(contexts.length == 0) {
			// most inferred statements don't have a context so let's just deal with those
			Statement stmt = valueFactory.createStatement(subj, pred, obj);
			if(addedStmts.add(stmt)) {
				return super.addInferredStatement(subj, pred, obj);
			}
			else {
				dedupCount++;
				return false;
			}
		}
		else {
			return super.addInferredStatement(subj, pred, obj, contexts);
		}
	}

	@Override
	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		Statement stmt = valueFactory.createStatement(subj, pred, obj);
		addedStmts.remove(stmt);
		return super.removeInferredStatement(subj, pred, obj, contexts);
	}

	@Override
	public void clearInferred(Resource... contexts)
		throws SailException
	{
		resetDedupBuffer();
		super.clearInferred(contexts);
	}

	@Override
	public void commit()
		throws SailException
	{
		super.commit();
		logger.debug("Added {} unique statements, deduped {}", addedStmts.size(), dedupCount);
		resetDedupBuffer();
	}

	@Override
	public void rollback()
		throws SailException
	{
		super.rollback();
		resetDedupBuffer();
	}

	private void resetDedupBuffer()
	{
		addedStmts = null; // allow gc before alloc
		addedStmts = createDedupBuffer();
		dedupCount = 0;
	}



	static class LRUMap<K,V> extends LinkedHashMap<K,V>
	{
		final int maxSize;

		LRUMap(int maxSize) {
			super(maxSize, 0.75f, true);
			this.maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(Entry<K, V> entry) {
			return size() > maxSize;
		}
	}
}
