/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.serql.ast.ASTProjectionElem;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTSelectQuery;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.ASTWhere;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.serql.ast.VisitorException;

/**
 * Processes 'wildcard' projections, making them explicit by adding projection
 * elements with the appropriate variable nodes to them.
 * 
 * @author Arjohn Kampman
 */
class WildcardProjectionProcessor extends ASTVisitorBase {

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		try {
			qc.jjtAccept(new WildcardProjectionProcessor(), null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	public Object visit(ASTSelect node, Object data)
		throws VisitorException
	{
		if (node.isWildcard()) {
			// Collect all variables used in the body of the select query
			ProjectionVariableCollector visitor = new ProjectionVariableCollector();
			node.jjtGetParent().jjtAccept(visitor, null);

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
	 * class should be supplied to a {@link ASTSelectQuery} node. When done, the
	 * collected variable names can be acquired by calling
	 * {@link #getVariableNames}.
	 */
	private class ProjectionVariableCollector extends ASTVisitorBase {

		private Set<String> variableNames = new LinkedHashSet<String>();

		public Set<String> getVariableNames() {
			return variableNames;
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
			variableNames.add(node.getName());
			return super.visit(node, data);
		}
	}
}
