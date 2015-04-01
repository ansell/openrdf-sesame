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
 * An extension of {@link LiteralImpl} that stores an integer value using a
 * {@link BigDecimal} object.
 * 
 * @author Arjohn Kampman
 */
public class DecimalLiteralImpl extends LiteralImpl {

	private static final long serialVersionUID = -3310213093222314380L;
	
	private final BigDecimal value;

	/**
	 * Creates an xsd:decimal literal with the specified value.
	 */
	public DecimalLiteralImpl(BigDecimal value) {
		this(value, XMLSchema.DECIMAL);
	}

	/**
	 * Creates a literal with the specified value and datatype.
	 */
	public DecimalLiteralImpl(BigDecimal value, IRI datatype) {
		// TODO: maybe DecimalLiteralImpl should not extend LiteralImpl?
		super(value.toPlainString(), datatype);
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
		return value.toBigInteger();
	}

	@Override
	public BigDecimal decimalValue()
	{
		return value;
	}
}
