/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

/**
 * An update operation on a {@link org.openrdf.repository.Repository} that can
 * be formulated in one of the supported query languages (for example SPARQL).
 * 
 * @author Jeen
 */
public interface Update extends Operation {

	/**
	 * Execute this update on the repository.
	 * 
	 * @throws UpdateExecutionException
	 *         if the update could not be successfully completed.
	 */
	void execute()
		throws UpdateExecutionException;

}
