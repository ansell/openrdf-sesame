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

/**
 * A query on a repository that can be formulated in one of the supported query
 * languages (for example SeRQL or SPARQL). It allows one to predefine bindings
 * in the query to be able to reuse the same query with different bindings.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public interface Query extends Operation {

	/**
	 * Specifies the maximum time that a query is allowed to run. The query will
	 * be interrupted when it exceeds the time limit. Any consecutive requests to
	 * fetch query results will result in {@link QueryInterruptedException}s.
	 * 
	 * @param maxQueryTime
	 *        The maximum query time, measured in seconds. A negative or zero
	 *        value indicates an unlimited query time (which is the default).
	 * @deprecated since 2.8.0. Use {@link Operation#setMaxExecutionTime(int)}
	 *             instead.
	 */
	@Deprecated
	public void setMaxQueryTime(int maxQueryTime);

	/**
	 * Returns the maximum query evaluation time.
	 * 
	 * @return The maximum query evaluation time, measured in seconds.
	 * @see #setMaxQueryTime(int)
	 * @deprecated since 2.8.0. Use {@link Operation#getMaxExecutionTime()}
	 *             instead.
	 */
	@Deprecated
	public int getMaxQueryTime();
}
