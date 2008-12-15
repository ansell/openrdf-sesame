/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public interface BooleanQuery extends Query {

	public BooleanResult evaluate()
		throws StoreException;

	public boolean ask() throws StoreException;
}
