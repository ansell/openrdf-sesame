/*
 * Copyright 3 Round Stones Inc. (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Provided with add and remove operation to give them context within a
 * {@link UpdateExpr} operation.
 * 
 * @author James Leigh
 * @since 2.7.0
 */
public class UpdateContext {

	private final UpdateExpr updateExpr;

	private final Dataset dataset;

	private final BindingSet bindings;

	private final boolean includeInferred;

	public UpdateContext(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) {
		assert updateExpr != null;
		this.updateExpr = updateExpr;
		if (dataset == null) {
			this.dataset = new DatasetImpl();
		} else {
			this.dataset = dataset;
		}
		if (bindings == null) {
			this.bindings = EmptyBindingSet.getInstance();
		} else {
			this.bindings = bindings;
		}
		this.includeInferred = includeInferred;
	}

	@Override
	public String toString() {
		return updateExpr.toString();
	}

	/**
	 * @return Returns the updateExpr.
	 */
	public UpdateExpr getUpdateExpr() {
		return updateExpr;
	}

	/**
	 * @return Returns the dataset.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * @return Returns the bindings.
	 */
	public BindingSet getBindingSet() {
		return bindings;
	}

	/**
	 * @return Returns the includeInferred.
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}
}
