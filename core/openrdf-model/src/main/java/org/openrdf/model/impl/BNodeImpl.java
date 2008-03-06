/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.BNode;

/**
 * An implementation of the {@link BNode} interface.
 */
public class BNodeImpl implements BNode {

	/*-----------*
	 * Variables *
	 *-----------*/
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5273570771022125970L;
	
	/**
	 * The blank node's identifier.
	 */
	private final String _id;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new blank node with the supplied identifier.
	 * 
	 * @param id The identifier for this blank node, must not be <tt>null</tt>.
	 */
	public BNodeImpl(String id) {
		assert id != null;
		
		_id = id;
	}

	/*---------*
	 * Methods *
	 *---------*/
	
	// Implements BNode.getID()
	public String getID() {
		return _id;
	}

	// Overrides Object.equals(Object), implements BNode.equals(Object)
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
	public int hashCode() {
		return _id.hashCode();
	}

	// Overrides Object.toString()
	public String toString() {
		return _id;
	}
}
