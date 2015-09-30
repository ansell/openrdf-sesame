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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A "multi-projection" that can produce multiple solutions from a single set of
 * bindings.
 */
public class MultiProjection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lists of projections.
	 */
	private List<ProjectionElemList> projections = new ArrayList<ProjectionElemList>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjection() {
	}

	public MultiProjection(TupleExpr arg) {
		super(arg);
	}

	public MultiProjection(TupleExpr arg, Iterable<ProjectionElemList> projections) {
		this(arg);
		addProjections(projections);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ProjectionElemList> getProjections() {
		return Collections.unmodifiableList(projections);
	}

	public void setProjections(Iterable<ProjectionElemList> projections) {
		this.projections.clear();
		addProjections(projections);
	}

	public void addProjections(Iterable<ProjectionElemList> projections) {
		for (ProjectionElemList projection : projections) {
			addProjection(projection);
		}
	}

	public void addProjection(ProjectionElemList projection) {
		assert projection != null : "projection must not be null";
		projections.add(projection);
		projection.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		for (ProjectionElemList projElemList : projections) {
			bindingNames.addAll(projElemList.getTargetNames());
		}

		return bindingNames;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		if (projections.size() >= 1) {
			Set<String> assuredSourceNames = getArg().getAssuredBindingNames();

			bindingNames.addAll(projections.get(0).getTargetNamesFor(assuredSourceNames));

			for (int i = 1; i < projections.size(); i++) {
				bindingNames.retainAll(projections.get(i).getTargetNamesFor(assuredSourceNames));
			}
		}

		return bindingNames;
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
		for (ProjectionElemList projElemList : projections) {
			projElemList.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(projections, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MultiProjection && super.equals(other)) {
			MultiProjection o = (MultiProjection)other;
			return projections.equals(o.getProjections());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ projections.hashCode();
	}

	@Override
	public MultiProjection clone() {
		MultiProjection clone = (MultiProjection)super.clone();

		clone.projections = new ArrayList<ProjectionElemList>(getProjections().size());
		for (ProjectionElemList pe : getProjections()) {
			clone.addProjection(pe.clone());
		}

		return clone;
	}
}
