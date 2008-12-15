/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public interface BooleanResult extends Result<Boolean> {

	public boolean asBoolean()
		throws StoreException;
}
