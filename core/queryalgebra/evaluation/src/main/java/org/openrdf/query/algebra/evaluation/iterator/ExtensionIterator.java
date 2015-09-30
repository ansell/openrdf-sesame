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

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

public class ExtensionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	private final Extension extension;

	private final EvaluationStrategy strategy;

	public ExtensionIterator(Extension extension,
			CloseableIteration<BindingSet, QueryEvaluationException> iter, EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(iter);
		this.extension = extension;
		this.strategy = strategy;
	}

	@Override
	public BindingSet convert(BindingSet sourceBindings)
		throws QueryEvaluationException
	{
		QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);

		for (ExtensionElem extElem : extension.getElements()) {
			ValueExpr expr = extElem.getExpr();
			if (!(expr instanceof AggregateOperator)) {
				try {
					// we evaluate each extension element over the targetbindings, so that bindings from
					// a previous extension element in this same extension can be used by other extension elements. 
					// e.g. if a projection contains (?a + ?b as ?c) (?c * 2 as ?d)
					Value targetValue = strategy.evaluate(extElem.getExpr(), targetBindings);

					if (targetValue != null) {
						// Potentially overwrites bindings from super
						targetBindings.setBinding(extElem.getName(), targetValue);
					}
				}
				catch (ValueExprEvaluationException e) {
					// silently ignore type errors in extension arguments. They should not cause the 
					// query to fail but just result in no additional binding.
				}
			}
		}

		return targetBindings;
	}
}
