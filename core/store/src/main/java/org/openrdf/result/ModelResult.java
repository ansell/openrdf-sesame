/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result;

import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.store.StoreException;


/**
 * @author James Leigh
 */
public interface ModelResult extends RepositoryResult<Statement> {

	/**
	 * Retrieves relevant namespaces from the query result.
	 * 
	 * @return a Map<String, String> object containing (prefix, namespace) pairs.
	 * @throws StoreException
	 */
	public Map<String, String> getNamespaces()
		throws StoreException;

	public Model asModel()
		throws StoreException;

}
