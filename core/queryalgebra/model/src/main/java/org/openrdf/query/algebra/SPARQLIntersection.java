package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Intersection between inner projections used in SPARQL queries with inner
 * Select subqueries. The right operation between projections is intersection
 * rather than join (see issue http://www.openrdf.org/issues/browse/SES-821).
 * This is in fact an Intersection which returns a union of its binding names at
 * getBindingNames() rather than their intersection.
 * 
 * @author ruslan
 */
public class SPARQLIntersection extends Intersection {

	public SPARQLIntersection(TupleExpr left, TupleExpr right) {
		super(left, right);
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		return getBindingNames();
	}

	@Override
	public SPARQLIntersection clone() {
		return (SPARQLIntersection)super.clone();
	}

}
