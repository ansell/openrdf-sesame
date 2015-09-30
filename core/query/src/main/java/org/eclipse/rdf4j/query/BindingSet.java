/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.rdf4j.model.Value;

/**
 * A BindingSet is a set of named value bindings, which is used a.o. to
 * represent a single query solution. Values are indexed by name of the binding
 * which typically corresponds to the names of the variables used in the
 * projection of the orginal query.
 */
public interface BindingSet extends Iterable<Binding>, Serializable {

	/**
	 * Creates an iterator over the bindings in this BindingSet. This only
	 * returns bindings with non-null values. An implementation is free to return
	 * the bindings in arbitrary order.
	 */
	public Iterator<Binding> iterator();

	/**
	 * Gets the names of the bindings in this BindingSet.
	 * 
	 * @return A set of binding names.
	 */
	public Set<String> getBindingNames();

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
	 * @return <tt>true</tt> if this BindingSet has a binding with the
	 *         specified name, <tt>false</tt> otherwise.
	 */
	public boolean hasBinding(String bindingName);

	/**
	 * Gets the value of the binding with the specified name from this
	 * BindingSet.
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
