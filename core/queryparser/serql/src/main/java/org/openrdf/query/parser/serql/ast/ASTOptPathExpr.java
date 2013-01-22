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


public class ASTOptPathExpr extends ASTPathExpr {

	public ASTOptPathExpr(int id) {
		super(id);
	}

	public ASTOptPathExpr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTPathExpr getPathExpr() {
		return (ASTPathExpr)children.get(0);
	}

	/**
	 * Checks if this optional path expression has a constraint.
	 */
	public boolean hasConstraint() {
		return getWhereClause() != null;
	}

	/**
	 * Returns the where clause on the optional path expression, if present.
	 * 
	 * @return The where clause, or <tt>null</tt> if no where clause was
	 *         specified.
	 */
	public ASTWhere getWhereClause() {
		Node lastChildNode = children.get(children.size() - 1);

		if (lastChildNode instanceof ASTWhere) {
			return (ASTWhere)lastChildNode;
		}

		return null;
	}
}
