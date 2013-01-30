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
package org.openrdf.sail.rdbms.algebra.base;

import org.openrdf.query.algebra.QueryModelNodeBase;
import org.openrdf.query.algebra.QueryModelVisitor;

/**
 * An extension to {@link QueryModelNodeBase} for SQL query algebra.
 * 
 * @author James Leigh
 * 
 */
public abstract class RdbmsQueryModelNodeBase extends QueryModelNodeBase {

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		if (visitor instanceof RdbmsQueryModelVisitorBase) {
			visit((RdbmsQueryModelVisitorBase<X>)visitor);
		}
		else {
			visitor.meetOther(this);
		}
	}

	public abstract <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X;
}
