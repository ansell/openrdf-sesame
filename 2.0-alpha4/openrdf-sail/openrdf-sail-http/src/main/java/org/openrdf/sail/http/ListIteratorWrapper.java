/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.http;

import java.util.Iterator;

import org.openrdf.util.iterator.IteratorWrapper;

public class ListIteratorWrapper<E> extends IteratorWrapper<E> {

	public ListIteratorWrapper(Iterator<? extends E> iter) {
		super(iter);
	}

}
