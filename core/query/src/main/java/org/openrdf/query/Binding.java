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
package org.openrdf.query;

import java.io.Serializable;

import org.openrdf.model.Value;

/**
 * A named value binding.
 */
public interface Binding extends Serializable {

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
