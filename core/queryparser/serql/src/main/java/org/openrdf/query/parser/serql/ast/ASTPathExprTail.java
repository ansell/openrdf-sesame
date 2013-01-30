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

public abstract class ASTPathExprTail extends SimpleNode {

	private boolean isBranch = false;

	public ASTPathExprTail(int id) {
		super(id);
	}

	public ASTPathExprTail(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	public boolean isBranch() {
		return isBranch;
	}

	public void setBranch(boolean isBranch) {
		this.isBranch = isBranch;
	}

	public boolean hasNextTail() {
		return getNextTail() != null;
	}

	/**
	 * Gets the path epxression tail following this part of the path expression,
	 * if any.
	 * 
	 * @return The next part of the path expression, or <tt>null</tt> if this
	 *         is the last part of the path expression.
	 */
	public abstract ASTPathExprTail getNextTail();

	@Override
	public String toString() {
		String result = super.toString();

		if (isBranch) {
			result += " (branch)";
		}

		return result;
	}
}
