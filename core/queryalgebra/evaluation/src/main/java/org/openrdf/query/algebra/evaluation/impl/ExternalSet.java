/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Collections;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryModelNodeBase;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.result.Cursor;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public abstract class ExternalSet extends QueryModelNodeBase implements TupleExpr {

	private static final long serialVersionUID = 3903453394409442226L;

	public Set<String> getBindingNames() {
		return Collections.emptySet();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meetOther(this);
	}

	public ExternalSet clone() {
		return (ExternalSet)super.clone();
	}

	public abstract Cursor<BindingSet> evaluate(Dataset dataset, BindingSet bindings)
		throws StoreException;

}
