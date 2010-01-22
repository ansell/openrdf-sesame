/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;

/**
 * Abstract super class of all query types.
 */
public abstract class AbstractQuery implements Query {

	/*------------*
	 * Attributes *
	 *------------*/

	protected final MapBindingSet bindings = new MapBindingSet();

	protected Dataset dataset = null;

	protected boolean includeInferred = true;

	protected int maxQueryTime = 0;

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

	public void setBinding(String name, Value value) {
		bindings.addBinding(name, value);
	}

	public void removeBinding(String name) {
		bindings.removeBinding(name);
	}

	public void clearBindings() {
		bindings.clear();
	}

	public BindingSet getBindings() {
		return bindings;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	public boolean getIncludeInferred() {
		return includeInferred;
	}

	public void setMaxQueryTime(int maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
	}

	public int getMaxQueryTime() {
		return maxQueryTime;
	}
}
