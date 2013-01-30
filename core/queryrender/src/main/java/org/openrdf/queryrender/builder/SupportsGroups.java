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
package org.openrdf.queryrender.builder;

/**
 * <p>
 * Interface for anything that supports having a collection of groups or
 * sub-groups.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public interface SupportsGroups<T> {

	/**
	 * Add this group from the query
	 * 
	 * @param theGroup
	 *        the group to add
	 * @return this builder
	 */
	public T addGroup(Group theGroup);

	/**
	 * Remove this group from the query
	 * 
	 * @param theGroup
	 *        the group to remove
	 * @return this builder
	 */
	public T removeGroup(Group theGroup);
}
