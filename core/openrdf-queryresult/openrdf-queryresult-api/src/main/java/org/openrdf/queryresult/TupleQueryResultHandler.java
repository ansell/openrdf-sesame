/*  Copyright (C) 2001-2006 Aduna (http://www.aduna-software.com/)
 *
 *  This software is part of the Sesame Framework. It is licensed under
 *  the following two licenses as alternatives:
 *
 *   1. Open Software License (OSL) v3.0
 *   2. GNU Lesser General Public License (LGPL) v2.1 or any newer
 *      version
 *
 *  By using, modifying or distributing this software you agree to be 
 *  bound by the terms of at least one of the above licenses.
 *
 *  See the file LICENSE.txt that is distributed with this software
 *  for the complete terms and further details.
 */
package org.openrdf.queryresult;

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
	public void handleSolution(Solution solution)
		throws TupleQueryResultHandlerException;
}
