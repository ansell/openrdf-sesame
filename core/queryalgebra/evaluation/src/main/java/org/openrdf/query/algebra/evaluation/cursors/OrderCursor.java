/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.results.Cursor;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class OrderCursor implements Cursor<BindingSet> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Cursor<BindingSet> cursor;

	private final Comparator<BindingSet> comparator;

	private Iterator<BindingSet> ordered;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderCursor(Cursor<BindingSet> cursor, Comparator<BindingSet> comparator) {
		assert cursor != null : "cursor must not be null";
		assert comparator != null : " comparator must not be null";

		this.cursor = cursor;
		this.comparator = comparator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> getOrderedIterator()
		throws StoreException
	{
		if (ordered == null) {
			List<BindingSet> list = new ArrayList<BindingSet>(1024);
			BindingSet next;
			while ((next = cursor.next()) != null) {
				list.add(next);
			}
			Collections.sort(list, comparator);
			ordered = list.iterator();
		}

		return ordered;
	}

	public BindingSet next()
		throws StoreException
	{
		Iterator<BindingSet> iter = getOrderedIterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	public void close()
		throws StoreException
	{
		cursor.close();
	}

	@Override
	public String toString() {
		return "Order " + cursor.toString();
	}
}
