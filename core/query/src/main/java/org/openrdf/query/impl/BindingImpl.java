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
package org.openrdf.query.impl;

import org.openrdf.query.Binding;

import org.openrdf.model.Value;

/**
 * An implementation of the {@link Binding} interface.
 */
public class BindingImpl implements Binding {

	private static final long serialVersionUID = -8257244838478873298L;

	private final String name;

	private final Value value;

	/**
	 * Creates a binding object with the supplied name and value.
	 * 
	 * @param name
	 *        The binding's name.
	 * @param value
	 *        The binding's value.
	 */
	public BindingImpl(String name, Value value) {
		assert name != null : "name must not be null";
		assert value != null : "value must not be null";

		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Binding) {
			Binding other = (Binding)o;

			return name.equals(other.getName()) && value.equals(other.getValue());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode() ^ value.hashCode();
	}

	@Override
	public String toString()
	{
		return name + "=" + value.toString();
	}
}
