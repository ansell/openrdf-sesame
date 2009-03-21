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
 * An extension of MemLiteral that stores a decimal value to avoid parsing.
 * 
 * @author Arjohn Kampman
 */
public class DecimalMemLiteral extends MemLiteral {

	private static final long serialVersionUID = 6760727653986046772L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private final BigDecimal value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DecimalMemLiteral(Object creator, BigDecimal value) {
		this(creator, value, XMLSchema.DECIMAL);
	}

	public DecimalMemLiteral(Object creator, BigDecimal value, URI datatype) {
		this(creator, value.toPlainString(), value, datatype);
	}

	public DecimalMemLiteral(Object creator, String label, BigDecimal value, URI datatype) {
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
		return value.toBigInteger();
	}

	@Override
	public BigDecimal decimalValue() {
		return value;
	}
}
