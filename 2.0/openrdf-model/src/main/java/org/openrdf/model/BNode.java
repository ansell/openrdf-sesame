/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

/**
 * A blank node (aka <em>bnode</em>, aka <em>anonymous node</em>). A blank node
 * has an identifier to be able to compare it to other blank nodes internally.
 * Please note that, conceptually, blank node equality can only be determined by
 * examining the statements that refer to them.
 */
public interface BNode extends Resource {

	/**
	 * retrieves this blank node's identifier.
	 *
	 * @return A blank node identifier.
	 */
	public String getID();
	
	/**
	 * Compares a blank node object to another object.
	 *
	 * @param o The object to compare this blank node to.
	 * @return <tt>true</tt> if the other object is an instance of {@link BNode}
	 * and their IDs are equal, <tt>false</tt> otherwise.
	 */
	public boolean equals(Object o);
	
	/**
	 * The hash code of a blank node is defined as the hash code of its
	 * identifier: <tt>id.hashCode()</tt>.
	 * 
	 * @return A hash code for the blank node.
	 */
	public int hashCode();
}
