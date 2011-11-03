/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.query.parser.serql.ast;

import java.util.AbstractList;
import java.util.List;

/**
 * A list that wraps another list and casts its elements to a specific subtype
 * of the list's element type.
 */
class CastingList<E> extends AbstractList<E> {

	protected List<? super E> _elements;

	public CastingList(List<? super E> elements) {
		_elements = elements;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E) _elements.get(index);
	}

	@Override
	public int size() {
		return _elements.size();
	}
}
