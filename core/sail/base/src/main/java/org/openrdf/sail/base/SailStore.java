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
package org.openrdf.sail.base;

import org.openrdf.IsolationLevel;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;

/**
 * A high level interface used by {@link DerivedSailConnection} to access
 * {@link SailSource}.
 * 
 * @author James Leigh
 */
public interface SailStore extends SailClosable {

	/**
	 * The {@link ValueFactory} that should be used in association with this.
	 * 
	 * @return this object's {@link ValueFactory}
	 */
	ValueFactory getValueFactory();

	/**
	 * Used by {@link DerivedSailConnection} to determine query join order.
	 * 
	 * @return a {@link EvaluationStatistics} that is aware of the data
	 *         distribution of this {@link SailStore}.
	 */
	EvaluationStatistics getEvaluationStatistics();

	/**
	 * @param level
	 *        Minimum isolation level required
	 * @return {@link SailSource} of only explicit statements
	 */
	SailSource getExplicitSailSource(IsolationLevel level);

	/**
	 * @param level
	 *        Minimum isolation level required
	 * @return {@link SailSource} of only inferred statements
	 */
	SailSource getInferredSailSource(IsolationLevel level);

}