/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * A RDFHandler that can be used to collect reported statements in collections.
 */
public class StatementCollector extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Collection<Statement> _statements;

	private Map<String, String> _namespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new StatementCollector that uses a new ArrayList to store the
	 * reported statements and a new LinkedHashMap to store the reported
	 * namespaces.
	 */
	public StatementCollector() {
		this(new ArrayList<Statement>());
	}

	/**
	 * Creates a new StatementCollector that stores reported statements in the
	 * supplied collection and that uses a new LinkedHashMap to store the
	 * reported namespaces.
	 */
	public StatementCollector(Collection<Statement> statements) {
		this(statements, new LinkedHashMap<String, String>());
	}

	/**
	 * Creates a new StatementCollector that stores reported statements and
	 * namespaces in the supplied containers.
	 */
	public StatementCollector(Collection<Statement> statements, Map<String, String> namespaces) {
		_statements = statements;
		_namespaces = namespaces;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Clear the set of collected statements.
	 */
	public void clear() {
		_statements.clear();
	}

	/**
	 * Gets the collection that contains the collected statements.
	 */
	public Collection<Statement> getStatements() {
		return _statements;
	}

	/**
	 * Gets the map that contains the collected namespaces.
	 */
	public Map<String, String> getNamespaces() {
		return _namespaces;
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		if (!_namespaces.containsKey(prefix)) {
			_namespaces.put(prefix, uri);
		}
	}

	@Override
	public void handleStatement(Statement st)
	{
		_statements.add(st);
	}
}
