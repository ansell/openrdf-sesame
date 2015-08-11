/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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