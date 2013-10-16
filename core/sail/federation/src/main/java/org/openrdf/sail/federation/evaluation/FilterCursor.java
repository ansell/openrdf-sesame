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

import java.util.Set;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.iterator.FilterIterator;

/**
 * Provides a convenient constructor for {@link FilterIterator} using the
 * condition.
 * 
 * @author James Leigh
 */
public class FilterCursor extends FilterIterator {

	public FilterCursor(CloseableIteration<BindingSet, QueryEvaluationException> result, ValueExpr condition,
			final Set<String> scopeBindingNames, EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(new Filter(new EmptySet() {

			@Override
			public Set<String> getBindingNames() {
				return scopeBindingNames;
			}
		}, condition), result, strategy);
	}

}
