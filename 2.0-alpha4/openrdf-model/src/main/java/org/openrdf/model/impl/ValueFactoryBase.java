/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Abstract base class for {@link ValueFactory} implementations, offering
 * default implementation for a number of methods.
 */
public abstract class ValueFactoryBase implements ValueFactory {

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#BOOLEAN}
	 * as datatype.
	 */
	public Literal createLiteral(boolean value) {
		return createLiteral(String.valueOf(value), XMLSchema.BOOLEAN);
	}

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#LONG} as
	 * datatype.
	 */
	public Literal createLiteral(long value) {
		return createLiteral(String.valueOf(value), XMLSchema.LONG);
	}

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#INT} as
	 * datatype.
	 */
	public Literal createLiteral(int value) {
		return createLiteral(String.valueOf(value), XMLSchema.INT);
	}

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#SHORT} as
	 * datatype.
	 */
	public Literal createLiteral(short value) {
		return createLiteral(String.valueOf(value), XMLSchema.SHORT);
	}

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#BYTE} as
	 * datatype.
	 */
	public Literal createLiteral(byte value) {
		return createLiteral(String.valueOf(value), XMLSchema.BYTE);
	}

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#DOUBLE}
	 * as datatype.
	 */
	public Literal createLiteral(double value) {
		return createLiteral(String.valueOf(value), XMLSchema.DOUBLE);
	}

	/**
	 * Calls
	 * {@link ValueFactory#createLiteral(java.lang.String,org.openrdf.model.URI}
	 * with the String-value of the supplied value and {@link XMLSchema#FLOAT} as
	 * datatype.
	 */
	public Literal createLiteral(float value) {
		return createLiteral(String.valueOf(value), XMLSchema.FLOAT);
	}
}
