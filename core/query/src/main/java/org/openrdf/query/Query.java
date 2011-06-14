/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;


/**
 * A query on a {@link org.openrdf.repository.Repository} that can be formulated
 * in one of the supported query languages (for example SeRQL or SPARQL). It
 * allows one to predefine bindings in the query to be able to reuse the same
 * query with different bindings.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @see org.openrdf.repository.RepositoryConnection
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
	 */
	public void setMaxQueryTime(int maxQueryTime);

	/**
	 * Returns the maximum query evaluation time.
	 * 
	 * @return The maximum query evaluation time, measured in seconds.
	 * @see #setMaxQueryTime(int)
	 */
	public int getMaxQueryTime();
}
