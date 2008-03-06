/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Query;

/**
 * Abstract super class of all query types.
 */
public abstract class AbstractQuery implements Query {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected MapBindingSet _bindings = new MapBindingSet();

	protected boolean _includeInferred = true;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new query object.
	 */
	protected AbstractQuery() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void addBinding(String name, Value value) {
		setBinding(name, value);
	}

	public void setBinding(String name, Value value) {
		_bindings.addBinding(name, value);
	}

	public void removeBinding(String name) {
		_bindings.removeBinding(name);
	}

	public BindingSet getBindings() {
		return _bindings;
	}

	public void setIncludeInferred(boolean includeInferred) {
		_includeInferred = includeInferred;
	}

	public boolean getIncludeInferred() {
		return _includeInferred;
	}
}
