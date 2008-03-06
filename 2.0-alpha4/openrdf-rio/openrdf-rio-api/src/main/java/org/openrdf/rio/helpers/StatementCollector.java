/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.model.Statement;

/**
 * A RDFHandler that can be used to collect reported statements in
 * collections.
 */
public class StatementCollector extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Collection<Statement> _statements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new StatementCollector that uses a List for storing the
	 * reported statements.
	 */
	public StatementCollector() {
		this(new ArrayList<Statement>());
	}

	/**
	 * Creates a new StatementCollector that stores reported statements in the
	 * supplied collection.
	 */
	public StatementCollector(Collection<Statement> statements) {
		_statements = statements;
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

	// Implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st) {
		_statements.add(st);
	}
}
