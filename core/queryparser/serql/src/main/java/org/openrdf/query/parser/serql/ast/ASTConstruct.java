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


public class ASTConstruct extends SimpleNode {

	private boolean distinct = false;

	private boolean reduced = false;

	private boolean wildcard = false;

	public ASTConstruct(int id) {
		super(id);
	}

	public ASTConstruct(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setReduced(boolean reduced) {
		this.reduced = reduced;
	}

	public boolean isReduced() {
		return reduced;
	}

	public boolean isWildcard() {
		return wildcard;
	}

	public void setWildcard(boolean wildcard) {
		this.wildcard = wildcard;
	}

	public ASTPathExpr getPathExpr() {
		return (ASTPathExpr)children.get(0);
	}

	@Override
	public String toString() {
		String result = super.toString();

		if (distinct || reduced || wildcard) {
			result += " (";
			if (distinct) {
				result += " distinct";
			}
			if (reduced) {
				result += " reduced";
			}
			if (wildcard) {
				result += " *";
			}
			result += " )";
		}

		return result;
	}
}
