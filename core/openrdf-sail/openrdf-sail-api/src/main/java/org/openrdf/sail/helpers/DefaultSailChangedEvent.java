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
	 * Variables *
	 *-----------*/

	private Sail _sail;

	private boolean _statementsAdded;

	private boolean _statementsRemoved;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DefaultSailChangedEvent in which all possible changes are
	 * set to false.
	 */
	public DefaultSailChangedEvent(Sail sail) {
		_sail = sail;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements SailChangedEvent.getSail()
	public Sail getSail() {
		return _sail;
	}

	// Implements SailChangedEvent.statementsAdded()
	public boolean statementsAdded() {
		return _statementsAdded;
	}

	public void setStatementsAdded(boolean statementsAdded) {
		_statementsAdded = statementsAdded;
	}

	// Implements SailChangedEvent.statementsRemoved()
	public boolean statementsRemoved() {
		return _statementsRemoved;
	}

	public void setStatementsRemoved(boolean statementsRemoved) {
		_statementsRemoved = statementsRemoved;
	}
}
