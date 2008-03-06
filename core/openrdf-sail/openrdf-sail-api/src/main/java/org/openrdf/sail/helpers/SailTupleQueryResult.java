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
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;


public class SailTupleQueryResult implements TupleQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<String> _bindingNames;

	private CloseableIterator<Solution> _iterator;

	private boolean _distinct;

	private boolean _ordered;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailTupleQueryResult(Set<String> bindingNames, CloseableIterator<Solution> iterator) {
		_bindingNames = Collections.unmodifiableList(new ArrayList<String>(bindingNames));
		_iterator = iterator;
		_distinct = false;
		_ordered = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<String> getBindingNames() {
		return _bindingNames;
	}

	public void setOrdered(boolean ordered) {
		_ordered = ordered;
	}

	public boolean isOrdered() {
		return _ordered;
	}

	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public boolean isDistinct() {
		return _distinct;
	}

	public Solution nextSolution() {
		return _iterator.next();
	}

	public Iterator<Solution> iterator() {
		return _iterator;
	}

	public void close() {
		_iterator.close();
	}

	public boolean isEmpty() {
		return ! _iterator.hasNext();
	}
}
