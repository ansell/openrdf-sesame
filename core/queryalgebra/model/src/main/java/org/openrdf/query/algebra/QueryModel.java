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
		if (defaultGraphs.isEmpty() && namedGraphs.isEmpty()) {
			return getTupleExpr().toString();
		}
		StringBuilder sb = new StringBuilder();
		for (URI uri : defaultGraphs) {
			sb.append("FROM ");
			appendURI(sb, uri);
		}
		for (URI uri : namedGraphs) {
			sb.append("FROM NAMED ");
			appendURI(sb, uri);
		}
		sb.append(getTupleExpr().toString());
		return sb.toString();
	}

	private void appendURI(StringBuilder sb, URI uri) {
		String str = uri.toString();
		if (str.length() > 50) {
			sb.append("<").append(str, 0, 19).append("..");
			sb.append(str, str.length() - 29, str.length()).append(">\n");
		}
		else {
			sb.append("<").append(uri).append(">\n");
		}
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
