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
package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A natural join between two tuple expressions.
 */
public class Join extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Join() {
	}

	/**
	 * Creates a new natural join operator.
	 */
	public Join(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasSubSelectInRightArg() {
		return containsProjection(rightArg);
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getAssuredBindingNames());
		bindingNames.addAll(getRightArg().getAssuredBindingNames());
		return bindingNames;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Join && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Join".hashCode();
	}

	@Override
	public Join clone() {
		return (Join)super.clone();
	}

	private boolean containsProjection(TupleExpr t) {
		@SuppressWarnings("serial")
		class VisitException extends Exception {
		}
		final boolean[] result = new boolean[1];
		try {
			t.visit(new QueryModelVisitorBase<VisitException>() {

				@Override
				public void meet(Projection node)
					throws VisitException
				{
					result[0] = true;
					throw new VisitException();
				}

				@Override
				public void meet(Join node)
					throws VisitException
				{
					// projections already inside a Join need not be
					// taken into account
					result[0] = false;
					throw new VisitException();
				}
			});
		}
		catch (VisitException ex) {
			// Do nothing. We have thrown this exception on the first successful
			// meeting of Projection.
		}
		return result[0];
	}
}
