/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import info.aduna.iteration.SingletonIteration;

import org.openrdf.query.QueryResult;
import org.openrdf.store.StoreException;

/**
 * Wraps a boolean, needed because Spring MVC @ModuleAttribute does not support
 * primitives or their wrappers.
 * 
 * @author James Leigh
 */
public class BooleanQueryResult extends SingletonIteration<Boolean, StoreException> implements
		QueryResult<Boolean>
{

	public BooleanQueryResult(boolean result) {
		super(result);
	}
}
