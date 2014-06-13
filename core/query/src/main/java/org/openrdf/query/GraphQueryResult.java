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

import java.util.Map;

import org.openrdf.model.Statement;

/**
 * A representation of a query result as a sequence of {@link Statement}
 * objects. Each query result consists of zero or more Statements and
 * additionaly carries information about relevant namespace declarations. Note:
 * take care to always close a GraphQueryResult after use to free any resources
 * it keeps hold of.
 * 
 * @author Jeen Broekstra
 */
public interface GraphQueryResult extends QueryResult<Statement> {

	/**
	 * Retrieves relevant namespaces from the query result.
	 * 
	 * @return a Map<String, String> object containing (prefix, namespace) pairs.
	 * @throws QueryEvaluationException
	 */
	public Map<String, String> getNamespaces()
		throws QueryEvaluationException;

}
