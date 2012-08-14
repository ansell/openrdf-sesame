/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.util;


/**
 * A parameter consisting of a key and a value, which are both strings.
 */
public class Parameter {

	private final String key;
	private final String value;
	
	public Parameter(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter other = (Parameter)obj;
			return
				key.equals(other.getKey()) &&
				value.equals(other.getValue());
		}
		
		return false;
	}
	
	public int hashCode() {
		return key.hashCode();
	}
	
	public String toString() {
		if (value == null) {
			return key;
		}
		else {
			return key + "="  + value;
		}
	}
}
