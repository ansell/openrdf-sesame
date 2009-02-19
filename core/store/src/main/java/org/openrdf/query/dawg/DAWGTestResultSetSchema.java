/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.dawg;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetSchema {

	public static final String NAMESPACE = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

	public static final URI RESULTSET;

	public static final URI RESULTVARIABLE;

	public static final URI SOLUTION;

	public static final URI BINDING;

	public static final URI VALUE;

	public static final URI VARIABLE;

	public static final URI BOOLEAN;

	public static final Literal TRUE;

	public static final Literal FALSE;

	static {
		ValueFactory vf = ValueFactoryImpl.getInstance();
		RESULTSET = vf.createURI(NAMESPACE, "ResultSet");
		RESULTVARIABLE = vf.createURI(NAMESPACE, "resultVariable");
		SOLUTION = vf.createURI(NAMESPACE, "solution");
		BINDING = vf.createURI(NAMESPACE, "binding");
		VALUE = vf.createURI(NAMESPACE, "value");
		VARIABLE = vf.createURI(NAMESPACE, "variable");
		BOOLEAN = vf.createURI(NAMESPACE, "boolean");
		TRUE = vf.createLiteral(true);
		FALSE = vf.createLiteral(false);
	}
}
