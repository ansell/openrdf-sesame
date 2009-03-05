/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;

/**
 * A RDFHandler that can be used to collect reported statements in collections.
 * 
 * @author Arjohn Kampman
 */
public class StatementCollector extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Collection<Statement> statements;

	private Map<String, String> namespaces;

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
		this.statements = statements;
		this.namespaces = namespaces;
	}

	/**
	 * Creates a new StatementCollector that stores reported statements and
	 * namespaces in the supplied Model.
	 */
	public StatementCollector(Model model) {
		this.statements = model;
		this.namespaces = model.getNamespaces();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Clear the set of collected statements.
	 */
	public void clear() {
		statements.clear();
	}

	/**
	 * Gets the collection that contains the collected statements.
	 */
	public Collection<Statement> getStatements() {
		return statements;
	}

	/**
	 * Gets the map that contains the collected namespaces.
	 */
	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	public Model getModel() {
		return new LinkedHashModel(namespaces, statements);
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		if (!namespaces.containsKey(prefix)) {
			namespaces.put(prefix, uri);
		}
	}

	@Override
	public void handleStatement(Statement st) {
		statements.add(st);
	}

	public boolean isEmpty() {
		return statements.isEmpty();
	}

	public int size() {
		return statements.size();
	}
}
