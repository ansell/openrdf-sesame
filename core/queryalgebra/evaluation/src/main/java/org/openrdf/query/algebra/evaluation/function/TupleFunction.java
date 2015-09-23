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
package org.openrdf.query.algebra.evaluation.function;

import info.aduna.iteration.CloseableIteration;

import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;

/**
 * A function that can return a tuple of {@link Value}s.
 * This can be used to implement "property functions" or "magic properties".
 */
public interface TupleFunction {

	public String getURI();

	public CloseableIteration<? extends List<? extends Value>,QueryEvaluationException> evaluate(ValueFactory valueFactory, Value... args)
		throws QueryEvaluationException;
}
