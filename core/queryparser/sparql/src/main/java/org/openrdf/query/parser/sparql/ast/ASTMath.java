/* Generated By:JJTree: Do not edit this line. ASTMath.java */

package org.openrdf.query.parser.sparql.ast;

import org.openrdf.query.algebra.MathExpr.MathOp;

public class ASTMath extends SimpleNode {

	private MathOp operator;

	public ASTMath(int id) {
		super(id);
	}

	public ASTMath(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public MathOp getOperator() {
		return operator;
	}

	public void setOperator(MathOp operator) {
		this.operator = operator;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + operator + ")";
	}
}
