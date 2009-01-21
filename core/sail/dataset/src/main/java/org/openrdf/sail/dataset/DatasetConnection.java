/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.dataset;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.store.StoreException;

/**
 * Loads and refreshes all the default graphs from QueryModel in a cleared
 * context of the same URL. Loads and refreshes all the registered named graphs
 * from QueryModel in a cleared context of the named URI.
 * 
 * @author James Leigh
 */
public class DatasetConnection extends SailConnectionWrapper {

	private DatasetSail repository;

	public DatasetConnection(DatasetSail repository, SailConnection delegate) {
		super(delegate);
		this.repository = repository;
	}

	@Override
	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		for (URI dataset : query.getDefaultGraphs()) {
			repository.loadGraph(dataset);
		}
		for (URI dataset : query.getNamedGraphs()) {
			repository.loadGraph(dataset);
		}
		return super.evaluate(query, bindings, includeInferred);
	}
}
