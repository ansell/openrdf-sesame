/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import java.util.concurrent.atomic.AtomicReference;

import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class SingletonCursor<E> implements Cursor<E> {

	private final AtomicReference<E> value;

	public SingletonCursor(E value) {
		assert value != null : "value must not be null";
		this.value = new AtomicReference<E>(value);
	}

	public E next()
		throws StoreException
	{
		return value.getAndSet(null);
	}

	public void close()
		throws StoreException
	{
		value.set(null);
	}

	@Override
	public String toString() {
		return value.get().toString();
	}
}
