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

import org.openrdf.model.IRI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of MemLiteral that stores a numeric value to avoid parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class NumericMemLiteral extends MemLiteral {

	private static final long serialVersionUID = -4077489124945558638L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Number number;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NumericMemLiteral(Object creator, String label, Number number, IRI datatype) {
		super(creator, label, datatype);
		this.number = number;
	}

	public NumericMemLiteral(Object creator, Number number, IRI datatype) {
		this(creator, number.toString(), number, datatype);
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
	public byte byteValue() {
		return number.byteValue();
	}

	@Override
	public short shortValue() {
		return number.shortValue();
	}

	@Override
	public int intValue() {
		return number.intValue();
	}

	@Override
	public long longValue() {
		return number.longValue();
	}

	@Override
	public float floatValue() {
		return number.floatValue();
	}

	@Override
	public double doubleValue() {
		return number.doubleValue();
	}
}
