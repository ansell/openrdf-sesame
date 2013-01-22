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
package org.openrdf.query;

import java.util.List;

/**
 * A representation of a variable-binding query result as a sequence of
 * {@link BindingSet} objects. Each query result consists of zero or more
 * solutions, each of which represents a single query solution as a set of
 * bindings. Note: take care to always close a TupleQueryResult after use to
 * free any resources it keeps hold of.
 * 
 * @author jeen
 */
public interface TupleQueryResult extends QueryResult<BindingSet> {

	/**
	 * Gets the names of the bindings, in order of projection.
	 * 
	 * @return The binding names, in order of projection.
	 * @throws QueryEvaluationException
	 */
	public List<String> getBindingNames()
		throws QueryEvaluationException;
}
