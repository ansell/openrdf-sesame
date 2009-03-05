/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the Sesame schema namespace.
 */
public class SESAME {

	/**
	 * The Sesame Schema namespace (
	 * <tt>http://www.openrdf.org/schema/sesame#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/schema/sesame#";

	/** <tt>http://www.openrdf.org/schema/sesame#directSubClassOf</tt> */
	public final static URI DIRECTSUBCLASSOF;

	/** <tt>http://www.openrdf.org/schema/sesame#directSubPropertyOf</tt> */
	public final static URI DIRECTSUBPROPERTYOF;

	/** <tt>http://www.openrdf.org/schema/sesame#directType</tt> */
	public final static URI DIRECTTYPE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		DIRECTSUBCLASSOF = factory.createURI(SESAME.NAMESPACE, "directSubClassOf");
		DIRECTSUBPROPERTYOF = factory.createURI(SESAME.NAMESPACE, "directSubPropertyOf");
		DIRECTTYPE = factory.createURI(SESAME.NAMESPACE, "directType");
	}
}
