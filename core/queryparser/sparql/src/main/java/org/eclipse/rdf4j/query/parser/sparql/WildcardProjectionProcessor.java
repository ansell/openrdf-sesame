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
package org.eclipse.rdf4j.query.parser.sparql;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDescribe;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDescribeQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTProjectionElem;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTSelect;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTSelectQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTVar;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTWhereClause;
import org.eclipse.rdf4j.query.parser.sparql.ast.Node;
import org.eclipse.rdf4j.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

/**
 * Processes 'wildcard' projections, making them explicit by adding the
 * appropriate variable nodes to them.
 * 
 * @author arjohn
 * @author Jeen Broekstra
 */
public class WildcardProjectionProcessor extends AbstractASTVisitor {

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		ASTQuery queryNode = qc.getQuery();

		// scan for nested SELECT clauses in the query
		if (queryNode != null) {
			ASTWhereClause queryBody = queryNode.getWhereClause();

			// DESCRIBE queries can be without a query body sometimes
			if (queryBody != null) {
				SelectClauseCollector collector = new SelectClauseCollector();
				try {
					queryBody.jjtAccept(collector, null);

					Set<ASTSelect> selectClauses = collector.getSelectClauses();

					for (ASTSelect selectClause : selectClauses) {
						if (selectClause.isWildcard()) {
							ASTSelectQuery q = (ASTSelectQuery)selectClause.jjtGetParent();

							addQueryVars(q.getWhereClause(), selectClause);
							selectClause.setWildcard(false);
						}
					}

				}
				catch (VisitorException e) {
					throw new MalformedQueryException(e);
				}
			}
		}

		if (queryNode instanceof ASTSelectQuery) {
			// check for wildcard in upper SELECT query

			ASTSelectQuery selectQuery = (ASTSelectQuery)queryNode;
			ASTSelect selectClause = selectQuery.getSelect();
			if (selectClause.isWildcard()) {
				addQueryVars(selectQuery.getWhereClause(), selectClause);
				selectClause.setWildcard(false);
			}
		}
		else if (queryNode instanceof ASTDescribeQuery) {
			// check for possible wildcard in DESCRIBE query
			ASTDescribeQuery describeQuery = (ASTDescribeQuery)queryNode;
			ASTDescribe describeClause = describeQuery.getDescribe();

			if (describeClause.isWildcard()) {
				addQueryVars(describeQuery.getWhereClause(), describeClause);
				describeClause.setWildcard(false);
			}
		}
	}

	private static void addQueryVars(ASTWhereClause queryBody, Node wildcardNode)
		throws MalformedQueryException
	{
		QueryVariableCollector visitor = new QueryVariableCollector();

		try {
			// Collect variable names from query
			queryBody.jjtAccept(visitor, null);

			// Adds ASTVar nodes to the ASTProjectionElem nodes and to the parent
			for (String varName : visitor.getVariableNames()) {
				ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
				ASTProjectionElem projectionElemNode = new ASTProjectionElem(
						SyntaxTreeBuilderTreeConstants.JJTPROJECTIONELEM);

				varNode.setName(varName);
				projectionElemNode.jjtAppendChild(varNode);
				varNode.jjtSetParent(projectionElemNode);

				wildcardNode.jjtAppendChild(projectionElemNode);
				projectionElemNode.jjtSetParent(wildcardNode);

			}

		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	/*------------------------------------*
	 * Inner class QueryVariableCollector *
	 *------------------------------------*/

	private static class QueryVariableCollector extends AbstractASTVisitor {

		private Set<String> variableNames = new LinkedHashSet<String>();

		public Set<String> getVariableNames() {
			return variableNames;
		}

		@Override
		public Object visit(ASTSelectQuery node, Object data)
			throws VisitorException
		{
			// stop visitor from processing body of sub-select, only add variables
			// from the projection
			return visit(node.getSelect(), data);
		}

		@Override
		public Object visit(ASTProjectionElem node, Object data)
			throws VisitorException
		{
			// only include the actual alias from a projection element in a
			// subselect, not any variables used as
			// input to a function
			String alias = node.getAlias();
			if (alias != null) {
				variableNames.add(alias);
				return null;
			}
			else {
				return super.visit(node, data);
			}
		}

		@Override
		public Object visit(ASTVar node, Object data)
			throws VisitorException
		{
			if (!node.isAnonymous()) {
				variableNames.add(node.getName());
			}
			return super.visit(node, data);
		}
	}

	/*------------------------------------*
	 * Inner class SelectClauseCollector  *
	 *------------------------------------*/

	private static class SelectClauseCollector extends AbstractASTVisitor {

		private Set<ASTSelect> selectClauses = new LinkedHashSet<ASTSelect>();

		public Set<ASTSelect> getSelectClauses() {
			return selectClauses;
		}

		@Override
		public Object visit(ASTSelect node, Object data)
			throws VisitorException
		{
			selectClauses.add(node);
			return super.visit(node, data);
		}
	}
}
