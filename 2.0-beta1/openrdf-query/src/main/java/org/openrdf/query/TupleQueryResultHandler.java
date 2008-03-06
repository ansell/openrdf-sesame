/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.List;

/**
 * An interface defining methods related to handling sequences of Solutions.
 */
public interface TupleQueryResultHandler {

	/**
	 * Indicates the start of a sequence of Solutions. The supplied bindingNames
	 * are an indication of the values that are in the Solutions. For example, a
	 * SeRQL query like <tt>select X, Y from {X} P {Y} </tt> will have binding
	 * names <tt>X</tt> and <tt>Y</tt>.
	 * 
	 * @param bindingNames
	 *        An ordered set of binding names.
	 * @param distinct
	 *        Flag indicating whether the solutions in the query result are
	 *        guaranteed to be unique.
	 * @param ordered
	 *        Flag indicating whether the solutions in the query result are
	 *        ordered.
	 */
	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the end of a sequence of solutions.
	 */
	public void endQueryResult()
		throws TupleQueryResultHandlerException;

	/**
	 * Handles a solution.
	 */
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException;
}
