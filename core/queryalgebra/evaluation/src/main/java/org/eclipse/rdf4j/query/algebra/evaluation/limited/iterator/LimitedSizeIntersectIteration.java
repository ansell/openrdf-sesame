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

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.common.iteration.IntersectIteration;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeIntersectIteration extends IntersectIteration<BindingSet, QueryEvaluationException> {

	private final AtomicLong used;

	private final long maxSize;

	/**
	 * Creates a new IntersectIteration that returns the intersection of the
	 * results of two Iterations. By default, duplicates are <em>not</em>
	 * filtered from the results.
	 * 
	 * @param arg1
	 *        An Iteration containing the first set of elements.
	 * @param arg2
	 *        An Iteration containing the second set of elements.
	 * @param used
	 *        An atomic long used to monitor how many elements are in the set
	 *        collections.
	 * @param maxSize
	 *        Maximum size allowed by the sum of all collections used by the
	 *        LimitedSizeQueryEvaluatlion.
	 */
	public LimitedSizeIntersectIteration(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg1,
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2, AtomicLong used,
			long maxSize)
	{
		this(arg1, arg2, false, used, maxSize);

	}

	public LimitedSizeIntersectIteration(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg1,
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2, boolean distinct,
			AtomicLong used, long maxSize)
	{
		super(arg1, arg2, distinct);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	public Set<BindingSet> addSecondSet(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2, Set<BindingSet> set)
		throws QueryEvaluationException
	{

		LimitedSizeIteratorUtil.addAll(arg2, set, used, maxSize);
		return set;
	}

	/**
	 * After closing the set is cleared and any "used" capacity for collections
	 * is returned.
	 */
	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		
		long size = clearIncludeSet();
		used.addAndGet(-size);
		super.handleClose();
	}

}
