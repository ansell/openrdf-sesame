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
package org.openrdf.query.parser.serql;

import org.openrdf.query.parser.serql.ast.ASTEdge;
import org.openrdf.query.parser.serql.ast.ASTNode;
import org.openrdf.query.parser.serql.ast.ASTNodeElem;
import org.openrdf.query.parser.serql.ast.ASTReifiedStat;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.serql.ast.VisitorException;

/**
 * Inserts anonymous variables into the abstract syntax tree at places where
 * such variables are already implicitly present.
 */
public class AnonymousVarGenerator extends AbstractASTVisitor {

	private int anonymousVarNo = 1;

	@Override
	public Object visit(ASTNode node, Object data)
		throws VisitorException
	{
		if (node.jjtGetNumChildren() == 0) {
			ASTNodeElem nodeElem = createNodeElem();
			nodeElem.jjtSetParent(node);
			node.jjtAppendChild(nodeElem);
		}

		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTReifiedStat node, Object data)
		throws VisitorException
	{
		if (node.jjtGetChild(0) instanceof ASTEdge) {
			// subject node is missing
			ASTNodeElem nodeElem = createNodeElem();
			nodeElem.jjtSetParent(node);
			node.jjtInsertChild(nodeElem, 0);
		}

		if (node.jjtGetNumChildren() <= 2) {
			// object node is missing
			ASTNodeElem nodeElem = createNodeElem();
			nodeElem.jjtSetParent(node);
			node.jjtAppendChild(nodeElem);
		}
		
		if (node.getID() == null) {
			node.setID(createAnonymousVar());
		}

		return super.visit(node, data);
	}

	private ASTNodeElem createNodeElem() {
		ASTNodeElem nodeElem = new ASTNodeElem(SyntaxTreeBuilderTreeConstants.JJTNODEELEM);
		
		ASTVar var = createAnonymousVar();
		var.jjtSetParent(nodeElem);
		nodeElem.jjtAppendChild(var);
		
		return nodeElem;
	}
	
	private ASTVar createAnonymousVar() {
		ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
		var.setName("-anon-" + anonymousVarNo++);
		var.setAnonymous(true);
		return var;
	}
}
