/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;


/**
 * The SERVICE keyword as defined in <a href="http://www.w3.org/TR/sparql11-federated-
 * query/#defn_service">SERVICE definition</a>. The service expression is evaluated at 
 * the specified service URI. If the service reference is a variable, a value for this
 * variable must be available at evaluation time (e.g. from earlier computations). 
 * 
 * @author Andreas Schwarte
 */
public class Service extends UnaryTupleOperator {

	
	/*-----------*
	 * Variables *
	 *-----------*/
	
	private Var serviceRef;

		
	/*--------------*
	 * Constructors *
	 *--------------*/
	
	public Service(Var serviceRef, TupleExpr serviceExpr) {
		super(serviceExpr);
		this.serviceRef = serviceRef;
	}
		
	
	/*---------*
	 * Methods *
	 *---------*/
	
	public Var getServiceRef() {
		return this.serviceRef;
	}
	
	public TupleExpr getServiceExpr() {
		return this.arg;
	}
	
	public void setServiceRef(Var serviceRef) {
		this.serviceRef = serviceRef;
	}
	
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
			throws X {
		visitor.meet(this);		
	}
	
	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		serviceRef.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (serviceRef == current) {
			setServiceRef((Var)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Service && super.equals(other)) {
			Service o = (Service)other;
			return serviceRef.equals(o.getServiceRef());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ serviceRef.hashCode();
	}

	@Override
	public Service clone() {
		Service clone = (Service)super.clone();
		clone.setServiceRef(serviceRef.clone());
		return clone;
	}
}
