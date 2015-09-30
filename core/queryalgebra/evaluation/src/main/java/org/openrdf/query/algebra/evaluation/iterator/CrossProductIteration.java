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
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Iteration which forms the cross product of a list of materialized input bindings
 * with each result obtained from the inner iteration.
 * 
 * Example:
 * <source>
 * inputBindings := {b1, b2, ...}
 * resultIteration := {r1, r2, ...}
 * 
 * getNextElement() returns (r1,b1), (r1, b2), ..., (r2, b1), (r2, b2), ...
 * 
 * i.e. compute the cross product per result binding
 * </source>
 * 
 * 
 * @author Andreas Schwarte
 */
public class CrossProductIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	protected final List<BindingSet> inputBindings;
	protected final CloseableIteration<BindingSet, QueryEvaluationException> resultIteration;

	protected Iterator<BindingSet> inputBindingsIterator = null;
	protected BindingSet currentInputBinding = null;
	
	public CrossProductIteration(
			CloseableIteration<BindingSet, QueryEvaluationException> resultIteration,
			List<BindingSet> inputBindings) {
		super();
		this.resultIteration = resultIteration;
		this.inputBindings = inputBindings;
	}
	
	@Override
	protected BindingSet getNextElement() throws QueryEvaluationException {
		
		if (currentInputBinding==null) {
			inputBindingsIterator = inputBindings.iterator();
			if (resultIteration.hasNext())
				currentInputBinding = resultIteration.next();
			else
				return null;  // no more results
		}	
		
		if (inputBindingsIterator.hasNext()) {
			BindingSet next = inputBindingsIterator.next();
			QueryBindingSet res = new QueryBindingSet(next.size() + currentInputBinding.size() );
			res.addAll(next);
			res.addAll(currentInputBinding);
			if (!inputBindingsIterator.hasNext())
				currentInputBinding = null;
			return res;
		}
		
		return null;
	}	
}
