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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Jeen Broekstra
 */
public class DescribeOperator extends QueryModelNodeBase implements TupleExpr {

	private List<ValueExpr> valueExprs;

	/**
	 * Create a new Describe Operator with the supplied DESCRIBE expressions.
	 * 
	 * @param describeExprs
	 */
	public DescribeOperator(List<ValueExpr> describeExprs) {
		super();
		this.valueExprs = describeExprs;
	}

	@Override
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public Set<String> getBindingNames() {
		return null;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		return null;
	}

	public List<ValueExpr> getDescribeExprs() {
		return this.valueExprs;
	}

	public void setDescribeExprs(List<ValueExpr> describeExprs) {
		this.valueExprs = describeExprs;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof DescribeOperator) {
			DescribeOperator o = (DescribeOperator)other;
			return valueExprs.equals(o.getDescribeExprs());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return valueExprs.hashCode();
	}

	@Override
	public DescribeOperator clone() {
		DescribeOperator clone = (DescribeOperator)super.clone();
		clone.setDescribeExprs(new ArrayList<ValueExpr>(getDescribeExprs()));
		return clone;
	}
}
