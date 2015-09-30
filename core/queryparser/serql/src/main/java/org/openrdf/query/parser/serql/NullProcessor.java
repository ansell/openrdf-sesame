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

	private static class NullVisitor extends AbstractASTVisitor {

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
