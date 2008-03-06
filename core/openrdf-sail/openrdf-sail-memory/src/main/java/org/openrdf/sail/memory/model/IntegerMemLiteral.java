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

	private BigInteger _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IntegerMemLiteral(Object creator, BigInteger value) {
		this(creator, value, XMLSchema.INTEGER);
	}

	public IntegerMemLiteral(Object creator, BigInteger value, URI datatype) {
		super(creator, value.toString(), datatype);
		_value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public byte byteValue()
	{
		return _value.byteValue();
	}

	@Override
	public short shortValue()
	{
		return _value.shortValue();
	}

	@Override
	public int intValue()
	{
		return _value.intValue();
	}

	@Override
	public long longValue()
	{
		return _value.longValue();
	}

	@Override
	public float floatValue()
	{
		return _value.floatValue();
	}

	@Override
	public double doubleValue()
	{
		return _value.doubleValue();
	}

	@Override
	public BigInteger integerValue()
	{
		return _value;
	}

	@Override
	public BigDecimal decimalValue()
	{
		return new BigDecimal(_value);
	}
}
