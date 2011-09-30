/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;


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
	
	/* a string representation of the inner expression (e.g. extracted during parsing) */
	private String serviceExpressionString;
	
	private Set<String> serviceVars;
	
	/* the prefix declarations, potentially null */
	private Map<String, String> prefixDeclarations;
	
	/* the computed prefix string or empty string. needs to be computed only once*/
	private String computedPrefixString;
	
	private boolean silent;
	
		
	/*--------------*
	 * Constructors *
	 *--------------*/


	public Service(Var serviceRef, TupleExpr serviceExpr, String serviceExpressionString, Map<String, String> prefixDeclarations, boolean silent) {
		super(serviceExpr);
		this.serviceRef = serviceRef;
		setExpressionString(serviceExpressionString);
		this.serviceVars = computeServiceVars(serviceExpr);
		this.prefixDeclarations = prefixDeclarations;
		this.computedPrefixString = computePrefixString(prefixDeclarations);
		this.silent = silent;
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
			
	/**
	 * @return Returns the silent.
	 */
	public boolean isSilent() {
		return silent;
	}

	/**
	 * A computed prefix string from available prefix declarations.
	 * 
	 * @return Returns the computedPrefixString.
	 */
	public String getComputedPrefixString() {
		return computedPrefixString;
	}
	
	/**
	 * @return Returns the prefixDeclarations.
	 */
	public Map<String, String> getPrefixDeclarations() {
		return prefixDeclarations;
	}

	/**
	 * @param prefixDeclarations The prefixDeclarations to set.
	 */
	public void setPrefixDeclarations(Map<String, String> prefixDeclarations) {
		this.prefixDeclarations = prefixDeclarations;
	}

	/**
	 * The SERVICE expression, either complete or just the expression
	 * 
	 * e.g. "SERVICE <url> { ... }" becomes " ... "
	 * 
	 * @param serviceExpressionString 
	 * 			the inner expression as SPARQL String representation
	
	 */
	public void setExpressionString(String serviceExpressionString) {
		this.serviceExpressionString = parseServiceExpression(serviceExpressionString);
	}
	
	/**
	 * @return Returns the serviceExpressionString.
	 */
	public String getServiceExpressionString() {
		return serviceExpressionString;
	}

	/**
	 * @return Returns the serviceVars.
	 */
	public Set<String> getServiceVars() {
		return serviceVars;
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
	
	
	/**
	 * Compute the variable names occurring in the service
	 * expression using tree traversal, since these are necessary 
	 * for building the SPARQL query.
	 * 
	 * @return
	 */
	private Set<String> computeServiceVars(TupleExpr serviceExpression) {
		final Set<String> res = new HashSet<String>();
		serviceExpression.visit(new QueryModelVisitorBase<RuntimeException>() {
			@Override
			public void meet(Var node) throws RuntimeException {
				if (!node.hasValue())
					res.add(node.getName());
			}	
			// TODO maybe stop tree traversal in nested SERVICE?
			// TODO special case handling for BIND
		});
		return res;
	}
	
	
	/**
	 * Compute the prefix string only once to avoid computation overhead 
	 * during evaluation.
	 * 
	 * @param prefixDeclarations
	 * @return a Prefix String or an empty string if there are no prefixes 
	 */
	private String computePrefixString(Map<String, String> prefixDeclarations) {
		if (prefixDeclarations==null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		for (String prefix : prefixDeclarations.keySet()) {
			String uri = prefixDeclarations.get(prefix);
			sb.append("PREFIX ").append(prefix).append(":").append(" <").append(uri).append("> ");
		}
		return sb.toString();
	}

	/**
	 * Parses a service expression to just have the inner expresion, 
	 * 
	 * e.g. from something like "SERVICE <url> { ... }" becomes " ... "
	 *  
	 * @param serviceExpression
	 * @return
	 */
	private String parseServiceExpression(String serviceExpression) {
		
		// TODO fixme (currently the string does not start with service for two SERVICE or nested)
		if (true || serviceExpression.toLowerCase().startsWith("service")) {
			return serviceExpression.substring(serviceExpression.indexOf("{")+1, serviceExpression.length()-2);
		} 
		return serviceExpression;
	}
}
