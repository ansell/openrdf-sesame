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
package org.openrdf.query.algebra;

import java.util.Set;

/**
 * A generalized projection (allowing the bindings to be renamed) on a tuple
 * expression.
 */
public class Projection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ProjectionElemList projElemList = new ProjectionElemList();

	private Var projectionContext = null;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public Projection() {
	}

	public Projection(TupleExpr arg) {
		super(arg);
	}

	public Projection(TupleExpr arg, ProjectionElemList elements) {
		this(arg);
		setProjectionElemList(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ProjectionElemList getProjectionElemList() {
		return projElemList;
	}

	public void setProjectionElemList(ProjectionElemList projElemList) {
		this.projElemList = projElemList;
		projElemList.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		return projElemList.getTargetNames();
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		// Return all target binding names for which the source binding is assured
		// by the argument
		return projElemList.getTargetNamesFor(getArg().getAssuredBindingNames());
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
		projElemList.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (projElemList == current) {
			setProjectionElemList((ProjectionElemList)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Projection && super.equals(other)) {
			Projection o = (Projection)other;
			return projElemList.equals(o.getProjectionElemList());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ projElemList.hashCode();
	}

	@Override
	public Projection clone() {
		Projection clone = (Projection)super.clone();
		clone.setProjectionElemList(getProjectionElemList().clone());
		return clone;
	}

	/**
	 * @return Returns the projectionContext.
	 */
	public Var getProjectionContext() {
		return projectionContext;
	}

	/**
	 * @param projectionContext The projectionContext to set.
	 */
	public void setProjectionContext(Var projectionContext) {
		this.projectionContext = projectionContext;
	}

}
