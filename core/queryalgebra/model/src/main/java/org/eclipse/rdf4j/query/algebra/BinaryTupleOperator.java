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
 * An abstract superclass for binary tuple operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryTupleOperator extends AbstractQueryModelNode implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's left argument.
	 */
	protected TupleExpr leftArg;

	/**
	 * The operator's right argument.
	 */
	protected TupleExpr rightArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BinaryTupleOperator() {
	}

	/**
	 * Creates a new binary tuple operator.
	 * 
	 * @param leftArg
	 *        The operator's left argument, must not be <tt>null</tt>.
	 * @param rightArg
	 *        The operator's right argument, must not be <tt>null</tt>.
	 */
	public BinaryTupleOperator(TupleExpr leftArg, TupleExpr rightArg) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the left argument of this binary tuple operator.
	 * 
	 * @return The operator's left argument.
	 */
	public TupleExpr getLeftArg() {
		return leftArg;
	}

	/**
	 * Sets the left argument of this binary tuple operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(TupleExpr leftArg) {
		assert leftArg != null : "leftArg must not be null";
		leftArg.setParentNode(this);
		this.leftArg = leftArg;
	}

	/**
	 * Gets the right argument of this binary tuple operator.
	 * 
	 * @return The operator's right argument.
	 */
	public TupleExpr getRightArg() {
		return rightArg;
	}

	/**
	 * Sets the right argument of this binary tuple operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(TupleExpr rightArg) {
		assert rightArg != null : "rightArg must not be null";
		rightArg.setParentNode(this);
		this.rightArg = rightArg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		leftArg.visit(visitor);
		rightArg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (leftArg == current) {
			setLeftArg((TupleExpr)replacement);
		}
		else if (rightArg == current) {
			setRightArg((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BinaryTupleOperator) {
			BinaryTupleOperator o = (BinaryTupleOperator)other;
			return leftArg.equals(o.getLeftArg()) && rightArg.equals(o.getRightArg());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return leftArg.hashCode() ^ rightArg.hashCode();
	}

	@Override
	public BinaryTupleOperator clone() {
		BinaryTupleOperator clone = (BinaryTupleOperator)super.clone();
		clone.setLeftArg(getLeftArg().clone());
		clone.setRightArg(getRightArg().clone());
		return clone;
	}
}
