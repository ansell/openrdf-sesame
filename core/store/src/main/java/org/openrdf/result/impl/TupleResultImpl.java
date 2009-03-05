/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openrdf.cursor.CollectionCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.query.BindingSet;
import org.openrdf.result.TupleResult;

/**
 * A generic implementation of the {@link TupleResult} interface.
 */
public class TupleResultImpl extends ResultImpl<BindingSet> implements TupleResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final List<String> bindingNames;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public TupleResultImpl(List<String> bindingNames, Iterable<? extends BindingSet> bindingSets) {
		this(bindingNames, bindingSets.iterator());
	}

	public TupleResultImpl(List<String> bindingNames, Iterator<? extends BindingSet> bindingSetIter) {
		this(bindingNames, new CollectionCursor<BindingSet>(bindingSetIter));
	}

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public TupleResultImpl(List<String> bindingNames, Cursor<? extends BindingSet> bindingSetIter) {
		super(bindingSetIter);
		// Don't allow modifications to the binding names when it is accessed
		// through getBindingNames:
		this.bindingNames = Collections.unmodifiableList(bindingNames);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<String> getBindingNames() {
		return bindingNames;
	}
}
