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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.serql.ast.ASTProjectionElem;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTSelectQuery;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.ASTWhere;
import org.openrdf.query.parser.serql.ast.Node;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.serql.ast.VisitorException;

/**
 * Processes projections. 'Wildcard' projections are made explicit by adding
 * projection elements with the appropriate variable nodes to them. Explicit
 * projections are checked to see if they don't contain any unbound variables.
 * 
 * @author Arjohn Kampman
 */
class ProjectionProcessor extends AbstractASTVisitor {

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		try {
			qc.jjtAccept(new ProjectionProcessor(), null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	@Override
	public Object visit(ASTSelect selectNode, Object data)
		throws VisitorException
	{
		// Collect all variables used in the body of the select query
		Set<String> bodyVars = VariableCollector.process(selectNode.jjtGetParent());

		if (selectNode.isWildcard()) {
			// Make wildcard projection explicit
			for (String varName : bodyVars) {
				ASTProjectionElem projElemNode = new ASTProjectionElem(
						SyntaxTreeBuilderTreeConstants.JJTPROJECTIONELEM);
				selectNode.jjtAppendChild(projElemNode);
				projElemNode.jjtSetParent(selectNode);

				ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
				varNode.setName(varName);
				projElemNode.jjtAppendChild(varNode);
				varNode.jjtSetParent(projElemNode);
			}

			selectNode.setWildcard(false);
		}
		else {
			// Verify that all projection vars are bound
			Set<String> projVars = new LinkedHashSet<String>();

			for (ASTProjectionElem projElem : selectNode.getProjectionElemList()) {
				projVars.addAll(VariableCollector.process(projElem.getValueExpr()));
			}

			projVars.removeAll(bodyVars);

			if (!projVars.isEmpty()) {
				StringBuilder errMsg = new StringBuilder(64);
				errMsg.append("Unbound variable(s) in projection: ");

				Iterator<String> iter = projVars.iterator();
				while (iter.hasNext()) {
					errMsg.append(iter.next());
					if (iter.hasNext()) {
						errMsg.append(", ");
					}
				}

				throw new VisitorException(errMsg.toString());
			}
		}

		return data;
	}

	/*-------------------------------*
	 * Inner class VariableCollector *
	 *-------------------------------*/

	/**
	 * Collects variable names for 'wildcard' projections. An instance of this
	 * class should be supplied to a {@link ASTSelectQuery} node. When done, the
	 * collected variable names can be acquired by calling
	 * {@link #getVariableNames}.
	 */
	private static class VariableCollector extends AbstractASTVisitor {

		public static Set<String> process(Node node)
			throws VisitorException
		{
			VariableCollector visitor = new VariableCollector();
			node.jjtAccept(visitor, null);
			return visitor.getVariableNames();
		}

		private Set<String> variableNames = new LinkedHashSet<String>();

		public Set<String> getVariableNames() {
			return variableNames;
		}

		@Override
		public Object visit(ASTSelect node, Object data)
			throws VisitorException
		{
			// Do not visit select clauses
			return data;
		}

		@Override
		public Object visit(ASTWhere node, Object data)
			throws VisitorException
		{
			// Do not visit where clauses
			return data;
		}

		@Override
		public Object visit(ASTVar node, Object data)
			throws VisitorException
		{
			variableNames.add(node.getName());
			return super.visit(node, data);
		}
	}
}
