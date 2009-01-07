/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelegatingCursor;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.store.StoreException;

/**
 * Adds more bindings to each of the results.
 * 
 * @author James Leigh
 */
public class InsertBindingSetCursor extends DelegatingCursor<BindingSet> {

	private BindingSet bindings;

	public InsertBindingSetCursor(Cursor<BindingSet> delegate, BindingSet bindings) {
		super(delegate);
		this.bindings = bindings;
	}

	@Override
	public BindingSet next()
		throws StoreException
	{
		BindingSet next = super.next();
		if (next == null) {
			return null;
		}
		int size = bindings.size() + next.size();
		QueryBindingSet set = new QueryBindingSet(size);
		set.addAll(bindings);
		for (Binding binding : next) {
			set.setBinding(binding);
		}
		return set;
	}

}
