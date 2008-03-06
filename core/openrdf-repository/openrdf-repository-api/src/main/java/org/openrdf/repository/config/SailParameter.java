/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

@Deprecated
public class SailParameter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _name;

	private String _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailParameter() {
	}

	public SailParameter(String name, String value) {
		setName(name);
		setValue(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}
}
