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

public class ASTSelectQuery extends ASTTupleQuery {

	public ASTSelectQuery(int id) {
		super(id);
	}

	public ASTSelectQuery(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTSelect getSelectClause() {
		return (ASTSelect)children.get(0);
	}

	public boolean hasQueryBody() {
		return children.size() >= 2;
	}

	public ASTQueryBody getQueryBody() {
		if (hasQueryBody()) {
			return (ASTQueryBody)children.get(1);
		}

		return null;
	}

	public boolean hasOrderBy() {
		return getOrderBy() != null;
	}

	public ASTOrderBy getOrderBy() {
		return jjtGetChild(ASTOrderBy.class);
	}

	public boolean hasLimit() {
		return getLimit() != null;
	}

	public ASTLimit getLimit() {
		return jjtGetChild(ASTLimit.class);
	}

	public boolean hasOffset() {
		return getOffset() != null;
	}

	public ASTOffset getOffset() {
		return jjtGetChild(ASTOffset.class);
	}
}
