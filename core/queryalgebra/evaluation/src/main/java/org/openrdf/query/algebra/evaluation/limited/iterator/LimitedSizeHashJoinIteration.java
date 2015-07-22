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
package org.openrdf.query.algebra.evaluation.limited.iterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.iterator.BindingSetHashKey;
import org.openrdf.query.algebra.evaluation.iterator.HashJoinIteration;


/**
 *
 * @author MJAHale
 */
public class LimitedSizeHashJoinIteration extends HashJoinIteration {
	private static final String SIZE_LIMIT_REACHED = "Size limited reached inside bottom up join operator, max size is:";
	private AtomicLong used;

	private long maxSize;

	public LimitedSizeHashJoinIteration(EvaluationStrategy limitedSizeEvaluationStrategy,
			Join join, BindingSet bindings, AtomicLong used, long maxSize)
			throws QueryEvaluationException
	{
		super(limitedSizeEvaluationStrategy, join, bindings);
		this.used = used;
		this.maxSize = maxSize;
	}


	protected <E> E nextFromCache(Iterator<E> iter)
	{
		E v = iter.next();
		used.decrementAndGet();
		iter.remove();
		return v;
	}

	protected <E> void add(Collection<E> col, E value)
		throws QueryEvaluationException
	{
		if (col.add(value) && used.incrementAndGet() > maxSize) {
			throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
		}
	}

	protected <E> void addAll(Collection<E> col, List<E> values)
		throws QueryEvaluationException
	{
		for (E v : values) {
			add(col, v);
		}
	}

	protected void putHashTableEntry(Map<BindingSetHashKey, List<BindingSet>> hashTable, BindingSetHashKey hashKey,
			List<BindingSet> hashValue)
		throws QueryEvaluationException
	{
		List<BindingSet> put = hashTable.put(hashKey, hashValue);
		if (put == null && used.incrementAndGet() > maxSize) {
			throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
		}
	}

	@Override
	protected void disposeHashTable(Map<BindingSetHashKey, List<BindingSet>> map)
	{
		long htvSize = map.size();
		map.clear();
		used.addAndGet(-htvSize);
	}

}
