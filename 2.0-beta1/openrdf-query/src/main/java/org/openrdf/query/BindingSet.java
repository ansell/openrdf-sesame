/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Iterator;

import org.openrdf.model.Value;

/**
 * A BindingSet is a a set of named value bindings, which is used a.o. to
 * represent a single query solution. Values are indexed by name of the binding
 * which typically corresponds to the names of the variables used in the
 * projection of the orginal query.
 */
public interface BindingSet extends Iterable<Binding> {

	/**
	 * Creates an iterator over the bindings in this BindingSet. This only returns
	 * bindings with non-null values. An implementation is free to return the
	 * bindings in arbitrary order.
	 */
	public Iterator<Binding> iterator();

	/**
	 * Gets the binding with the specified name from this BindingSet.
	 * 
	 * @param bindingName
	 *        The name of the binding.
	 * @return The binding with the specified name, or <tt>null</tt> if there
	 *         is no such binding in this BindingSet.
	 */
	public Binding getBinding(String bindingName);

	/**
	 * Checks whether this BindingSet has a binding with the specified name.
	 * 
	 * @param bindingName
	 *        The name of the binding.
	 * @return <tt>true</tt> if this BindingSet has a binding with the specified
	 *         name, <tt>false</tt> otherwise.
	 */
	public boolean hasBinding(String bindingName);

	/**
	 * Gets the value of the binding with the specified name from this BindingSet.
	 * 
	 * @param bindingName
	 *        The name of the binding.
	 * @return The value of the binding with the specified name, or <tt>null</tt>
	 *         if there is no such binding in this BindingSet.
	 */
	public Value getValue(String bindingName);

	/**
	 * Returns the number of bindings in this BindingSet.
	 * 
	 * @return The number of bindings in this BindingSet.
	 */
	public int size();

	/**
	 * Compares a BindingSet object to another object.
	 * 
	 * @param o
	 *        The object to compare this binding to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link BindingSet} and it contains the same set of bindings
	 *         (disregarding order), <tt>false</tt> otherwise.
	 */
	public boolean equals(Object o);

	/**
	 * The hash code of a binding is defined as the bit-wise XOR of the hash
	 * codes of its bindings:
	 * 
	 * <pre>
	 * int hashCode = 0;
	 * for (Binding binding : this) {
	 * 	hashCode &circ;= binding.hashCode();
	 * }
	 * </pre>
	 * 
	 * Note: the calculated hash code intentionally does not dependent on the
	 * order in which the bindings are iterated over.
	 * 
	 * @return A hash code for the BindingSet.
	 */
	public int hashCode();
}
