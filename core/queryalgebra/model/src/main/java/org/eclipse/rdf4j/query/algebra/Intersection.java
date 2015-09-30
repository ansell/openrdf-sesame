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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The INTERSECT set operator, which returns the intersection of the result sets
 * of two tuple expressions.
 */
public class Intersection extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Intersection() {
	}

	/**
	 * Creates a new intersection operator that operates on the two specified
	 * arguments.
	 * 
	 * @param leftArg
	 *        The left argument of the intersection operator.
	 * @param rightArg
	 *        The right argument of the intersection operator.
	 */
	public Intersection(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.retainAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getAssuredBindingNames());
		bindingNames.retainAll(getRightArg().getAssuredBindingNames());
		return bindingNames;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Intersection && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Intersection".hashCode();
	}

	@Override
	public Intersection clone() {
		return (Intersection)super.clone();
	}
}
