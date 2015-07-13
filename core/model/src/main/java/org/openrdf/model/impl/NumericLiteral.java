/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.model.impl;

import org.openrdf.model.IRI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of {@link SimpleLiteral} that stores a numeric value to avoid
 * parsing.
 * 
 * @author David Huynh
 */
public class NumericLiteral extends SimpleLiteral {

	private static final long serialVersionUID = 3004497457768807919L;

	private final Number number;

	/**
	 * Creates a literal with the specified value and datatype.
	 */
	public NumericLiteral(Number number, IRI datatype) {
		super(number.toString(), datatype);
		this.number = number;
	}

	/**
	 * Creates an xsd:byte typed litral with the specified value.
	 */
	public NumericLiteral(byte number) {
		this(number, XMLSchema.BYTE);
	}

	/**
	 * Creates an xsd:short typed litral with the specified value.
	 */
	public NumericLiteral(short number) {
		this(number, XMLSchema.SHORT);
	}

	/**
	 * Creates an xsd:int typed litral with the specified value.
	 */
	public NumericLiteral(int number) {
		this(number, XMLSchema.INT);
	}

	/**
	 * Creates an xsd:long typed litral with the specified value.
	 */
	public NumericLiteral(long n) {
		this(n, XMLSchema.LONG);
	}

	/**
	 * Creates an xsd:float typed litral with the specified value.
	 */
	public NumericLiteral(float n) {
		this(n, XMLSchema.FLOAT);
	}

	/**
	 * Creates an xsd:double typed litral with the specified value.
	 */
	public NumericLiteral(double n) {
		this(n, XMLSchema.DOUBLE);
	}

	@Override
	public byte byteValue()
	{
		return number.byteValue();
	}

	@Override
	public short shortValue()
	{
		return number.shortValue();
	}

	@Override
	public int intValue()
	{
		return number.intValue();
	}

	@Override
	public long longValue()
	{
		return number.longValue();
	}

	@Override
	public float floatValue()
	{
		return number.floatValue();
	}

	@Override
	public double doubleValue()
	{
		return number.doubleValue();
	}
}
