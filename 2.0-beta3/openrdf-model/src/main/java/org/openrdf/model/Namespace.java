/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

/**
 * A namespace, consisting of a namespace name and a prefix that has been
 * assigned to it.
 */
public interface Namespace {

	/**
	 * Gets the name of the current namespace (i.e. it's URI).
	 *
	 * @return name of namespace
	 */
	public String getName();

	/**
	 * Gets the prefix of the current namespace.
	 *
	 * @return prefix of namespace
	 */
	public String getPrefix();
}
