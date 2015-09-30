/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
