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


public class ASTFrom extends SimpleNode {

	public ASTFrom(int id) {
		super(id);
	}

	public ASTFrom(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean hasContextID() {
		return getContextID() != null;
	}

	public ASTValueExpr getContextID() {
		Node firstNode = children.get(0);

		if (firstNode instanceof ASTValueExpr) {
			return (ASTValueExpr)firstNode;
		}

		return null;
	}

	public ASTPathExpr getPathExpr() {
		return (ASTPathExpr)children.get(children.size() - 1);
	}
}
