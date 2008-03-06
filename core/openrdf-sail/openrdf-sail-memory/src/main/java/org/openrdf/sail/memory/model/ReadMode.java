/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

/**
 * A type-safe enumeration for read modes for MemStatementIterator.
 *
 * @see MemStatementIterator
 */
public enum ReadMode  {
	
	/**
	 * Constant indicating that only committed statements should be read.
	 * Statements that have been added but not yet committed will be skipped.
	 */
	COMMITTED, 
	
	/**
	 * Constant indicating that statements should be treated as if the currently
	 * active transaction has been committed. Statements that have been
	 * scheduled for removal will be skipped.
	 */
	TRANSACTION, 
	
	/**
	 * Constant indicating that statements should be read no matter what their
	 * transaction status is.
	 */
	RAW;
}

