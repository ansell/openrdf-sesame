/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.List;
import java.util.Set;

import org.openrdf.query.BindingSet;

/**
 */
public class BindingSetAssignment extends QueryModelNodeBase implements TupleExpr {

	private List<BindingSet> bindingSets;
	
	public Set<String> getBindingNames() {
		return getAssuredBindingNames();
	}

	public Set<String> getAssuredBindingNames() {
		// TODO implement more robustly.
		return bindingSets.get(0).getBindingNames();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BindingSetAssignment;
	}

	@Override
	public int hashCode() {
		return "BindingSetAssignment".hashCode();
	}

	@Override
	public BindingSetAssignment clone() {
		return (BindingSetAssignment)super.clone();
	}

	/**
	 * @param bindingSets The bindingSets to set.
	 */
	public void setBindingSets(List<BindingSet> bindingSets) {
		this.bindingSets = bindingSets;
	}

	/**
	 * @return Returns the bindingSets.
	 */
	public List<BindingSet> getBindingSets() {
		return bindingSets;
	}

	@Override
	public String getSignature() {
		String signature = super.getSignature();
		
		signature += " (" + this.getBindingSets().toString() + ")";
		
		return signature;
	}
}
