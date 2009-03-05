/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
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
	 * 
	 * @return a MemStatementList containing the statements.
	 */
	public MemStatementList getSubjectStatementList();

	/**
	 * Gets the number of statements for which this MemResource is the subject.
	 * 
	 * @return An integer larger than or equal to 0.
	 */
	public int getSubjectStatementCount();

	/**
	 * Adds a statement to this MemResource's list of statements for which it is
	 * the subject.
	 */
	public void addSubjectStatement(MemStatement st);

	/**
	 * Removes a statement from this MemResource's list of statements for which
	 * it is the subject.
	 */
	public void removeSubjectStatement(MemStatement st);

	/**
	 * Removes statements from old snapshots (those that have expired at or
	 * before the specified snapshot version) from this MemValue's list of
	 * statements for which it is the subject.
	 * 
	 * @param currentSnapshot
	 *        The current snapshot version.
	 */
	public void cleanSnapshotsFromSubjectStatements(int currentSnapshot);

	/**
	 * Gets the list of statements for which this MemResource represents the
	 * context.
	 * 
	 * @return a MemStatementList containing the statements.
	 */
	public MemStatementList getContextStatementList();

	/**
	 * Gets the number of statements for which this MemResource represents the
	 * context.
	 * 
	 * @return An integer larger than or equal to 0.
	 */
	public int getContextStatementCount();

	/**
	 * Adds a statement to this MemResource's list of statements for which it
	 * represents the context.
	 */
	public void addContextStatement(MemStatement st);

	/**
	 * Removes a statement from this MemResource's list of statements for which
	 * it represents the context.
	 */
	public void removeContextStatement(MemStatement st);

	/**
	 * Removes statements from old snapshots (those that have expired at or
	 * before the specified snapshot version) from this MemValue's list of
	 * statements for which it is the context.
	 * 
	 * @param currentSnapshot
	 *        The current snapshot version.
	 */
	public void cleanSnapshotsFromContextStatements(int currentSnapshot);
}
