/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.query.Cursor;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.base.CursorWrapper;
import org.openrdf.store.StoreException;

/**
 * An utility implementation of the {@link GraphQueryResult} interface.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class GraphQueryResultImpl extends CursorWrapper<Statement> implements
		GraphQueryResult
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> namespaces;

	private Statement next;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GraphQueryResultImpl(Map<String, String> namespaces, Iterable<? extends Statement> statements) {
		this(namespaces, statements.iterator());
	}

	public GraphQueryResultImpl(Map<String, String> namespaces, Iterator<? extends Statement> statementIter) {
		this(namespaces, new IteratorCursor<Statement>(statementIter));
	}

	public GraphQueryResultImpl(Map<String, String> namespaces,
			Cursor<? extends Statement> statementIter)
	{
		super(statementIter);
		this.namespaces = Collections.unmodifiableMap(namespaces);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	@Override
	public Statement next()
		throws StoreException
	{
		Statement result = next;
		if (result == null)
			return super.next();
		next = null;
		return result;
	}

	public boolean hasNext()
		throws StoreException
	{
		return next != null || (next = next()) != null;
	}

	public <C extends Collection<? super Statement>> C addTo(C collection)
		throws StoreException
	{
		Statement st;
		while ((st = next()) != null) {
			collection.add(st);
		}
		return collection;
	}

	public List<Statement> asList()
		throws StoreException
	{
		return addTo(new ArrayList<Statement>());
	}

	public Set<Statement> asSet()
		throws StoreException
	{
		return addTo(new HashSet<Statement>());
	}
}
