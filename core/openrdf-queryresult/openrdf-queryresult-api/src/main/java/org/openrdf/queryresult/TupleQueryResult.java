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

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

/**
 * A representation of a variable-binding query result as a sequence of
 * {@link Solution} objects. Each query result consists of zero or more
 * solutions, each of which represents a single query solution as a set of
 * bindings. Note: TupleQueryResult implements Iterable but, unlike normal Collection
 * classes, <em>can only be iterated over once</em>; iterating over an
 * exhausted query result will not return any solutions. Take care to always
 * close a TupleQueryResult after use to free any resources it keeps hold of.
 * <p>
 * As TupleQueryResult implements Iterable, one can use Java's enhanced for loops to
 * conveniently iterate over all solutions, like so:
 * 
 * <pre>
 * String query = &quot;SELECT X, P, Y FROM {X} P {Y}&quot;;
 * TupleQueryResult result = connection.evaluateTupleQuery(QueryLanguage.SERQL, query);
 * 
 * for (Solution solution : result) {
 * 	Value valueOfX = solution.getValue(&quot;X&quot;);
 * 	System.out.println(valueOfX);
 * }
 * 
 * result.close();
 * </pre>
 */
public interface TupleQueryResult extends Iterable<Solution>, Closeable {
	
	/**
	 * Gets the names of the bindings, in order of projection.
	 * 
	 * @return The binding names, in order of projection.
	 */
	public List<String> getBindingNames();

	/**
	 * Returns the next solution in the sequence.
	 * 
	 * @return the next solution.
	 */
	public Solution nextSolution();

	/**
	 * Creates an iterator over the solutions of this query result. Note that,
	 * unlike normal Collection classes, <em>a query result can only be iterated
	 * over once</em>.
	 */
	public Iterator<Solution> iterator();

	/**
	 * Closes the query result and frees any resources (such as open connections)
	 * it keeps hold of.
	 */
	public void close();

	/**
	 * Checks if the result is ordered.
	 * 
	 * @return true if the result is ordered, false otherwise.
	 */
	public boolean isOrdered();

	/**
	 * Checks if the result is guaranteed to contain no duplicate solutions.
	 * 
	 * @return true if the result is guaranteed to contain no duplicate
	 *         solutions, false otherwise.
	 */
	public boolean isDistinct();

	/**
	 * Checks if the result has any solutions.
	 * 
	 * @return true if the result has zero solutions, false otherwise.
	 */
	public boolean isEmpty();
}
