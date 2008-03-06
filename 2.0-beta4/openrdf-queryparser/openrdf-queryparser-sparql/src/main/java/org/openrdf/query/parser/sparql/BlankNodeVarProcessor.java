/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTBlankNode;
import org.openrdf.query.parser.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.sparql.ast.ASTCollection;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Processes blank nodes in the query body, replacing them with variables while
 * retaining scope.
 * 
 * @author Arjohn Kampman
 */
class BlankNodeVarProcessor extends ASTVisitorBase {

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		try {
			qc.jjtAccept(new BlankNodeToVarConverter(), null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	/*-------------------------------------*
	 * Inner class BlankNodeToVarConverter *
	 *-------------------------------------*/

	private static class BlankNodeToVarConverter extends ASTVisitorBase {

		private int _anonVarNo = 1;

		private Map<String, String> _conversionMap = new HashMap<String, String>();

		private String _createAnonVarName() {
			return "-anon-" + _anonVarNo++;
		}

		public Object visit(ASTBasicGraphPattern node, Object data)
			throws VisitorException
		{
			// Blank nodes are scope to Basic Graph Patterns
			_conversionMap.clear();

			return super.visit(node, data);
		}

		public Object visit(ASTBlankNode node, Object data)
			throws VisitorException
		{
			String varName = null;
			String bnodeID = node.getID();

			if (bnodeID != null) {
				varName = _conversionMap.get(bnodeID);
			}

			if (varName == null) {
				varName = _createAnonVarName();

				if (bnodeID != null) {
					_conversionMap.put(bnodeID, varName);
				}
			}

			ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
			varNode.setName(varName);
			varNode.setAnonymous(true);

			node.jjtReplaceWith(varNode);

			return null;
		}

		public Object visit(ASTBlankNodePropertyList node, Object data)
			throws VisitorException
		{
			node.setVarName(_createAnonVarName());
			return null;
		}

		public Object visit(ASTCollection node, Object data)
			throws VisitorException
		{
			node.setVarName(_createAnonVarName());
			return null;
		}
	}
}
