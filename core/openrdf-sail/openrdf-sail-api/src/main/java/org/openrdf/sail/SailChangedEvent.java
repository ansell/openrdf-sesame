/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * Event object that is send to {@link SailChangedListener}s to indicate that
 * the contents of the Sail that sent the event have changed.
 */
public interface SailChangedEvent {

	/**
	 * The Sail object that sent this event.
	 */
	public Sail getSail();

	/**
	 * Indicates if statements were added to the Sail.
	 *
	 * @return <tt>true</tt> if statements were added during a transaction,
	 * <tt>false</tt> otherwise.
	 */
	public boolean statementsAdded();

	/**
	 * Indicates if statements were removed from the Sail.
	 *
	 * @return <tt>true</tt> if statements were removed during a transaction,
	 * <tt>false</tt> otherwise.
	 */
	public boolean statementsRemoved();

}
