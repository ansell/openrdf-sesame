/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.NoSuchElementException;

import info.aduna.iteration.CloseableIterationBase;
import info.aduna.lang.ObjectUtil;

/**
 * A StatementIterator that can iterate over a list of Statement objects. This
 * iterator compares Resource and Literal objects using the '==' operator, which
 * is possible thanks to the extensive sharing of these objects in the
 * MemoryStore.
 */
public class MemStatementIterator<X extends Exception> extends CloseableIterationBase<MemStatement, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lists of statements over which to iterate.
	 */
	private MemStatementList statementList;

	/**
	 * The subject of statements to return, or null if any subject is OK.
	 */
	private MemResource subject;

	/**
	 * The predicate of statements to return, or null if any predicate is OK.
	 */
	private MemURI predicate;

	/**
	 * The object of statements to return, or null if any object is OK.
	 */
	private MemValue object;

	/**
	 * The context of statements to return, or null if any context is OK.
	 */
	private MemResource[] contexts;

	/**
	 * Flag indicating whether this iterator should only return explicitly added
	 * statements.
	 */
	private boolean explicitOnly;

	/**
	 * Indicates which snapshot should be iterated over.
	 */
	private int snapshot;

	/**
	 * Flag indicating whether or not the iterator should read any non-committed
	 * changes to the data.
	 */
	private ReadMode readMode;

	/**
	 * The statement to return next.
	 */
	private MemStatement nextStatement;

	/**
	 * The index of the next statement to return.
	 */
	private int nextStatementIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemStatementIterator that will iterate over the statements
	 * contained in the supplied MemStatementList searching for statements that
	 * match the specified pattern of subject, predicate, object and context.
	 * 
	 * @param statementList
	 *        the statements over which to iterate.
	 * @param subject
	 *        subject of pattern.
	 * @param predicate
	 *        predicate of pattern.
	 * @param object
	 *        object of pattern.
	 * @param context
	 *        context of pattern.
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

		this.nextStatementIdx = -1;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Searches through statementList, starting from index
	 * <tt>_nextStatementIdx + 1</tt>, for statements that match the
	 * constraints that have been set for this iterator. If a matching statement
	 * has been found it will be stored in <tt>_nextStatement</tt> and
	 * <tt>_nextStatementIdx</tt> points to the index of this statement in
	 * <tt>_statementList</tt>. Otherwise, <tt>_nextStatement</tt> will set
	 * to <tt>null</tt>.
	 */
	private void findNextStatement() {
		nextStatementIdx++;

		for (; nextStatementIdx < statementList.size(); nextStatementIdx++) {
			nextStatement = statementList.get(nextStatementIdx);

			if (nextStatement.isInSnapshot(snapshot)
					&& (subject == null || subject == nextStatement.getSubject())
					&& (predicate == null || predicate == nextStatement.getPredicate())
					&& (object == null || object == nextStatement.getObject()))
			{
				// A matching statement has been found, check if it should be
				// skipped due to explicitOnly, contexts and readMode requirements

				if (contexts != null && contexts.length > 0) {
					boolean matchingContext = false;
					for (int i = 0; i < contexts.length && !matchingContext; i++) {
						matchingContext = ObjectUtil.nullEquals(nextStatement.getContext(), contexts[i]);
					}
					if (!matchingContext) {
						// statement does not appear in one of the specified contexts,
						// skip it.
						continue;
					}
				}

				if (ReadMode.COMMITTED.equals(readMode)) {
					// Only read committed statements

					if (nextStatement.getTxnStatus() == TxnStatus.NEW) {
						// Uncommitted statements, skip it
						continue;
					}
					if (explicitOnly && !nextStatement.isExplicit()) {
						// Explicit statements only; skip inferred ones
						continue;
					}
				}
				else if (ReadMode.TRANSACTION.equals(readMode)) {
					// Pretend that the transaction has already been committed

					TxnStatus txnStatus = nextStatement.getTxnStatus();

					if (TxnStatus.DEPRECATED.equals(txnStatus) || TxnStatus.ZOMBIE.equals(txnStatus)) {
						// Statement scheduled for removal, skip it
						continue;
					}

					if (explicitOnly) {
						if (!nextStatement.isExplicit() && !TxnStatus.EXPLICIT.equals(txnStatus)
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

					if (explicitOnly && !nextStatement.isExplicit()) {
						// Explicit statements only; skip inferred ones
						continue;
					}
				}

				return;
			}
		}

		// No more matching statements.
		nextStatement = null;
	}

	public boolean hasNext() {
		if (nextStatement == null && statementList != null && nextStatementIdx < statementList.size()) {
			findNextStatement();
		}

		return nextStatement != null;
	}

	public MemStatement next() {
		if (statementList == null) {
			throw new NoSuchElementException("Iterator has been closed");
		}
		if (nextStatement == null && nextStatementIdx < statementList.size()) {
			findNextStatement();
		}
		if (nextStatement == null) {
			throw new NoSuchElementException("No more statements");
		}

		MemStatement result = nextStatement;
		nextStatement = null;
		return result;
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws X
	{
		nextStatement = null;
		statementList = null;

		subject = null;
		predicate = null;
		object = null;
		contexts = null;

		super.handleClose();
	}
}
