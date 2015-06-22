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
