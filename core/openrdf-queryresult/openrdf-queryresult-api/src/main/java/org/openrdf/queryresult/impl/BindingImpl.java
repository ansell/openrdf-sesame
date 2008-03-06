/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.impl;

import org.openrdf.queryresult.Binding;

import org.openrdf.model.Value;


/**
 * An implementation of the {@link Binding} interface.
 */
public class BindingImpl implements Binding {

	private String _name;

	private Value _value;

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
		
		_name = name;
		_value = value;
	}

	public String getName() {
		return _name;
	}

	public Value getValue() {
		return _value;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Binding) {
			Binding other = (Binding)o;
			
			return _name.equals(other.getName()) && _value.equals(other.getValue());
		}
		
		return false;
	}
	
	public int hashCode() {
		return _name.hashCode() ^ _value.hashCode();
	}
}
