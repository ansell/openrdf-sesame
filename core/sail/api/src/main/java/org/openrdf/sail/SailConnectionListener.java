/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.model.Statement;

public interface SailConnectionListener {

	/**
	 * Notifies the listener that a statement has been added in a transaction
	 * that it has registered itself with.
	 * 
	 * @param st
	 *        The statement that was added.
	 */
	public void statementAdded(Statement st);

	/**
	 * Notifies the listener that a statement has been removed in a transaction
	 * that it has registered itself with.
	 * 
	 * @param st
	 *        The statement that was removed.
	 */
	public void statementRemoved(Statement st);
}
