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

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import info.aduna.iteration.Iteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.iterator.SPARQLMinusIteration;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeSPARQLMinusIteration extends SPARQLMinusIteration<QueryEvaluationException> {

	private AtomicLong used;

	private long maxSize;

	/**
	 * Creates a new MinusIteration that returns the results of the left argument
	 * minus the results of the right argument. By default, duplicates are
	 * <em>not</em> filtered from the results.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set. * @param used An atomic long used to monitor how
	 *        many elements are in the set collections.
	 * @param used
	 *        An atomic long used to monitor how many elements are in the set
	 *        collections.
	 * @param maxSize
	 *        Maximum size allowed by the sum of all collections used by the
	 *        LimitedSizeQueryEvaluatlion.
	 */
	public LimitedSizeSPARQLMinusIteration(Iteration<BindingSet, QueryEvaluationException> leftArg,
			Iteration<BindingSet, QueryEvaluationException> rightArg, AtomicLong used, long maxSize)
	{
		this(leftArg, rightArg, false, used, maxSize);
	}

	/**
	 * Creates a new SPARQLMinusIteration that returns the results of the left argument
	 * minus the results of the right argument.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 * @param used
	 *        An atomic long used to monitor how many elements are in the set
	 *        collections.
	 * @param maxSize
	 *        Maximum size allowed by the sum of all collections used by the
	 *        LimitedSizeQueryEvaluatlion.
	 */
	public LimitedSizeSPARQLMinusIteration(Iteration<BindingSet, QueryEvaluationException> leftArg,
			Iteration<BindingSet, QueryEvaluationException> rightArg, boolean distinct, AtomicLong used,
			long maxSize)
	{
		super(leftArg, rightArg, distinct);
		this.used = used;
		this.maxSize = maxSize;
	}
	
	@Override
	protected Set<BindingSet> makeSet(Iteration<BindingSet, QueryEvaluationException> rightArg2) 
			throws QueryEvaluationException {
		return LimitedSizeIteratorUtil.addAll(rightArg2, makeSet(),used, maxSize);
	}

	/**
	 * After closing the set is cleared and any "used" capacity for collections is returned.
	 */
	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		long size = clearExcludeSet();
		super.handleClose();
		used.addAndGet(-size);
	}


}
