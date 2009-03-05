/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.ConvertingCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

public class ProjectionCursor extends ConvertingCursor<BindingSet, BindingSet> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Projection projection;

	private final BindingSet parentBindings;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ProjectionCursor(Projection projection, Cursor<BindingSet> iter, BindingSet parentBindings)
		throws EvaluationException
	{
		super(iter);
		this.projection = projection;
		this.parentBindings = parentBindings;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet convert(BindingSet sourceBindings)
		throws EvaluationException
	{

		return project(projection.getProjectionElemList(), sourceBindings, parentBindings);
	}

	public static BindingSet project(ProjectionElemList projElemList, BindingSet sourceBindings,
			BindingSet parentBindings)
	{
		QueryBindingSet resultBindings = new QueryBindingSet(parentBindings);

		for (ProjectionElem pe : projElemList.getElements()) {
			Value targetValue = sourceBindings.getValue(pe.getSourceName());
			if (targetValue != null) {
				// Potentially overwrites bindings from super
				resultBindings.setBinding(pe.getTargetName(), targetValue);
			}
		}

		return resultBindings;
	}
}
