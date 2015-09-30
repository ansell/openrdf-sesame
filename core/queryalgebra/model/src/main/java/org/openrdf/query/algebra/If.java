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
package org.openrdf.query.algebra;

/**
 * The IF function, as defined in SPARQL 1.1 Query.
 * 
 * @author Jeen Broekstra
 */
public class If extends AbstractQueryModelNode implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	private ValueExpr condition;

	private ValueExpr result;

	private ValueExpr alternative;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public If() {
	}

	public If(ValueExpr condition) {
		setCondition(condition);
	}

	public If(ValueExpr condition, ValueExpr result) {
		setCondition(condition);
		setResult(result);
	}

	public If(ValueExpr condition, ValueExpr result, ValueExpr alternative) {
		setCondition(condition);
		setResult(result);
		setAlternative(alternative);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getCondition() {
		return condition;
	}

	/**
	 * Sets the condition argument of this unary value operator.
	 * 
	 * @param condition
	 *        The (new) condition argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setCondition(ValueExpr condition) {
		assert condition != null : "arg must not be null";
		condition.setParentNode(this);
		this.condition = condition;
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
		condition.visit(visitor);
		if (result != null) {
			result.visit(visitor);
		}
		if (alternative != null) {
			alternative.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (condition == current) {
			setCondition((ValueExpr)replacement);
		}
		else if (result == current) {
			setResult((ValueExpr)replacement);
		}
		else if (alternative == current) {
			setAlternative((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof If) {
			If o = (If)other;

			boolean equal = condition.equals(o.getCondition());
			if (!equal) {
				return equal;
			}

			equal = (result == null) ? o.getResult() == null : result.equals(o.getResult());
			if (!equal) {
				return equal;
			}

			equal = (alternative == null) ? o.getAlternative() == null : alternative.equals(o.getAlternative());

			return equal;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = condition.hashCode();

		if (result != null) {
			hashCode = hashCode ^ result.hashCode();
		}
		if (alternative != null) {
			hashCode = hashCode ^ alternative.hashCode();
		}

		hashCode = hashCode ^ "If".hashCode();

		return hashCode;
	}

	@Override
	public If clone() {
		If clone = (If)super.clone();
		clone.setCondition(condition.clone());
		if (result != null) {
			clone.setResult(result.clone());
		}
		if (alternative != null) {
			clone.setAlternative(alternative.clone());
		}
		return clone;
	}

	/**
	 * @param result
	 *        The result to set.
	 */
	public void setResult(ValueExpr result) {
		result.setParentNode(this);
		this.result = result;

	}

	/**
	 * @return Returns the result.
	 */
	public ValueExpr getResult() {
		return result;
	}

	/**
	 * @param alternative
	 *        The alternative to set.
	 */
	public void setAlternative(ValueExpr alternative) {
		alternative.setParentNode(this);
		this.alternative = alternative;
	}

	/**
	 * @return Returns the alternative.
	 */
	public ValueExpr getAlternative() {
		return alternative;
	}
}
