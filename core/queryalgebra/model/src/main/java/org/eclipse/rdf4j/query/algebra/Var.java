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
package org.eclipse.rdf4j.query.algebra;

import org.eclipse.rdf4j.model.Value;

/**
 * A variable that can contain a Value.
 */
public class Var extends AbstractQueryModelNode implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String name;

	private Value value;

	private boolean anonymous = false;

	private boolean constant = false;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public Var() {
	}

	public Var(String name) {
		setName(name);
	}

	public Var(String name, Value value) {
		this(name);
		setValue(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean hasValue() {
		return value != null;
	}

	public Value getValue() {
		return value;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(super.getSignature());

		sb.append(" (name=").append(name);

		if (value != null) {
			sb.append(", value=").append(value.toString());
		}

		if (anonymous) {
			sb.append(", anonymous");
		}

		sb.append(")");

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Var) {
			Var o = (Var)other;
			return name.equals(o.getName()) && nullEquals(value, o.getValue()) && anonymous == o.isAnonymous();
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		if (value != null) {
			result ^= value.hashCode();
		}
		if (anonymous) {
			result = ~result;
		}
		return result;
	}

	@Override
	public Var clone() {
		return (Var)super.clone();
	}

	/**
	 * @return Returns the constant.
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * @param constant The constant to set.
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
	}
}
