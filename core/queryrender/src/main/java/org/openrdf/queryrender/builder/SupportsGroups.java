/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
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
