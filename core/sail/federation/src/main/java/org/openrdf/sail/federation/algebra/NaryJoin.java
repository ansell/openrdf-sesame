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
package org.openrdf.sail.federation.algebra;

import java.util.List;

import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;

/**
 * A natural join between two tuple expressions.
 */
public class NaryJoin extends AbstractNaryTupleOperator {

	private static final long serialVersionUID = -1501013589230065874L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NaryJoin() {
		super();
	}

	/**
	 * Creates a new natural join operator.
	 */
	public NaryJoin(TupleExpr... args) {
		super(args);
	}

	/**
	 * Creates a new natural join operator.
	 */
	public NaryJoin(List<TupleExpr> args) {
		super(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
			throws X {
		visitor.meetOther(this);
	}

	@Override
	public NaryJoin clone() { // NOPMD
		return (NaryJoin) super.clone();
	}
}
