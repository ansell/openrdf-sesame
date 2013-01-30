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
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.algebra.base.UnarySqlOperator;

/**
 * The SQL IS CAST expression.
 * 
 * @author James Leigh
 * 
 */
public class SqlCast extends UnarySqlOperator {

	private int type;

	public SqlCast(SqlExpr arg, int type) {
		super(arg);
		this.type = type;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public int getType() {
		return type;
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " AS " + type;
	}

}
