/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.List;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelegatingCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.store.StoreException;

public class MultiProjectionCursor extends DelegatingCursor<BindingSet> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final List<ProjectionElemList> projections;

	private final BindingSet parentBindings;

	/*-----------*
	 * Variables *
	 *-----------*/

	private BindingSet currentBindings;

	private volatile int nextProjectionIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjectionCursor(MultiProjection multiProjection, Cursor<BindingSet> cursor,
			BindingSet bindings)
	{
		super(cursor);
		this.projections = multiProjection.getProjections();
		this.parentBindings = bindings;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public BindingSet next()
		throws StoreException
	{
		int idx = nextProjectionIdx;

		if (currentBindings == null || idx >= projections.size()) {
			currentBindings = super.next();

			if (currentBindings == null) {
				return null;
			}

			idx = nextProjectionIdx = 0;
		}

		ProjectionElemList nextProjection = projections.get(idx);
		nextProjectionIdx++;

		return ProjectionCursor.project(nextProjection, currentBindings, parentBindings);
	}

	@Override
	public void close()
		throws StoreException
	{
		super.close();
		nextProjectionIdx = projections.size();
	}
}
