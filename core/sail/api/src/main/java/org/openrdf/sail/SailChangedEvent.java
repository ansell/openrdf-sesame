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
