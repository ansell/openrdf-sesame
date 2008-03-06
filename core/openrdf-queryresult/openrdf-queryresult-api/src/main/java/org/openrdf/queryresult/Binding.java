/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult;

import org.openrdf.model.Value;

/**
 * A named value binding.
 */
public interface Binding {

	/**
	 * Gets the name of the binding (e.g. the variable name).
	 * 
	 * @return The name of the binding.
	 */
	public String getName();

	/**
	 * Gets the value of the binding. The returned value is never equal to
	 * <tt>null</tt>, such a "binding" is considered to be unbound.
	 * 
	 * @return The value of the binding, never <tt>null</tt>.
	 */
	public Value getValue();

	/**
	 * Compares a binding object to another object.
	 * 
	 * @param o
	 *        The object to compare this binding to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Binding} and both their names and values are equal,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean equals(Object o);

	/**
	 * The hash code of a binding is defined as the bit-wise XOR of the hash
	 * codes of its name and value:
	 * 
	 * <pre>
	 * name.hashCode() &circ; value.hashCode()
	 * </pre>.
	 * 
	 * @return A hash code for the binding.
	 */
	public int hashCode();
}
