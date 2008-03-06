/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

/**
 * An immutable empty BindingSet.
 * 
 * @author Arjohn Kampman
 */
public class EmptyBindingSet implements BindingSet {

	private static final EmptyBindingSet _singleton = new EmptyBindingSet();

	public static BindingSet getInstance() {
		return _singleton;
	}

	private EmptyBindingIterator _iter = new EmptyBindingIterator();

	public Iterator<Binding> iterator() {
		return _iter;
	}

	public Binding getBinding(String bindingName) {
		return null;
	}

	public boolean hasBinding(String bindingName) {
		return false;
	}

	public Value getValue(String bindingName) {
		return null;
	}

	public int size() {
		return 0;
	}

	public boolean equals(Object o) {
		if (o instanceof BindingSet) {
			return ((BindingSet)o).size() == 0;
		}

		return false;
	}

	public int hashCode() {
		return 0;
	}

	public String toString() {
		return "[]";
	}

	private static class EmptyBindingIterator implements Iterator<Binding> {

		public boolean hasNext() {
			return false;
		}

		public Binding next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}
	}
}
