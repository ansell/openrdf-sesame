/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.StoreException;

/**
 * @author Arjohn Kampman
 */
public interface BooleanQuery extends Query {

	public boolean evaluate()
		throws StoreException;
}
