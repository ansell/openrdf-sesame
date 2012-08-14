/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import org.openrdf.query.Query;

/**
 * Abstract super class of all query types.
 */
public abstract class AbstractQuery extends AbstractOperation implements Query {

	/*------------*
	 * Attributes *
	 *------------*/

	protected int maxQueryTime = 0;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new query object.
	 */
	protected AbstractQuery() {
		super();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setMaxQueryTime(int maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
	}

	public int getMaxQueryTime() {
		return maxQueryTime;
	}
}
