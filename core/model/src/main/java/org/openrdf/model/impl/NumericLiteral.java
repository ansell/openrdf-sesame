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
	protected NumericLiteral(Number number, IRI datatype) {
		super(number.toString(), datatype);
		this.number = number;
	}

	/**
	 * Creates an xsd:byte typed litral with the specified value.
	 */
	protected NumericLiteral(byte number) {
		this(number, XMLSchema.BYTE);
	}

	/**
	 * Creates an xsd:short typed litral with the specified value.
	 */
	protected NumericLiteral(short number) {
		this(number, XMLSchema.SHORT);
	}

	/**
	 * Creates an xsd:int typed litral with the specified value.
	 */
	protected NumericLiteral(int number) {
		this(number, XMLSchema.INT);
	}

	/**
	 * Creates an xsd:long typed litral with the specified value.
	 */
	protected NumericLiteral(long n) {
		this(n, XMLSchema.LONG);
	}

	/**
	 * Creates an xsd:float typed litral with the specified value.
	 */
	protected NumericLiteral(float n) {
		this(n, XMLSchema.FLOAT);
	}

	/**
	 * Creates an xsd:double typed litral with the specified value.
	 */
	protected NumericLiteral(double n) {
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
