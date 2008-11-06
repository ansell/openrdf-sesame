/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Namespace;

/**
 * A default implementation of the {@link Namespace} interface.
 */
public class NamespaceImpl implements Namespace {

	private static final long serialVersionUID = -5829768428912588171L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The namespace's prefix.
	 */
	private final String prefix;

	/**
	 * The namespace's name.
	 */
	private final String name;

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
		assert prefix != null : "prefix must not be null";
		assert name != null : "name must not be null";
		this.prefix = prefix;
		this.name = name;
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
		return prefix;
	}

	/**
	 * Gets the name of the namespace.
	 * 
	 * @return name of the namespace
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + 31 * prefix.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof NamespaceImpl) {
			NamespaceImpl other = (NamespaceImpl)obj;
			return name.equals(other.name) && prefix.equals(other.prefix);
		}

		return false;
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @return String representation of the namespace
	 */
	@Override
	public String toString() {
		return prefix + " :: " + name;
	}
}
