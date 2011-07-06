/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author jeen
 */
public class Move extends QueryModelNodeBase implements UpdateExpr {

	private ValueConstant sourceGraph;

	private ValueConstant destinationGraph;

	private boolean silent;

	public Move() {
		super();
	}

	public Move(ValueConstant graph) {
		super();
		setSourceGraph(graph);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		if (sourceGraph != null) {
			sourceGraph.visit(visitor);
		}
		if (destinationGraph != null) {
			destinationGraph.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (sourceGraph == current) {
			setSourceGraph((ValueConstant)replacement);
		}
		else if (destinationGraph == current) {
			setDestinationGraph((ValueConstant)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Move clone() {
		Move clone = new Move();
		clone.setSilent(isSilent());
		if (getSourceGraph() != null) {
			clone.setSourceGraph(getSourceGraph().clone());
		}
		return clone;
	}

	/**
	 * @param graph
	 *        The graph to set.
	 */
	public void setSourceGraph(ValueConstant graph) {
		this.sourceGraph = graph;
	}

	/**
	 * The named graph from which to copy. If null, the default graph should be
	 * used.
	 * 
	 * @return Returns the graph.
	 */
	public ValueConstant getSourceGraph() {
		return sourceGraph;
	}

	/**
	 * @param silent
	 *        The silent to set.
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/**
	 * @return Returns the silent.
	 */
	public boolean isSilent() {
		return silent;
	}

	/**
	 * @param destinationGraph
	 *        The destinationGraph to set.
	 */
	public void setDestinationGraph(ValueConstant destinationGraph) {
		this.destinationGraph = destinationGraph;
	}

	/**
	 * The named graph to which to copy. If null, the default graph should be
	 * used.
	 * 
	 * @return Returns the destinationGraph.
	 */
	public ValueConstant getDestinationGraph() {
		return destinationGraph;
	}

}
