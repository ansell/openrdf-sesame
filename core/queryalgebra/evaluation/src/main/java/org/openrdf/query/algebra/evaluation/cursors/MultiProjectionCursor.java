/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.List;

import org.openrdf.StoreException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.ProjectionElemList;

public class MultiProjectionCursor implements Cursor<BindingSet> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final List<ProjectionElemList> projections;

	private final Cursor<BindingSet> cursor;

	private final BindingSet parentBindings;

	/*-----------*
	 * Variables *
	 *-----------*/

	private BindingSet currentBindings;

	private int nextProjectionIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjectionCursor(MultiProjection multiProjection,
			Cursor<BindingSet> cursor, BindingSet bindings)
	{
		this.projections = multiProjection.getProjections();
		this.cursor = cursor;
		this.parentBindings = bindings;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BindingSet next()
		throws StoreException
	{
		if (currentBindings == null || nextProjectionIdx >= projections.size()) {
			currentBindings = cursor.next();
			nextProjectionIdx = 0;
			if (currentBindings == null)
				return null;
		}

		ProjectionElemList nextProjection = projections.get(nextProjectionIdx++);
		return ProjectionCursor.project(nextProjection, currentBindings, parentBindings);
	}

	public void close() {
		nextProjectionIdx = projections.size();
		currentBindings = null;
	}

	@Override
	public String toString() {
		return "MultProjection " + cursor.toString();
	}
}
