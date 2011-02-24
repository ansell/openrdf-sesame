/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTCount;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * @author Jeen
 */
public class WildcardCountProcessor extends ASTVisitorBase {

	private static QueryVariableCollector collector;

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		try {
			collector = new QueryVariableCollector();
			qc.jjtAccept(collector, null);

			CountVisitor visitor = new CountVisitor();
			qc.jjtAccept(visitor, null);

		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	private static class CountVisitor extends ASTVisitorBase {

		private void addQueryVars(ASTCount node)
			throws VisitorException
		{
			if (node.isWildcard()) {
				// Adds ASTVar nodes to the ASTCount node
				for (ASTVar var : collector.getVariables()) {
					
					node.jjtAppendChild(var);
					//countVarNode.jjtSetParent(node);
				}
				node.setWildcard(false);
			}
		}

		@Override
		public Object visit(ASTCount node, Object data)
			throws VisitorException
		{
			addQueryVars(node);
			return super.visit(node, data);
		}
	}

	private static class QueryVariableCollector extends ASTVisitorBase {

		private Set<ASTVar> variables = new LinkedHashSet<ASTVar>();

		public Set<ASTVar> getVariables() {
			return variables;
		}

		@Override
		public Object visit(ASTVar node, Object data)
			throws VisitorException
		{
			if (!node.isAnonymous()) {
				variables.add(node);
			}
			return super.visit(node, data);
		}
	}

}
