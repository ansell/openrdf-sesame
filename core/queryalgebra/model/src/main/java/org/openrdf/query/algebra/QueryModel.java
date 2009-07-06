/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;

/**
 * A semantics-less query model node that is used as the root of query model
 * trees. This is a placeholder that facilitates modifications to query model
 * trees, including the replacement of the actual (semantically relevant) root
 * node with another root node.
 * 
 * @author Arjohn Kampman
 */
public class QueryModel extends UnaryTupleOperator {

	private static final long serialVersionUID = 6117214014374287092L;

	/**
	 * The dataset that was specified in the query, if any.
	 */
	protected Set<URI> defaultGraphs = new LinkedHashSet<URI>();

	protected Set<URI> namedGraphs = new LinkedHashSet<URI>();

	public QueryModel() {
		super();
	}

	public QueryModel(TupleExpr tupleExpr) {
		super(tupleExpr);
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public QueryModel(TupleExpr tupleExpr, Set<URI> defaultGraphs, Set<URI> namedGraphs) {
		this(tupleExpr);
		this.defaultGraphs.addAll(defaultGraphs);
		this.namedGraphs.addAll(namedGraphs);
	}

	/**
	 * Gets the tuple expression underlying this query.
	 */
	public TupleExpr getTupleExpr() {
		return getArg();
	}

	public Set<URI> getDefaultGraphs() {
		return defaultGraphs;
	}

	public void setDefaultGraphs(Set<URI> graphURI) {
		defaultGraphs = new LinkedHashSet<URI>(graphURI);
	}

	public Set<URI> getNamedGraphs() {
		return namedGraphs;
	}

	public void setNamedGraphs(Set<URI> graphURI) {
		namedGraphs = new LinkedHashSet<URI>(graphURI);
	}

	/**
	 * Returns a string representation of the query that can be used for
	 * debugging.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(512);

		if (!defaultGraphs.isEmpty()) {
			for (URI uri : defaultGraphs) {
				sb.append("FROM <");
				sb.append(uri);
				sb.append(">\n");
			}
		}

		if (!namedGraphs.isEmpty()) {
			for (URI uri : namedGraphs) {
				sb.append("FROM NAMED <");
				sb.append(uri);
				sb.append(">\n");
			}
		}

		sb.append(getTupleExpr());
		return sb.toString();
	}

	@Override
	public void setParentNode(QueryModelNode parent) {
		throw new UnsupportedOperationException("Not allowed to set a parent on a QueryRoot object");
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public QueryModel clone() {
		QueryModel clone = (QueryModel)super.clone();
		clone.setDefaultGraphs(getDefaultGraphs());
		clone.setNamedGraphs(getNamedGraphs());
		return clone;
	}
}
