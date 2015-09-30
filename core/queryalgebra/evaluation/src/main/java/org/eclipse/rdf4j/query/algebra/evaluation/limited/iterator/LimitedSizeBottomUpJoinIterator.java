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
package org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.BottomUpJoinIterator;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 * @deprecated replaced by LimitedSizeHashJoinIteration
 */
@Deprecated
public class LimitedSizeBottomUpJoinIterator extends BottomUpJoinIterator {

	private static final String SIZE_LIMIT_REACHED = "Size limited reached inside bottom up join operator, max size is:";
	private AtomicLong used;

	private long maxSize;

	/**
	 * @param limitedSizeEvaluationStrategy
	 * @param join
	 * @param bindings
	 * @param used
	 * @param maxSize
	 * @throws QueryEvaluationException
	 */
	public LimitedSizeBottomUpJoinIterator(EvaluationStrategy limitedSizeEvaluationStrategy,
			Join join, BindingSet bindings, AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		super(limitedSizeEvaluationStrategy, join, bindings);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	protected void addAll(List<BindingSet> hashTableValues, List<BindingSet> values)
		throws QueryEvaluationException
	{
		Iterator<BindingSet> iter = values.iterator();
		while (iter.hasNext()) {
			if (hashTableValues.add(iter.next()) && used.incrementAndGet() > maxSize) {
				throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
			}
		}
	}

	@Override
	protected void add(List<BindingSet> leftArgResults, BindingSet b)
		throws QueryEvaluationException
	{
		if (leftArgResults.add(b) && used.incrementAndGet() > maxSize) {
			throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
		}
	}

	@Override
	protected BindingSet removeFirstElement(List<BindingSet> list)
		throws QueryEvaluationException
	{
		used.decrementAndGet();
		return super.removeFirstElement(list);
	}

	@Override
	protected void put(Map<BindingSet, List<BindingSet>> hashTable, BindingSet hashKey,
			List<BindingSet> hashValue)
		throws QueryEvaluationException
	{
		List<BindingSet> put = hashTable.put(hashKey, hashValue);
		if (put == null && used.incrementAndGet() > maxSize) {
			throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
		}
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		long htvSize = clearHashTable();
		super.handleClose();
		used.addAndGet(-htvSize);
	}

	

}
