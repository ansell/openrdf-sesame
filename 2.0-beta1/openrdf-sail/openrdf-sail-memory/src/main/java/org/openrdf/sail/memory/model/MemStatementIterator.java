/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.NoSuchElementException;

import info.aduna.iteration.CloseableIterationBase;

import org.openrdf.model.Resource;

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
	private MemStatementList _statementList;

	/**
	 * The subject of statements to return, or null if any subject is OK.
	 */
	private MemResource _subject;

	/**
	 * The predicate of statements to return, or null if any predicate is OK.
	 */
	private MemURI _predicate;

	/**
	 * The object of statements to return, or null if any object is OK.
	 */
	private MemValue _object;

	/**
	 * The context of statements to return, or null if any context is OK.
	 */
	private MemResource[] _contexts;

	/**
	 * Flag indicating whether this iterator should only return explicitly added
	 * statements.
	 */
	private boolean _explicitOnly;

	/**
	 * Flag indicating whether or not the iterator should read any non-committed
	 * changes to the data.
	 */
	private ReadMode _readMode;

	/**
	 * The statement to return next.
	 */
	private MemStatement _nextStatement;

	/**
	 * The index of the next statement to return.
	 */
	private int _nextStatementIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a MemStatementIterator that iterates over all statements in the
	 * supplied list.
	 * 
	 * @param statementList
	 *        the statements over which to iterate.
	 */
	public MemStatementIterator(MemStatementList statementList) {
		this(statementList, null, null, null, false, ReadMode.COMMITTED);
	}

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
			MemValue object, boolean explicitOnly, ReadMode readMode, MemResource... contexts)
	{
		_statementList = statementList;
		_subject = subject;
		_predicate = predicate;
		_object = object;
		_contexts = contexts;
		_explicitOnly = explicitOnly;
		_readMode = readMode;

		_nextStatementIdx = -1;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Searches through _statementList, starting from index
	 * <tt>_nextStatementIdx + 1</tt>, for statements that match the
	 * constraints that have been set for this iterator. If a matching statement
	 * has been found it will be stored in <tt>_nextStatement</tt> and
	 * <tt>_nextStatementIdx</tt> points to the index of this statement in
	 * <tt>_statementList</tt>. Otherwise, <tt>_nextStatement</tt> will set
	 * to <tt>null</tt>.
	 */
	private void _findNextStatement() {
		_nextStatementIdx++;

		for (; _nextStatementIdx < _statementList.size(); _nextStatementIdx++) {
			_nextStatement = _statementList.get(_nextStatementIdx);

			if ((_subject == null || _subject == _nextStatement.getSubject())
					&& (_predicate == null || _predicate == _nextStatement.getPredicate())
					&& (_object == null || _object == _nextStatement.getObject()))
			{
				// A matching statement has been found, check if it should be
				// skipped
				// due to _explicitOnly, _contexts and _readMode requirements

				if (_contexts != null && _contexts.length > 0) {
					boolean matchingContext = false;
					Resource nsContext = _nextStatement.getContext();
					for (MemResource context : _contexts) {
						if (nsContext == null) {
							if (context == null) {
								matchingContext = true;
								break;
							}
						}
						else if (nsContext.equals(context)) {
							matchingContext = true;
							break;
						}
					}
					if (!matchingContext) {
						// statement does not appear in one of the specified contexts,
						// skip it.
						continue;
					}
				}

				if (ReadMode.COMMITTED.equals(_readMode)) {
					// Only read committed statements

					if (_nextStatement.getTxnStatus() == TxnStatus.NEW) {
						// Uncommitted statements, skip it
						continue;
					}
					if (_explicitOnly && !_nextStatement.isExplicit()) {
						// Explicit statements only; skip inferred ones
						continue;
					}
				}
				else if (ReadMode.TRANSACTION.equals(_readMode)) {
					// Pretend that the transaction has already been committed

					TxnStatus txnStatus = _nextStatement.getTxnStatus();

					if (TxnStatus.DEPRECATED.equals(txnStatus) || TxnStatus.ZOMBIE.equals(txnStatus)) {
						// Statement scheduled for removal, skip it
						continue;
					}

					if (_explicitOnly) {
						if (!_nextStatement.isExplicit() && (!TxnStatus.EXPLICIT.equals(txnStatus))
								|| TxnStatus.INFERRED.equals(txnStatus))
						{
							// Explicit statements only; skip inferred ones
							continue;
						}
					}
				}
				else if (ReadMode.RAW.equals(_readMode)) {
					// Ignore the statement's transaction status, only check the
					// _explicitOnly requirement

					if (_explicitOnly && !_nextStatement.isExplicit()) {
						// Explicit statements only; skip inferred ones
						continue;
					}
				}

				return;
			}
		}

		// No more matching statements.
		_nextStatement = null;
	}

	// Implements Iterator.hasNext()
	public boolean hasNext() {
		if (_nextStatement == null && _statementList != null && _nextStatementIdx < _statementList.size()) {
			_findNextStatement();
		}

		return _nextStatement != null;
	}

	// Implements Iterator.next()
	public MemStatement next() {
		if (_statementList == null) {
			throw new IllegalStateException("Iterator has been closed");
		}
		if (_nextStatement == null && _nextStatementIdx < _statementList.size()) {
			_findNextStatement();
		}
		if (_nextStatement == null) {
			throw new NoSuchElementException("No more statements");
		}

		MemStatement nextStatement = _nextStatement;
		_nextStatement = null;

		return nextStatement;
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	// Implements StatementIterator.close()
	public void close()
		throws X
	{
		_nextStatement = null;
		_statementList = null;

		_subject = null;
		_predicate = null;
		_object = null;
		_contexts = null;

		super.close();
	}
}
