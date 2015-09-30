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
package org.openrdf.query.algebra.evaluation.federation;

import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Inserts original bindings into the result, uses ?__rowIdx to resolve original
 * bindings. See {@link ServiceJoinIterator} and {@link SPARQLFederatedService}.
 * 
 * @author Andreas Schwarte
 */
public class ServiceJoinConversionIteration extends
		ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	protected final List<BindingSet> bindings;

	public ServiceJoinConversionIteration(
			CloseableIteration<BindingSet, QueryEvaluationException> iter,
			List<BindingSet> bindings) {
		super(iter);
		this.bindings = bindings;
	}

	@Override
	protected BindingSet convert(BindingSet bIn)
			throws QueryEvaluationException {

		// overestimate the capacity
		QueryBindingSet res = new QueryBindingSet(bIn.size() + bindings.size());

		int bIndex = -1;
		Iterator<Binding> bIter = bIn.iterator();
		while (bIter.hasNext()) {
			Binding b = bIter.next();
			String name = b.getName();
			if (name.equals("__rowIdx")) {
				bIndex = Integer.parseInt(b.getValue().stringValue());
				continue;
			}
			res.addBinding(b.getName(), b.getValue());
		}
		
		// should never occur: in such case we would have to create the cross product (which
		// is dealt with in another place)
		if (bIndex == -1)
			throw new QueryEvaluationException("Invalid join. Probably this is due to non-standard behavior of the SPARQL endpoint. " +
					"Please report to the developers.");
		
		res.addAll(bindings.get(bIndex));
		return res;
	}
}
