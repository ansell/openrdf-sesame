/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of MemLiteral that stores an integer value to avoid parsing.
 * 
 * @author Arjohn Kampman
 */
public class IntegerMemLiteral extends MemLiteral {

	/*-----------*
	 * Variables *
	 *-----------*/

	private static final long serialVersionUID = -8121416400439616510L;

	private final BigInteger value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IntegerMemLiteral(Object creator, BigInteger value) {
		this(creator, value, XMLSchema.INTEGER);
	}

	public IntegerMemLiteral(Object creator, BigInteger value, URI datatype) {
		this(creator, value.toString(), value, datatype);
	}

	public IntegerMemLiteral(Object creator, String label, BigInteger value, URI datatype) {
		super(creator, label, datatype);
		this.value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public byte byteValue() {
		return value.byteValue();
	}

	@Override
	public short shortValue() {
		return value.shortValue();
	}

	@Override
	public int intValue() {
		return value.intValue();
	}

	@Override
	public long longValue() {
		return value.longValue();
	}

	@Override
	public float floatValue() {
		return value.floatValue();
	}

	@Override
	public double doubleValue() {
		return value.doubleValue();
	}

	@Override
	public BigInteger integerValue() {
		return value;
	}

	@Override
	public BigDecimal decimalValue() {
		return new BigDecimal(value);
	}
}
