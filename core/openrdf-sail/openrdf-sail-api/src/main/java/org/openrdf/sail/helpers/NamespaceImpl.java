/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.Namespace;

/**
 * A default implementation of the {@link Namespace} interface.
 */
public class NamespaceImpl implements Namespace {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The namespace's prefix.
	 */
	private String _prefix;

	/**
	 * The namespace's name.
	 */
	private String _name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NamespaceImpl object.
	 * 
	 * @param prefix
	 *        The namespace's prefix.
	 * @param name
	 *        The namespace's name.
	 */
	public NamespaceImpl(String prefix, String name) {
		setPrefix(prefix);
		setName(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the prefix of the namespace.
	 * 
	 * @return prefix of the namespace
	 */
	public String getPrefix() {
		return _prefix;
	}

	/**
	 * Sets the prefix of the namespace.
	 * 
	 * @param prefix
	 *        The (new) prefix for this namespace.
	 */
	public void setPrefix(String prefix) {
		_prefix = prefix;
	}

	/**
	 * Gets the name of the namespace.
	 * 
	 * @return name of the namespace
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of the namespace.
	 * 
	 * @param name
	 *        The (new) name for this namespace.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @return String representation of the namespace
	 */
	public String toString() {
		return _prefix + " :: " + _name;
	}
}
