/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Deprecated
public class SailConfig {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _className;

	/**
	 * A List of SailParameter objects.
	 */
	private List<SailParameter> _parameters = new ArrayList<SailParameter>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SailConfig() {
	}

	public SailConfig(String className) {
		setClassName(className);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns the name of the configured Sail class.
	 */
	public String getClassName() {
		return _className;
	}
	
	/**
	 * Assigns the name of the Sail class to be used.
	 */
	public void setClassName(String className) {
		_className = className;
	}
	
	/**
	 * Adds an initialization parameter to this Sail configuration.
	 * 
	 * @param name
	 *        the name of the parameter
	 * @param value
	 *        the value of the parameter
	 */
	public void addParameter(String name, String value) {
		addParameter(new SailParameter(name, value));
	}

	/**
	 * Adds an initialization parameter to this Sail configuration.
	 * 
	 * @param param
	 *        The parameter.
	 */
	public void addParameter(SailParameter param) {
		_parameters.add(param);
	}

	/**
	 * Removes the parameter with the supplied key
	 * 
	 * @param param
	 *        The parameter that should be removed.
	 */
	public void removeParameter(SailParameter param) {
		_parameters.remove(param);
	}

	/**
	 * Gets the configured Sail initialization parameters.
	 * 
	 * @return An immutable list of SailParameter objects.
	 */
	public List<SailParameter> getParameters() {
		return Collections.unmodifiableList(_parameters);
	}
}
