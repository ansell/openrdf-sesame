/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of MemLiteral that stores a numeric value to avoid parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class NumericMemLiteral extends MemLiteral {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Number _number;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NumericMemLiteral(Object creator, Number number, URI datatype) {
		super(creator, number.toString(), datatype);
		_number = number;
	}

	public NumericMemLiteral(Object creator, byte number) {
		this(creator, number, XMLSchema.BYTE);
	}

	public NumericMemLiteral(Object creator, short number) {
		this(creator, number, XMLSchema.SHORT);
	}

	public NumericMemLiteral(Object creator, int number) {
		this(creator, number, XMLSchema.INT);
	}

	public NumericMemLiteral(Object creator, long n) {
		this(creator, n, XMLSchema.LONG);
	}

	public NumericMemLiteral(Object creator, float n) {
		this(creator, n, XMLSchema.FLOAT);
	}

	public NumericMemLiteral(Object creator, double n) {
		this(creator, n, XMLSchema.DOUBLE);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public byte byteValue()
	{
		return _number.byteValue();
	}

	@Override
	public short shortValue()
	{
		return _number.shortValue();
	}

	@Override
	public int intValue()
	{
		return _number.intValue();
	}

	@Override
	public long longValue()
	{
		return _number.longValue();
	}

	@Override
	public float floatValue()
	{
		return _number.floatValue();
	}

	@Override
	public double doubleValue()
	{
		return _number.doubleValue();
	}
}
