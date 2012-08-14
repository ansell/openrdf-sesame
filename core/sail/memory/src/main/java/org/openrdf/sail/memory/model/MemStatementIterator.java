/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import info.aduna.iteration.LookAheadIteration;
import info.aduna.lang.ObjectUtil;

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
	private final MemURI predicate;

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
	 * statements.
	 */
	private final boolean explicitOnly;

	/**
	 * Indicates which snapshot should be iterated over.
	 */
	private final int snapshot;

	/**
	 * Flag indicating whether or not the iterator should read any non-committed
	 * changes to the data.
	 */
	private final ReadMode readMode;

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
	public MemStatementIterator(MemStatementList statementList, MemResource subject, MemURI predicate,
			MemValue object, boolean explicitOnly, int snapshot, ReadMode readMode, MemResource... contexts)
	{
		this.statementList = statementList;
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.contexts = contexts;
		this.explicitOnly = explicitOnly;
		this.snapshot = snapshot;
		this.readMode = readMode;

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

			if (st.isInSnapshot(snapshot) && (subject == null || subject == st.getSubject())
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

				if (ReadMode.COMMITTED.equals(readMode)) {
					// Only read committed statements

					if (st.getTxnStatus() == TxnStatus.NEW) {
						// Uncommitted statements, skip it
						continue;
					}
					if (explicitOnly && !st.isExplicit()) {
						// Explicit statements only; skip inferred ones
						continue;
					}
				}
				else if (ReadMode.TRANSACTION.equals(readMode)) {
					// Pretend that the transaction has already been committed

					TxnStatus txnStatus = st.getTxnStatus();

					if (TxnStatus.DEPRECATED.equals(txnStatus) || TxnStatus.ZOMBIE.equals(txnStatus)) {
						// Statement scheduled for removal, skip it
						continue;
					}

					if (explicitOnly) {
						if (!st.isExplicit() && !TxnStatus.EXPLICIT.equals(txnStatus)
								|| TxnStatus.INFERRED.equals(txnStatus))
						{
							// Explicit statements only; skip inferred ones
							continue;
						}
					}
				}
				else if (ReadMode.RAW.equals(readMode)) {
					// Ignore the statement's transaction status, only check the
					// explicitOnly requirement

					if (explicitOnly && !st.isExplicit()) {
						// Explicit statements only; skip inferred ones
						continue;
					}
				}

				return st;
			}
		}

		// No more matching statements.
		return null;
	}
}
