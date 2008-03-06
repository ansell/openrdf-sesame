/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.querylanguage.serql.ast.ASTProjectionElem;
import org.openrdf.querylanguage.serql.ast.ASTSelect;
import org.openrdf.querylanguage.serql.ast.ASTVar;
import org.openrdf.querylanguage.serql.ast.ASTWhere;
import org.openrdf.querylanguage.serql.ast.Node;
import org.openrdf.querylanguage.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.querylanguage.serql.ast.VisitorException;


/**
 * Processes 'wildcard' projections, making them explicit by adding projection
 * elements with the appropriate variable nodes to them.
 */
class WildcardProjectionProcessor extends ASTVisitorBase {

	public Object visit(ASTSelect node, Object data)
		throws VisitorException
	{
		if (node.isWildcard()) {
			ProjectionVariableCollector visitor = new ProjectionVariableCollector();
			node.jjtAccept(visitor, data);

			for (String varName : visitor.getVariableNames()) {
				ASTProjectionElem projElemNode = new ASTProjectionElem(
						SyntaxTreeBuilderTreeConstants.JJTPROJECTIONELEM);
				node.jjtAppendChild(projElemNode);
				projElemNode.jjtSetParent(node);

				ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
				varNode.setName(varName);
				projElemNode.jjtAppendChild(varNode);
				varNode.jjtSetParent(projElemNode);
			}
			
			node.setWildcard(false);
		}

		return data;
	}

	/*-----------------------------------------*
	 * Inner class ProjectionVariableCollector *
	 *-----------------------------------------*/

	/**
	 * Collects variable names for 'wildcard' projections. An instance of this
	 * class should be supplied to the {@link ASTSelect#jjtAccept}. When done,
	 * the collected variable names can be acquired by calling
	 * {@link #getVariableNames}.
	 */
	private class ProjectionVariableCollector extends ASTVisitorBase {

		private Set<String> _variableNames = new LinkedHashSet<String>();

		public Set<String> getVariableNames() {
			return _variableNames;
		}

		public Object visit(ASTSelect node, Object data)
			throws VisitorException
		{
			// Collect all variables used in the body of the select query
			Node selectQueryNode = node.jjtGetParent();

			if (selectQueryNode.jjtGetNumChildren() >= 2) {
				selectQueryNode.jjtGetChild(1).jjtAccept(this, data);
			}

			return data;
		}

		public Object visit(ASTWhere node, Object data)
			throws VisitorException
		{
			// Do not visit where clauses
			return data;
		}

		public Object visit(ASTVar node, Object data)
			throws VisitorException
		{
			_variableNames.add(node.getName());
			return data;
		}
	}
}
