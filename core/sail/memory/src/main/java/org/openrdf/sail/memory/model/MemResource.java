/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
