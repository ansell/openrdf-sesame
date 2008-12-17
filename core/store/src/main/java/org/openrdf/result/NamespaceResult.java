/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result;

import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public interface NamespaceResult extends RepositoryResult<Namespace> {

	public Map<String, String> asMap()
		throws StoreException;
}
