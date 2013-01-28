/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Adds more bindings to each of the results.
 * 
 * @author James Leigh
 */
public class InsertBindingSetCursor extends
		IterationWrapper<BindingSet, QueryEvaluationException> {

	private final BindingSet bindings;

	public InsertBindingSetCursor(
			CloseableIteration<BindingSet, QueryEvaluationException> delegate,
			BindingSet bindings) {
		super(delegate);
		this.bindings = bindings;
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
		BindingSet next = super.next();
		QueryBindingSet result;
		if (next == null) {
			result = null; // NOPMD
		} else {
			int size = bindings.size() + next.size();
			result = new QueryBindingSet(size);
			result.addAll(bindings);
			for (Binding binding : next) {
				result.setBinding(binding);
			}
		}
		return result;
	}

}
