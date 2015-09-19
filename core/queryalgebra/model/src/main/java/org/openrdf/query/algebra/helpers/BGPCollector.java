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
package org.openrdf.query.algebra.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.StatementPattern;

/**
 * Basic graph pattern collector.
 */
public class BGPCollector<X extends Exception> extends QueryModelVisitorBase<X> {

	private final QueryModelVisitor<X> visitor;

	private List<StatementPattern> statementPatterns;

	public BGPCollector(QueryModelVisitor<X> visitor) {
		this.visitor = visitor;
	}

	public List<StatementPattern> getStatementPatterns() {
		return (statementPatterns != null) ? statementPatterns : Collections.<StatementPattern>emptyList();
	}

	@Override
	public void meet(Join node) throws X {
		// by-pass meetNode()
		node.visitChildren(this);
	}

	@Override
	public void meet(StatementPattern sp) throws X {
		if(statementPatterns == null)
		{
			statementPatterns = new ArrayList<StatementPattern>();
		}
		statementPatterns.add(sp);
	}

	@Override
	protected void meetNode(QueryModelNode node) throws X {
		// resume previous visitor
		node.visit(visitor);
	}
}
