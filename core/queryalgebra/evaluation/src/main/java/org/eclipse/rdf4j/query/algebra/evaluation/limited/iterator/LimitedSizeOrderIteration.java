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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.OrderIterator;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeOrderIteration extends OrderIterator {

	private final AtomicLong used;

	private final long maxSize;

	/**
	 * @param iter
	 * @param comparator
	 */
	public LimitedSizeOrderIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, AtomicLong used, long maxSize)
	{
		this(iter, comparator, Integer.MAX_VALUE, false, used, maxSize);
	}

	public LimitedSizeOrderIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, long limit, boolean distinct, AtomicLong used, long maxSize)
	{
		super(iter, comparator, limit, distinct);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	protected void removeLast(Collection<BindingSet> lastResults) {
		super.removeLast(lastResults);
		used.decrementAndGet();
	}

	@Override
	protected boolean add(BindingSet next, Collection<BindingSet> list)
		throws QueryEvaluationException
	{

		return LimitedSizeIteratorUtil.add(next, list, used, maxSize);
	}

	@Override
	protected Integer put(NavigableMap<BindingSet, Integer> map, BindingSet next, int count)
		throws QueryEvaluationException
	{
		final Integer i = map.get(next);
		final int oldCount = i == null ? 0 : i;
		
		final Integer put = super.put(map, next, count);

		if (oldCount < count) {
			if (used.incrementAndGet() > maxSize) {
				throw new QueryEvaluationException(
						"Size limited reached inside order operator query, max size is:" + maxSize);
			}
		}
		else if (oldCount > count) {
			used.decrementAndGet();
		}
		
		return put;
	}

}