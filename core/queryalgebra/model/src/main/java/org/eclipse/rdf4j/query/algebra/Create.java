/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra;


/**
 * @author jeen
 */
public class Create extends AbstractQueryModelNode implements UpdateExpr {

	private ValueConstant graph;

	private boolean silent;
	
	public Create() {
		super();
	}
	
	public Create(ValueConstant graph) {
		super();
		setGraph(graph);
	}

	@Override
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		if (graph != null) {
			graph.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (graph == current) {
			setGraph((ValueConstant)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof Create) {
			Create o = (Create)other;
			return silent == o.silent
					&& nullEquals(graph, o.graph);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = silent ? 1 :0;
		if(graph != null) {
			result ^= graph.hashCode();
		}
		return result;
	}

	@Override
	public Create clone() {
		Create clone = new Create();
		clone.setSilent(isSilent());
		if (getGraph() != null) {
			clone.setGraph(getGraph().clone());
		}
		return clone;
	}

	/**
	 * @param graph
	 *        The graph to set.
	 */
	public void setGraph(ValueConstant graph) {
		this.graph = graph;
	}

	/**
	 * @return Returns the graph.
	 */
	public ValueConstant getGraph() {
		return graph;
	}

	/**
	 * @param silent The silent to set.
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/**
	 * @return Returns the silent.
	 */
	@Override
	public boolean isSilent() {
		return silent;
	}

}
