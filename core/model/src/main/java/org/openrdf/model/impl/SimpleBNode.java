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
package org.openrdf.model.impl;

import org.openrdf.model.BNode;

/**
 * An simple default implementation of the {@link BNode} interface.
 * 
 * @author Arjohn Kampman
 */
public class SimpleBNode implements BNode {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 5273570771022125970L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The blank node's identifier.
	 */
	private String id;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new, unitialized blank node. This blank node's ID needs to be
	 * {@link #setID(String) set} before the normal methods can be used.
	 */
	protected SimpleBNode() {
	}

	/**
	 * Creates a new blank node with the supplied identifier.
	 * 
	 * @param id
	 *        The identifier for this blank node, must not be <tt>null</tt>.
	 */
	protected SimpleBNode(String id) {
		this();
		setID(id);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getID() {
		return id;
	}

	protected void setID(String id) {
		this.id = id;
	}

	public String stringValue() {
		return id;
	}

	// Overrides Object.equals(Object), implements BNode.equals(Object)
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof BNode) {
			BNode otherNode = (BNode)o;
			return this.getID().equals(otherNode.getID());
		}

		return false;
	}

	// Overrides Object.hashCode(), implements BNode.hashCode()
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	// Overrides Object.toString()
	@Override
	public String toString() {
		return "_:" + id;
	}
}
