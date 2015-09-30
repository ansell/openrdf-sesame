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
package org.eclipse.rdf4j.query.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.IterationWrapper;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;

/**
 * An iterating implementation of the {@link TupleQueryResult} interface.
 */
public class IteratingTupleQueryResult extends IterationWrapper<BindingSet, QueryEvaluationException> implements
		TupleQueryResult
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final List<String> bindingNames;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public IteratingTupleQueryResult(List<String> bindingNames, Iterable<? extends BindingSet> bindingSets) {
		this(bindingNames, bindingSets.iterator());
	}

	public IteratingTupleQueryResult(List<String> bindingNames, Iterator<? extends BindingSet> bindingSetIter) {
		this(bindingNames, new CloseableIteratorIteration<BindingSet, QueryEvaluationException>(bindingSetIter));
	}

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public IteratingTupleQueryResult(List<String> bindingNames,
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingSetIter)
	{
		super(bindingSetIter);
		// Don't allow modifications to the binding names when it is accessed
		// through getBindingNames:
		this.bindingNames = Collections.unmodifiableList(bindingNames);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public List<String> getBindingNames()
		throws QueryEvaluationException
	{
		return bindingNames;
	}

}
