/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;

/**
 * Default implementation of the SailChangedEvent interface.
 */
public class DefaultSailChangedEvent implements SailChangedEvent {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Sail sail;

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean statementsAdded;

	private boolean statementsRemoved;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DefaultSailChangedEvent in which all possible changes are
	 * set to false.
	 */
	public DefaultSailChangedEvent(Sail sail) {
		this.sail = sail;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Sail getSail() {
		return sail;
	}

	public boolean statementsAdded() {
		return statementsAdded;
	}

	public void setStatementsAdded(boolean statementsAdded) {
		this.statementsAdded = statementsAdded;
	}

	public boolean statementsRemoved() {
		return statementsRemoved;
	}

	public void setStatementsRemoved(boolean statementsRemoved) {
		this.statementsRemoved = statementsRemoved;
	}
}
