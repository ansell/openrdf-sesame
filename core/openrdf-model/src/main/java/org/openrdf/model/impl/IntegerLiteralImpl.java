/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of {@link LiteralImpl} that stores an integer value using a
 * {@link BigInteger} object.
 * 
 * @author Arjohn Kampman
 */
public class IntegerLiteralImpl extends LiteralImpl {

	private BigInteger _value;

	/**
	 * Creates an xsd:integer literal with the specified value.
	 */
	public IntegerLiteralImpl(BigInteger value) {
		this(value, XMLSchema.INTEGER);
	}

	/**
	 * Creates a literal with the specified value and datatype.
	 */
	public IntegerLiteralImpl(BigInteger value, URI datatype) {
		// TODO: maybe IntegerLiteralImpl should not extend LiteralImpl?
		super(value.toString(), datatype);
		_value = value;
	}

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
