/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import static org.openrdf.query.algebra.Compare.CompareOp.EQ;
import static org.openrdf.query.algebra.Compare.CompareOp.NE;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.parser.serql.ast.ASTBooleanConstant;
import org.openrdf.query.parser.serql.ast.ASTBooleanExpr;
import org.openrdf.query.parser.serql.ast.ASTBound;
import org.openrdf.query.parser.serql.ast.ASTCompare;
import org.openrdf.query.parser.serql.ast.ASTNot;
import org.openrdf.query.parser.serql.ast.ASTNull;
import org.openrdf.query.parser.serql.ast.ASTProjectionElem;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTValueExpr;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.Node;
import org.openrdf.query.parser.serql.ast.VisitorException;

/**
 * Processes {@link ASTNull} nodes in query models. Null's that appear in
 * projections are simply removed as that doesn't change the semantics. Null's
 * that appear in value comparisons are either replaced with {@link ASTBound}
 * nodes or constants.
 * 
 * @author Arjohn Kampman
 */
class NullProcessor {

	/**
	 * Processes escape sequences in ASTString objects.
	 * 
	 * @param qc
	 *        The query that needs to be processed.
	 * @throws MalformedQueryException
	 *         If an invalid escape sequence was found.
	 */
	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		NullVisitor visitor = new NullVisitor();
		try {
			qc.jjtAccept(visitor, null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	private static class NullVisitor extends ASTVisitorBase {

		protected final Logger logger = LoggerFactory.getLogger(this.getClass());

		public NullVisitor() {
		}

		@Override
		public Object visit(ASTSelect selectNode, Object data)
			throws VisitorException
		{
			Iterator<Node> iter = selectNode.jjtGetChildren().iterator();

			while (iter.hasNext()) {
				ASTProjectionElem pe = (ASTProjectionElem)iter.next();

				if (pe.getValueExpr() instanceof ASTNull) {
					logger.warn("Use of NULL values in SeRQL queries has been deprecated");
					iter.remove();
				}
			}

			return null;
		}

		@Override
		public Object visit(ASTCompare compareNode, Object data)
			throws VisitorException
		{
			boolean leftIsNull = compareNode.getLeftOperand() instanceof ASTNull;
			boolean rightIsNull = compareNode.getRightOperand() instanceof ASTNull;
			CompareOp operator = compareNode.getOperator().getValue();

			if (leftIsNull && rightIsNull) {
				switch (operator) {
					case EQ:
						logger.warn("Use of NULL values in SeRQL queries has been deprecated, use BOUND(...) instead");
						compareNode.jjtReplaceWith(new ASTBooleanConstant(true));
						break;
					case NE:
						logger.warn("Use of NULL values in SeRQL queries has been deprecated, use BOUND(...) instead");
						compareNode.jjtReplaceWith(new ASTBooleanConstant(false));
						break;
					default:
						throw new VisitorException(
								"Use of NULL values in SeRQL queries has been deprecated, use BOUND(...) instead");
				}
			}
			else if (leftIsNull || rightIsNull) {
				ASTValueExpr valueOperand;
				if (leftIsNull) {
					valueOperand = compareNode.getRightOperand();
				}
				else {
					valueOperand = compareNode.getLeftOperand();
				}

				if (valueOperand instanceof ASTVar && operator == EQ || operator == NE) {
					ASTBooleanExpr replacementNode = new ASTBound(valueOperand);

					if (operator == EQ) {
						replacementNode = new ASTNot(replacementNode);
					}

					compareNode.jjtReplaceWith(replacementNode);

					return null;
				}

				throw new VisitorException(
						"Use of NULL values in SeRQL queries has been deprecated, use BOUND(...) instead");
			}

			return null;
		}

		@Override
		public Object visit(ASTNull nullNode, Object data)
			throws VisitorException
		{
			throw new VisitorException(
					"Use of NULL values in SeRQL queries has been deprecated, use BOUND(...) instead");
		}
	}
}
