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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeIteratorUtil {

	/**
	 * 
	 * @param arg2 the iteration with elements to add to the includeSet
	 * @param includeSet the set that should have all unique elements of arg2
	 * @param used the collection size counter of all collections used in answering a query
	 * @param maxSize the point at which we throw a new query exception
	 * @return the includeSet 
	 * @throws QueryEvaluationException trigerred when maxSize is smaller than the used value
	 */
	public static Set<BindingSet> addAll(Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2,
			Set<BindingSet> includeSet, AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		while (arg2.hasNext()) {
			if (includeSet.add(arg2.next()) && used.incrementAndGet() > maxSize)
				throw new QueryEvaluationException("Size limited reached inside intersect operator");
		}
		return includeSet;
	}

	/**
	 * @param object
	 * 		 object to put in set if not there already.
	 * @param excludeSet
	 * 		 set that we need to store object in.
	 * @param used
	 *        AtomicLong tracking how many elements we have in storage.
	 * @param maxSize
	 * @throws QueryEvaluationException
	 *         when the object is added to the set and the total elements in all
	 *         limited size collections exceed the allowed maxSize.
	 */
	public static <V> boolean add(V object, Collection<V> excludeSet, AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		boolean add = excludeSet.add(object);
		if (add && used.incrementAndGet() > maxSize)
			throw new QueryEvaluationException("Size limited reached inside query operator.");
		return add;
	}
}
