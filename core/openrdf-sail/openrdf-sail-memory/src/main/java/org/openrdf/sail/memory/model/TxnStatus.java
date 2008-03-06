/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

/**
 * A type-safe enumeration for transaction status information on
 * MemStatements.
 */
public enum TxnStatus {

	/**
	 * Constant indicating that a statement has not been affected by a
	 * transaction.
	 */
	NEUTRAL,

	/**
	 * Constant indicating that a statement has been newly added as part of a
	 * transaction, but has not yet been committed. Such statements should not
	 * be queried to prevent 'dirty reads'.
	 */
	NEW,

	/**
	 * Constant indicating that an existing statement has been deprecated and
	 * should be removed upon commit.
	 */
	DEPRECATED,
	
	/**
	 * Constant indicating that an existing inferred statement has been added
	 * explicitly as part of a transaction and that it should be marked as such
	 * upon commit.
	 */
	EXPLICIT,
	
	/**
	 * Constant indicating that an existing explicit statement has been removed
	 * as part of a transaction, but that it can still be inferred from the
	 * other statements.
	 */
	INFERRED,

	/**
	 * Constant indicating that a statement was added and then removed in a
	 * single transaction. The statement should be removed upon commit.
	 */
	ZOMBIE;
}
