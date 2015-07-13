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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openrdf.model.IRI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of {@link SimpleLiteral} that stores an integer value using a
 * {@link BigInteger} object.
 * 
 * @author Arjohn Kampman
 */
public class IntegerLiteral extends SimpleLiteral {

	private static final long serialVersionUID = 4199641304079427245L;

	private final BigInteger value;

	/**
	 * Creates an xsd:integer literal with the specified value.
	 */
	public IntegerLiteral(BigInteger value) {
		this(value, XMLSchema.INTEGER);
	}

	/**
	 * Creates a literal with the specified value and datatype.
	 */
	public IntegerLiteral(BigInteger value, IRI datatype) {
		// TODO: maybe IntegerLiteralImpl should not extend LiteralImpl?
		super(value.toString(), datatype);
		this.value = value;
	}

	@Override
	public byte byteValue()
	{
		return value.byteValue();
	}

	@Override
	public short shortValue()
	{
		return value.shortValue();
	}

	@Override
	public int intValue()
	{
		return value.intValue();
	}

	@Override
	public long longValue()
	{
		return value.longValue();
	}

	@Override
	public float floatValue()
	{
		return value.floatValue();
	}

	@Override
	public double doubleValue()
	{
		return value.doubleValue();
	}

	@Override
	public BigInteger integerValue()
	{
		return value;
	}

	@Override
	public BigDecimal decimalValue()
	{
		return new BigDecimal(value);
	}
}
