package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A natural join between two tuple expressions, but which due to scoping issues
 * should always be evaluated in a bottom up manner. This is in contrast with
 * the standard {@link Join}, which can be evaluated in an interleaved manner.
 * BottomUpJoin is applied in case one of the two arguments of the join involves
 * a subquery.
 * 
 * @author Ruslan Velkov
 * @author Jeen Broekstra
 */
public class BottomUpJoin extends BinaryTupleOperator {

	public BottomUpJoin(TupleExpr left, TupleExpr right) {
		super(left, right);
	}

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		return getBindingNames();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BottomUpJoin && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "BottomUpJoin".hashCode();
	}

	@Override
	public BottomUpJoin clone() {
		return (BottomUpJoin)super.clone();
	}

}
