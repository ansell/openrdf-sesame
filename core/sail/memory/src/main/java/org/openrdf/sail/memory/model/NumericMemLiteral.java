/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
