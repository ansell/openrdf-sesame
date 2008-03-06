/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.http;


/**
 * A parameter consisting of a key and a value, which are both strings.
 */
public class Parameter {

	private String _key;
	private String _value;
	
	public Parameter(String key, String value) {
		_key = key;
		_value = value;
	}
	
	public String getKey() {
		return _key;
	}
	
	public String getValue() {
		return _value;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter other = (Parameter)obj;
			return
				_key.equals(other.getKey()) &&
				_value.equals(other.getValue());
		}
		
		return false;
	}
	
	public int hashCode() {
		return _key.hashCode();
	}
	
	public String toString() {
		if (_value == null) {
			return _key;
		}
		else {
			return _key + "="  + _value;
		}
	}
}
