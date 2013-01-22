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

public class ASTOptPathExprTail extends ASTPathExprTail {

	public ASTOptPathExprTail(int id) {
		super(id);
	}

	public ASTOptPathExprTail(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	/**
	 * Gets the optional tail part of the path expression.
	 * 
	 * @return The optional tail part of the path expression.
	 */
	public ASTBasicPathExprTail getOptionalTail() {
		return (ASTBasicPathExprTail)children.get(0);
	}

	public boolean hasWhereClause() {
		return getWhereClause() != null;
	}

	/**
	 * Gets the where-clause that constrains the results of the optional path
	 * expression tail, if any.
	 * 
	 * @return The where-clause, or <tt>null</tt> if not available.
	 */
	public ASTWhere getWhereClause() {
		if (children.size() >= 2) {
			Node node = children.get(1);

			if (node instanceof ASTWhere) {
				return (ASTWhere)node;
			}
		}

		return null;
	}

	@Override
	public ASTPathExprTail getNextTail() {
		if (children.size() >= 2) {
			Node node = children.get(children.size() - 1);

			if (node instanceof ASTPathExprTail) {
				return (ASTPathExprTail)node;
			}
		}

		return null;
	}
}
