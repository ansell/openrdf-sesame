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
package org.eclipse.rdf4j.model.impl;

import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 * An extension of {@link SimpleLiteral} that stores a boolean value to avoid
 * parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class BooleanLiteral extends SimpleLiteral {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -3610638093719366795L;

	public static final BooleanLiteral TRUE = new BooleanLiteral(true);

	public static final BooleanLiteral FALSE = new BooleanLiteral(false);

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates an xsd:boolean typed literal with the specified value.
	 */
	protected BooleanLiteral(boolean value) {
		super(Boolean.toString(value), XMLSchema.BOOLEAN);
		this.value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean booleanValue()
	{
		return value;
	}

	/**
	 * Returns a {@link BooleanLiteral} for the specified value. This method
	 * uses the constants {@link #TRUE} and {@link #FALSE} as result values,
	 * preventing the often unnecessary creation of new
	 * {@link BooleanLiteral} objects.
	 */
	public static BooleanLiteral valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}
}
