package org.openrdf.query.algebra;

/**
 * @author David Huynh
 */
public class Min extends UnaryValueOperator implements AggregateOperator {

	public Min(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String toString() {
		return "MIN";
	}

	public ValueExpr cloneValueExpr() {
		return new Min(getArg().cloneValueExpr());
	}
}
