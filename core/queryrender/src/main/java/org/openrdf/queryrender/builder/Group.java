/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender.builder;

/**
 * <p>
 * Abstract representation of a group of atoms in a query
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public interface Group extends SupportsExpr {

	public boolean isOptional();

	public void addChild(Group theGroup);

	public int size();
}
