/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

/**
 * A Connection object's transaction isolation level.
 * 
 * @author James Leigh
 */
public enum Isolation {

	/** Indicates that transactions are not supported. */
	NONE,

	/** Transactions are supported, but no isolation. */
	READ_UNCOMMITTED,

	/**
	 * In this isolation level only statements that have been committed (at some
	 * point) can be seen by the transaction.
	 */
	READ_COMMITTED,

	/**
	 * In addition to read committed, statements in this isolation level that are
	 * observed within a successful transaction will remain observable by the
	 * transaction until the end.
	 */
	REPEATABLE_READ,

	/**
	 * In addition to repeatable read, successful transactions in this isolation
	 * level will view a consistent snapshot. This isolation level will observe
	 * either the complete effects of other change-sets and their dependency or no
	 * effects of other change-sets.
	 */
	SNAPSHOT,

	/**
	 * In addition to snapshot, this isolation level requires that all other
	 * successful transactions must appear to occur either completely before or
	 * completely after a successful serializable transaction.
	 */
	SERIALIZABLE;
}
