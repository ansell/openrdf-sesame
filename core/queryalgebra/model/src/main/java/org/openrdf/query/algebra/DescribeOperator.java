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

import java.util.List;

/**
 * @author Jeen Broekstra
 */
public class DescribeOperator extends UnaryTupleOperator {

	private List<String> aliases;

	/**
	 * Create a new Describe Operator with the supplied DESCRIBE expressions.
	 * 
	 * @param describeExprs
	 *        the list of names of expressions in the DESCRIBE clause.
	 */
	public DescribeOperator(List<String> aliases) {
		super();
		this.aliases = aliases;
	}

	/**
	 * Create a new Describe Operator with the supplied tuple expression and
	 * DESCRIBE expressions.
	 * 
	 * @param arg
	 *        the tuple expresssion representing the WHERE clause
	 * @param describeExprs
	 *        list of names of expressions in the DESCRIBE clause.
	 */
	public DescribeOperator(TupleExpr arg, List<String> aliases) {
		super(arg);
		this.aliases = aliases;
	}

	@Override
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public List<String> getDescribeExprNames() {
		return this.aliases;
	}

	public void setDescribeExprNames(List<String> aliases) {
		this.aliases = aliases;
	}

}
