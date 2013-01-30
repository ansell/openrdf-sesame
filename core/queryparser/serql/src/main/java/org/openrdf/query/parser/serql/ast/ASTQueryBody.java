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
package org.openrdf.query.parser.serql.ast;

import java.util.ArrayList;
import java.util.List;

public class ASTQueryBody extends SimpleNode {

	public ASTQueryBody(int id) {
		super(id);
	}

	public ASTQueryBody(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public List<ASTFrom> getFromClauseList() {
		List<ASTFrom> fromClauseList = new ArrayList<ASTFrom>(children.size());

		for (Node n : children) {
			if (n instanceof ASTFrom) {
				fromClauseList.add((ASTFrom)n);
			}
			else {
				break;
			}
		}

		return fromClauseList;
	}

	public boolean hasWhereClause() {
		return getWhereClause() != null;
	}

	public ASTWhere getWhereClause() {
		for (Node n : children) {
			if (n instanceof ASTWhere) {
				return (ASTWhere)n;
			}
		}

		return null;
	}
}
