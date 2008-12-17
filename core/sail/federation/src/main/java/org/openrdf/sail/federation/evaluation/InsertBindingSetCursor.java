/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.result.Cursor;
import org.openrdf.result.base.CursorWrapper;
import org.openrdf.store.StoreException;

/**
 * Adds more bindings to each of the results.
 * 
 * @author James Leigh
 */
public class InsertBindingSetCursor extends CursorWrapper<BindingSet> {

	private BindingSet bindings;

	public InsertBindingSetCursor(Cursor<BindingSet> delegate, BindingSet bindings) {
		super(delegate);
		this.bindings = bindings;
	}

	public BindingSet next()
		throws StoreException
	{
		BindingSet next = super.next();
		if (next == null)
			return null;
		int size = bindings.size() + next.size();
		QueryBindingSet set = new QueryBindingSet(size);
		set.addAll(bindings);
		set.addAll(next);
		return set;
	}

}
