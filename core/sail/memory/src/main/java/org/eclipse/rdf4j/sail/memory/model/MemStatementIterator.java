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

import org.eclipse.rdf4j.common.iteration.LookAheadIteration;
import org.eclipse.rdf4j.common.lang.ObjectUtil;

/**
 * A StatementIterator that can iterate over a list of Statement objects. This
 * iterator compares Resource and Literal objects using the '==' operator, which
 * is possible thanks to the extensive sharing of these objects in the
 * MemoryStore.
 */
public class MemStatementIterator<X extends Exception> extends LookAheadIteration<MemStatement, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lists of statements over which to iterate.
	 */
	private final MemStatementList statementList;

	/**
	 * The subject of statements to return, or null if any subject is OK.
	 */
	private final MemResource subject;

	/**
	 * The predicate of statements to return, or null if any predicate is OK.
	 */
	private final MemIRI predicate;

	/**
	 * The object of statements to return, or null if any object is OK.
	 */
	private final MemValue object;

	/**
	 * The context of statements to return, or null if any context is OK.
	 */
	private final MemResource[] contexts;

	/**
	 * Flag indicating whether this iterator should only return explicitly added
	 * statements or only return inferred statements.
	 */
	private final Boolean explicit;

	/**
	 * Indicates which snapshot should be iterated over.
	 */
	private final int snapshot;

	/**
	 * The index of the last statement that has been returned.
	 */
	private volatile int statementIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemStatementIterator that will iterate over the statements
	 * contained in the supplied MemStatementList searching for statements that
	 * match the specified pattern of subject, predicate, object and context(s).
	 * 
	 * @param statementList
	 *        the statements over which to iterate.
	 * @param subject
	 *        subject of pattern.
	 * @param predicate
	 *        predicate of pattern.
	 * @param object
	 *        object of pattern.
	 * @param contexts
	 *        context(s) of pattern.
	 */
	public MemStatementIterator(MemStatementList statementList, MemResource subject, MemIRI predicate,
			MemValue object, Boolean explicit, int snapshot, MemResource... contexts)
	{
		this.statementList = statementList;
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.contexts = contexts;
		this.explicit = explicit;
		this.snapshot = snapshot;

		this.statementIdx = -1;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Searches through statementList, starting from index
	 * <tt>_nextStatementIdx + 1</tt>, for statements that match the constraints
	 * that have been set for this iterator. If a matching statement has been
	 * found it will be stored in <tt>_nextStatement</tt> and
	 * <tt>_nextStatementIdx</tt> points to the index of this statement in
	 * <tt>_statementList</tt>. Otherwise, <tt>_nextStatement</tt> will set to
	 * <tt>null</tt>.
	 */
	protected MemStatement getNextElement() {
		statementIdx++;

		for (; statementIdx < statementList.size(); statementIdx++) {
			MemStatement st = statementList.get(statementIdx);

			if (isInSnapshot(st) && (subject == null || subject == st.getSubject())
					&& (predicate == null || predicate == st.getPredicate())
					&& (object == null || object == st.getObject()))
			{
				// A matching statement has been found, check if it should be
				// skipped due to explicitOnly, contexts and readMode requirements

				if (contexts != null && contexts.length > 0) {
					boolean matchingContext = false;
					for (int i = 0; i < contexts.length && !matchingContext; i++) {
						matchingContext = ObjectUtil.nullEquals(st.getContext(), contexts[i]);
					}
					if (!matchingContext) {
						// statement does not appear in one of the specified contexts,
						// skip it.
						continue;
					}
				}

				if (explicit != null && explicit.booleanValue() != st.isExplicit()) {
					// Explicit flag does not match
					continue;
				}

				return st;
			}
		}

		// No more matching statements.
		return null;
	}

	private boolean isInSnapshot(MemStatement st) {
		return snapshot < 0 || st.isInSnapshot(snapshot);
	}
}
