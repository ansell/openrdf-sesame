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

import org.openrdf.model.Statement;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.util.iterator.CloseableIterator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SailGraphQueryResult implements GraphQueryResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private CloseableIterator<Statement> _iterator;

	private Map<String, String> _namespaces;

	private boolean _distinct;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailGraphQueryResult(Map<String, String> namespaces, CloseableIterator<Statement> iterator) {
		_iterator = iterator;
		_distinct = false;
		if (namespaces == null) {
			_namespaces = Collections.emptyMap();
		}
		else {
			_namespaces = Collections.unmodifiableMap(new HashMap<String, String>(namespaces));
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setDistinct(boolean distinct) {
		_distinct = distinct;
	}

	public boolean isDistinct() {
		return _distinct;
	}

	public CloseableIterator<Statement> iterator() {
		return _iterator;
	}

	public void close() {
		_iterator.close();
	}

	public boolean isEmpty() {
		return !_iterator.hasNext();
	}

	public Map<String, String> getNamespaces() {
		return _namespaces;
	}

	public Statement nextStatement() {
		return _iterator.next();
	}
}
