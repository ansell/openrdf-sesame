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
