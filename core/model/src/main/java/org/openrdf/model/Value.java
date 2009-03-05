/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import java.io.Serializable;

/**
 * The supertype of all RDF model objects (URIs, blank nodes and literals).
 */
public interface Value extends Serializable {

	/**
	 * Returns the String-value of a <tt>Value</tt> object. This returns either a
	 * {@link Literal}'s label, a {@link URI}'s URI or a {@link BNode}'s ID.
	 */
	public String stringValue();
}
