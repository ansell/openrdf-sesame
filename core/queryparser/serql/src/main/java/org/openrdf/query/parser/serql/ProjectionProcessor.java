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
