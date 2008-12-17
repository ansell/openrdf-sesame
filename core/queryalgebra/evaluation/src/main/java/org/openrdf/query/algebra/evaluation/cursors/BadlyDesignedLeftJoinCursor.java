/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class BadlyDesignedLeftJoinCursor extends LeftJoinCursor {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final BindingSet inputBindings;

	private final Set<String> problemVars;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BadlyDesignedLeftJoinCursor(EvaluationStrategy strategy, LeftJoin join, BindingSet inputBindings,
			Set<String> problemVars)
		throws StoreException
	{
		super(strategy, join, getFilteredBindings(inputBindings, problemVars));
		this.inputBindings = inputBindings;
		this.problemVars = problemVars;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public BindingSet next()
		throws StoreException
	{
		BindingSet result = super.next();

		// Ignore all results that are not compatible with the input bindings
		while (result != null && !QueryResultUtil.bindingSetsCompatible(inputBindings, result)) {
			result = super.next();
		}

		if (result != null) {
			// Make sure the provided problemVars are part of the returned results
			// (necessary in case of e.g. LeftJoin and Union arguments)
			QueryBindingSet extendedResult = null;

			for (String problemVar : problemVars) {
				if (!result.hasBinding(problemVar)) {
					if (extendedResult == null) {
						extendedResult = new QueryBindingSet(result);
					}
					extendedResult.addBinding(problemVar, inputBindings.getValue(problemVar));
				}
			}

			if (extendedResult != null) {
				result = extendedResult;
			}
		}

		return result;
	}

	/*--------------------*
	 * Static util method *
	 *--------------------*/

	private static QueryBindingSet getFilteredBindings(BindingSet bindings, Set<String> problemVars) {
		QueryBindingSet filteredBindings = new QueryBindingSet(bindings);
		filteredBindings.removeAll(problemVars);
		return filteredBindings;
	}
}
