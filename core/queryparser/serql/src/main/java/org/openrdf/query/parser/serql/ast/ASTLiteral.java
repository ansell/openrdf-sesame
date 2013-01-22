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

public class ASTLiteral extends ASTValue {

	private String label;

	private String lang;

	public ASTLiteral(int id) {
		super(id);
	}

	public ASTLiteral(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLang() {
		return lang;
	}

	public boolean hasLang() {
		return lang != null;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public ASTValueExpr getDatatypeNode() {
		if (children.size() >= 1) {
			return (ASTValueExpr)children.get(0);
		}

		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(super.toString());

		sb.append(" (\"").append(label).append("\"");

		if (lang != null) {
			sb.append('@').append(lang);
		}

		sb.append(")");

		return sb.toString();
	}
}
