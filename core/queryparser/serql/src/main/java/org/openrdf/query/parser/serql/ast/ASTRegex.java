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

public class ASTRegex extends ASTBooleanExpr {

	public ASTRegex(int id) {
		super(id);
	}

	public ASTRegex(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getText() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTValueExpr getPattern() {
		return (ASTValueExpr)children.get(1);
	}

	public boolean hasFlags() {
		return getFlags() != null;
	}

	public ASTValueExpr getFlags() {
		if (children.size() >= 3) {
			return (ASTValueExpr)children.get(2);
		}

		return null;
	}
}
