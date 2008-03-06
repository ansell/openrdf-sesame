/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.Collections;
import java.util.Set;


/**
 * A tuple expression that contains exactly one solution with zero bindings.
 */
public class SingletonSet extends TupleExpr {

	public Set<String> getBindingNames() {
		return Collections.emptySet();
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}
	
	public String toString() {
		return "SingletonSet";
	}
}
