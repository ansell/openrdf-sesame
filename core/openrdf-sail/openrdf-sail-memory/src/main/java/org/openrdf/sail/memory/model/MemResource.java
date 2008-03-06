/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.Resource;

/**
 * A MemoryStore-specific extension of Resource giving it subject statements.
 */
public interface MemResource extends MemValue, Resource {

	/**
	 * Gets the list of statements for which this MemResource is the subject.
	 * @return a MemStatementList containing the statements.
	 */
	public MemStatementList getSubjectStatementList();

	/**
	 * Gets the number of statements for which this MemResource is the subject.
	 * @return An integer larger than or equal to 0.
	 */
	public int getSubjectStatementCount();

	/**
	 * Adds a statement to this MemResource's list of statements for which it
	 * is the subject.
	 */
	public void addSubjectStatement(MemStatement st);

	/**
	 * Removes a statement from this MemResource's list of statements for which
	 * it is the subject.
	 */
	public void removeSubjectStatement(MemStatement st);

	/**
	 * Gets the list of statements for which this MemResource represents the
	 * context.
	 * @return a MemStatementList containing the statements.
	 */
	public MemStatementList getContextStatementList();

	/**
	 * Gets the number of statements for which this MemResource represents the
	 * context.
	 * @return An integer larger than or equal to 0.
	 */
	public int getContextStatementCount();

	/**
	 * Adds a statement to this MemResource's list of statements for which
	 * it represents the context.
	 */
	public void addContextStatement(MemStatement st);

	/**
	 * Removes a statement from this MemResource's list of statements for which
	 * it represents the context.
	 */
	public void removeContextStatement(MemStatement st);
}
