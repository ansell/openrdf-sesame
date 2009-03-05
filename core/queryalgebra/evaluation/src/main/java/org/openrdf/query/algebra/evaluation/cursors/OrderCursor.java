/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openrdf.cursor.CollectionCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelayedCursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class OrderCursor extends DelayedCursor<BindingSet> {

	private final Cursor<BindingSet> cursor;

	private final Comparator<BindingSet> comparator;

	public OrderCursor(Cursor<BindingSet> cursor, Comparator<BindingSet> comparator) {
		assert cursor != null : "cursor must not be null";
		assert comparator != null : " comparator must not be null";

		this.cursor = cursor;
		this.comparator = comparator;
	}

	@Override
	protected Cursor<BindingSet> createCursor()
		throws StoreException
	{
		List<BindingSet> list = new ArrayList<BindingSet>(1024);

		BindingSet next;
		while ((next = cursor.next()) != null) {
			list.add(next);
		}
		cursor.close();

		if (!isClosed()) {
			Collections.sort(list, comparator);
			return new CollectionCursor<BindingSet>(list);
		}
		else {
			return EmptyCursor.getInstance();
		}
	}

	@Override
	protected void handleClose()
		throws StoreException
	{
		super.handleClose();
		cursor.close();
	}

	@Override
	public String toString() {
		return "Order " + cursor.toString();
	}
}
