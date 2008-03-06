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

	/*-----------*
	 * Variables *
	 *-----------*/

	private BigDecimal _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DecimalMemLiteral(Object creator, BigDecimal value) {
		this(creator, value, XMLSchema.DECIMAL);
	}

	public DecimalMemLiteral(Object creator, BigDecimal value, URI datatype) {
		super(creator, value.toPlainString(), XMLSchema.DECIMAL);
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
		return _value.toBigInteger();
	}

	@Override
	public BigDecimal decimalValue()
	{
		return _value;
	}
}
