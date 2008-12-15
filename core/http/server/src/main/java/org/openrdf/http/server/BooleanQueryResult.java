/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public static BooleanQueryResult EMPTY = new BooleanQueryResult(null);

	public BooleanQueryResult(Boolean result) {
		super(result);
	}

	public <C extends Collection<? super Boolean>> C addTo(C collection)
		throws StoreException
	{
		Boolean bool;
		while ((bool = next()) != null) {
			collection.add(bool);
		}
		return collection;
	}

	public List<Boolean> asList()
		throws StoreException
	{
		return addTo(new ArrayList<Boolean>());
	}

	public Set<Boolean> asSet()
		throws StoreException
	{
		return addTo(new HashSet<Boolean>());
	}
}
