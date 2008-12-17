/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.store.StoreException;

/**
 * Super type of all query result types (TupleQueryResult, GraphQueryResult,
 * etc.).
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public interface Result<T> extends Cursor<T> {

	public boolean hasNext()
		throws StoreException;

	public List<T> asList()
		throws StoreException;

	public Set<T> asSet()
		throws StoreException;

	public <C extends Collection<? super T>> C addTo(C collection)
		throws StoreException;
}
