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
package org.eclipse.rdf4j.query.parser.serql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.query.algebra.And;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.SingletonSet;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;

/**
 * A graph pattern consisting of (required and optional) tuple expressions and
 * boolean constraints.
 */
class GraphPattern {

	/**
	 * The graph pattern's parent, if any.
	 */
	private GraphPattern parent = null;

	/**
	 * The context of this graph pattern.
	 */
	private Var contextVar;

	/**
	 * The StatementPattern-scope of this graph pattern.
	 */
	private StatementPattern.Scope spScope = StatementPattern.Scope.DEFAULT_CONTEXTS;

	/**
	 * The required tuple expressions in this graph pattern.
	 */
	private List<TupleExpr> requiredTEs = new ArrayList<TupleExpr>();

	/**
	 * The optional tuple expressions and their constraints in this graph
	 * pattern.
	 */
	private List<OptionalTupleExpr> optionalTEs = new ArrayList<OptionalTupleExpr>();

	/**
	 * The boolean constraints in this graph pattern.
	 */
	private List<ValueExpr> constraints = new ArrayList<ValueExpr>();

	/**
	 * Creates a new graph pattern.
	 */
	public GraphPattern() {
	}

	/**
	 * Creates a new graph pattern that inherits the context and scope from a
	 * parent graph pattern.
	 */
	public GraphPattern(GraphPattern parent) {
		if (parent != null) {
			this.parent = parent;
			contextVar = parent.contextVar;
			spScope = parent.spScope;
		}
	}

	public GraphPattern getParent() {
		return parent;
	}

	public void setContextVar(Var contextVar) {
		this.contextVar = contextVar;
	}

	public Var getContextVar() {
		return contextVar;
	}

	public void setStatementPatternScope(StatementPattern.Scope spScope) {
		this.spScope = spScope;
	}

	public StatementPattern.Scope getStatementPatternScope() {
		return spScope;
	}

	public void addRequiredTE(TupleExpr te) {
		requiredTEs.add(te);
	}

	public List<TupleExpr> getRequiredTEs() {
		return Collections.unmodifiableList(requiredTEs);
	}

	public void addOptionalTE(GraphPattern gp) {
		List<ValueExpr> constraints = gp.removeAllConstraints();
		TupleExpr tupleExpr = gp.buildTupleExpr();

		OptionalTupleExpr optTE;

		if (constraints.isEmpty()) {
			optTE = new OptionalTupleExpr(tupleExpr);
		}
		else {
			ValueExpr constraint = constraints.get(0);
			for (int i = 1; i < constraints.size(); i++) {
				constraint = new And(constraint, constraints.get(i));
			}

			optTE = new OptionalTupleExpr(tupleExpr, constraint);
		}

		optionalTEs.add(optTE);
	}

	public List<OptionalTupleExpr> getOptionalTEs() {
		return Collections.unmodifiableList(optionalTEs);
	}

	public void addConstraint(ValueExpr constraint) {
		constraints.add(constraint);
	}

	public void addConstraints(Collection<ValueExpr> constraints) {
		this.constraints.addAll(constraints);
	}

	public List<ValueExpr> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public List<ValueExpr> removeAllConstraints() {
		List<ValueExpr> constraints = this.constraints;
		this.constraints = new ArrayList<ValueExpr>();
		return constraints;
	}

	/**
	 * Removes all tuple expressions and constraints.
	 */
	public void clear() {
		requiredTEs.clear();
		optionalTEs.clear();
		constraints.clear();
	}

	/**
	 * Builds a combined tuple expression from the tuple expressions and
	 * constraints in this graph pattern.
	 * 
	 * @return A tuple expression for this graph pattern.
	 */
	public TupleExpr buildTupleExpr() {
		TupleExpr result;

		if (requiredTEs.isEmpty()) {
			result = new SingletonSet();
		}
		else {
			result = requiredTEs.get(0);

			for (int i = 1; i < requiredTEs.size(); i++) {
				result = new Join(result, requiredTEs.get(i));
			}
		}

		for (OptionalTupleExpr optTE : optionalTEs) {
			if (optTE.hasConstraint()) {
				result = new LeftJoin(result, optTE.getTupleExpr(), optTE.getConstraint());
			}
			else {
				result = new LeftJoin(result, optTE.getTupleExpr());
			}
		}

		for (ValueExpr constraint : constraints) {
			result = new Filter(result, constraint);
		}

		return result;
	}
}
