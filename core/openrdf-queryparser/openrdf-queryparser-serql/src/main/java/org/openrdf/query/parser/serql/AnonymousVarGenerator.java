/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
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
public class AnonymousVarGenerator extends ASTVisitorBase {

	private int _anonymousVarNo = 1;

	@Override
	public Object visit(ASTNode node, Object data)
		throws VisitorException
	{
		if (node.jjtGetNumChildren() == 0) {
			ASTNodeElem nodeElem = _createNodeElem();
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
			ASTNodeElem nodeElem = _createNodeElem();
			nodeElem.jjtSetParent(node);
			node.jjtInsertChild(nodeElem, 0);
		}

		if (node.jjtGetNumChildren() <= 2) {
			// object node is missing
			ASTNodeElem nodeElem = _createNodeElem();
			nodeElem.jjtSetParent(node);
			node.jjtAppendChild(nodeElem);
		}
		
		if (node.getID() == null) {
			node.setID(_createAnonymousVar());
		}

		return super.visit(node, data);
	}

	private ASTNodeElem _createNodeElem() {
		ASTNodeElem nodeElem = new ASTNodeElem(SyntaxTreeBuilderTreeConstants.JJTNODEELEM);
		
		ASTVar var = _createAnonymousVar();
		var.jjtSetParent(nodeElem);
		nodeElem.jjtAppendChild(var);
		
		return nodeElem;
	}
	
	private ASTVar _createAnonymousVar() {
		ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
		var.setName("-anon-" + _anonymousVarNo++);
		var.setAnonymous(true);
		return var;
	}
}
