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
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.SimpleEvaluationStrategy;

public class ZeroLengthPathIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {
	/**
	 * We potentially have to fit all resources in this set
	 * so let's start of with a reasonably big size.
	 */
	private static final int INITIAL_CAPACITY = 10000;
	private static final String ANON_SUBJECT_VAR = "zero-length-internal-start";
	private static final String ANON_PREDICATE_VAR = "zero-length-internal-pred";
	private static final String ANON_OBJECT_VAR = "zero-length-internal-end";
	private static final String ANON_SEQUENCE_VAR = "zero-length-internal-seq";

	private QueryBindingSet result;

	private Var subjectVar;

	private Var objVar;

	private Value subj;

	private Value obj;

	private BindingSet bindings;

	private CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private Set<Value> reportedValues;

	private Var contextVar;

	private final EvaluationStrategy evaluationStrategy;

	public ZeroLengthPathIteration(SimpleEvaluationStrategy evaluationStrategyImpl, Var subjectVar, Var objVar,
			Value subj, Value obj, Var contextVar, BindingSet bindings)
	{
		this.evaluationStrategy = evaluationStrategyImpl;
		result = new QueryBindingSet(bindings);
		this.subjectVar = subjectVar;
		this.objVar = objVar;
		this.contextVar = contextVar;
		this.subj = subj;
		this.obj = obj;
		this.bindings = bindings;
	}

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		if (subj == null && obj == null) {
			if (this.reportedValues == null) {
				reportedValues = makeSet();
			}
			if (this.iter == null) {
				// join with a sequence so we iterate over every entry twice
				QueryBindingSet bs1 = new QueryBindingSet(1);
				bs1.addBinding(ANON_SEQUENCE_VAR, ValueFactoryImpl.getInstance().createLiteral("subject"));
				QueryBindingSet bs2 = new QueryBindingSet(1);
				bs2.addBinding(ANON_SEQUENCE_VAR, ValueFactoryImpl.getInstance().createLiteral("object"));
				List<BindingSet> seqList = Arrays.<BindingSet>asList(bs1, bs2);
				iter = new CrossProductIteration(createIteration(), seqList);
			}

			while (iter.hasNext()) {
				BindingSet bs = iter.next();

				boolean isSubjOrObj = bs.getValue(ANON_SEQUENCE_VAR).stringValue().equals("subject");
				String endpointVarName = isSubjOrObj ? ANON_SUBJECT_VAR : ANON_OBJECT_VAR;
				Value v = bs.getValue(endpointVarName);

				if (add(reportedValues, v)) {
					QueryBindingSet next = new QueryBindingSet();
					next.addBinding(subjectVar.getName(), v);
					next.addBinding(objVar.getName(), v);
					if (contextVar != null) {
						Value context = bs.getValue(contextVar.getName());
						if (context != null) {
							next.addBinding(contextVar.getName(), context);
						}
					}
					return next;
				}
			}
			iter.close();

			// if we're done, throw away the cached list of values to avoid hogging
			// resources
			reportedValues = null;
			return null;
		}
		else {
			if (result != null) {
				if (obj == null && subj != null) {
					result.addBinding(objVar.getName(), subj);
				}
				else if (subj == null && obj != null) {
					result.addBinding(subjectVar.getName(), obj);
				}
				else if (subj != null && subj.equals(obj)) {
					// empty bindings
					// (result but nothing to bind as subjectVar and objVar are both fixed)
				}
				else {
					result = null;
				}
			}

			QueryBindingSet next = result;
			result = null;
			return next;
		}
	}

	/**
	 * add param v to the set reportedValues2
	 * 
	 * @param reportedValues2
	 * @param v
	 * @return true if v added to set and not yet present
	 */
	protected boolean add(Set<Value> reportedValues2, Value v)
		throws QueryEvaluationException
	{
		return reportedValues2.add(v);
	}

	private CloseableIteration<BindingSet, QueryEvaluationException> createIteration()
		throws QueryEvaluationException
	{
		Var startVar = createAnonVar(ANON_SUBJECT_VAR);
		Var predicate = createAnonVar(ANON_PREDICATE_VAR);
		Var endVar = createAnonVar(ANON_OBJECT_VAR);

		StatementPattern subjects = new StatementPattern(startVar, predicate, endVar);

		if (contextVar != null) {
			subjects.setScope(Scope.NAMED_CONTEXTS);
			subjects.setContextVar(contextVar);
		}
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluationStrategy.evaluate(
				subjects, bindings);

		return iter;
	}

	private Set<Value> makeSet() {
		return new HashSet<Value>(INITIAL_CAPACITY);
	}

	public Var createAnonVar(String varName) {
		Var var = new Var(varName);
		var.setAnonymous(true);
		return var;
	}
}