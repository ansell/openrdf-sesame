/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory.model;

import org.eclipse.rdf4j.model.Resource;

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
