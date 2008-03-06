package org.openrdf.query.algebra;

/**
 * @author David Huynh
 */
public class Max extends UnaryValueOperator implements AggregateOperator {

	public Max(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String toString() {
		return "MAX";
	}

	public ValueExpr cloneValueExpr() {
		return new Max(getArg().cloneValueExpr());
	}
}
