/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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

	@Override
	public Sail getSail() {
		return sail;
	}

	@Override
	public boolean statementsAdded() {
		return statementsAdded;
	}

	public void setStatementsAdded(boolean statementsAdded) {
		this.statementsAdded = statementsAdded;
	}

	@Override
	public boolean statementsRemoved() {
		return statementsRemoved;
	}

	public void setStatementsRemoved(boolean statementsRemoved) {
		this.statementsRemoved = statementsRemoved;
	}
}
