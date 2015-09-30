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

/**
 * Compares the string representation of a value expression to a pattern.
 */
public class Regex extends BinaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr flagsArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Regex() {
	}

	public Regex(ValueExpr expr, ValueExpr pattern, ValueExpr flags) {
		super(expr, pattern);
		setFlagsArg(flags);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return super.getLeftArg();
	}

	public void setArg(ValueExpr leftArg) {
		super.setLeftArg(leftArg);
	}

	public ValueExpr getPatternArg() {
		return super.getRightArg();
	}

	public void setPatternArg(ValueExpr rightArg) {
		super.setRightArg(rightArg);
	}

	public void setFlagsArg(ValueExpr flags) {
		this.flagsArg = flags;
	}

	public ValueExpr getFlagsArg() {
		return flagsArg;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		if (flagsArg != null) {
			flagsArg.visit(visitor);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Regex && super.equals(other)) {
			Regex o = (Regex)other;
			return nullEquals(flagsArg, o.getFlagsArg());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode() ^ "Regex".hashCode();
		if (flagsArg != null) {
			result ^= flagsArg.hashCode();
		}
		return result;
	}

	@Override
	public Regex clone() {
		Regex clone = (Regex)super.clone();
		if (flagsArg != null) {
			clone.setFlagsArg(flagsArg.clone());
		}
		return clone;
	}
}
