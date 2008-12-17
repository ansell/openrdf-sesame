/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.IteratorCursor;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.result.GraphResult;
import org.openrdf.store.StoreException;

/**
 * An utility implementation of the {@link GraphResult} interface.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public class GraphResultImpl extends ResultImpl<Statement> implements GraphResult {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> namespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GraphResultImpl(Cursor<? extends Statement> statements) {
		this(new HashMap<String, String>(0), statements);
	}

	public GraphResultImpl(Map<String, String> namespaces, Iterable<? extends Statement> statements) {
		this(namespaces, statements.iterator());
	}

	public GraphResultImpl(Map<String, String> namespaces, Iterator<? extends Statement> statementIter) {
		this(namespaces, new IteratorCursor<Statement>(statementIter));
	}

	public GraphResultImpl(Map<String, String> namespaces, Cursor<? extends Statement> statementIter) {
		super(statementIter);
		this.namespaces = Collections.unmodifiableMap(namespaces);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getNamespaces()
		throws StoreException
	{
		return namespaces;
	}

	public Model asModel()
		throws StoreException
	{
		return addTo(new ModelImpl(getNamespaces()));
	}
}
