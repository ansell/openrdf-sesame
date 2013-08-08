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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * Iteration that implements a simplified version of Symmetric Concise Bounded
 * Description (omitting reified statements).
 * 
 * @author Jeen Broekstra
 * @since 2.7.4
 * @see http://www.w3.org/Submission/CBD/#alternatives
 */
public class DescribeIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	private final static String VARNAME_SUBJECT = "subject";

	private final static String VARNAME_PREDICATE = "predicate";

	private final static String VARNAME_OBJECT = "object";

	private final List<String> valueExprs;

	private final EvaluationStrategy strategy;

	private Value startValue;

	private final Queue<BNode> nodeQueue = new ArrayDeque<BNode>();

	private final Set<BNode> processedNodes = new HashSet<BNode>();

	private CloseableIteration<BindingSet, QueryEvaluationException> currentValueExprIter;

	private enum Mode {
		OUTGOING_LINKS,
		INCOMING_LINKS
	}

	private Mode currentMode = Mode.OUTGOING_LINKS;

	private Iteration<BindingSet, QueryEvaluationException> sourceIter;

	public DescribeIteration(Iteration<BindingSet, QueryEvaluationException> sourceIter,
			EvaluationStrategy strategy, List<String> valueExprs, BindingSet parentBindings)
	{
		this.strategy = strategy;
		this.sourceIter = sourceIter;
		this.valueExprs = valueExprs;
		this.parentBindings = parentBindings;
	}

	private BindingSet currentBindings;

	private int valueExprIndex;

	private BindingSet parentBindings;

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{

		if (currentValueExprIter == null) {
			if (currentBindings == null) {
				if (sourceIter.hasNext()) {
					currentBindings = sourceIter.next();
				}
				else {
					currentBindings = parentBindings;
				}
			}

			if (startValue == null) {
				String nextValueExpr = valueExprs.get(valueExprIndex++);
				if (nextValueExpr != null) {
					startValue = currentBindings.getValue(nextValueExpr);
					if (valueExprIndex == valueExprs.size()) { 
						// reached the end of the list of valueExprs, reset to 
						// read next value from source iterator if any.
						currentBindings = null;
						valueExprIndex = 0;
					}
				}
			}

			switch (currentMode) {
				case OUTGOING_LINKS:
					currentValueExprIter = createNextIteration(startValue, null);
					if (!currentValueExprIter.hasNext()) {
						// special case: start value has no outgoing links.
						// immediately switch to incoming links.
						currentValueExprIter.close();
						currentMode = Mode.INCOMING_LINKS;
						currentValueExprIter = createNextIteration(null, startValue);
					}
					break;
				case INCOMING_LINKS:
					currentValueExprIter = createNextIteration(null, startValue);
					break;
			}
		}
		else {
			while (!currentValueExprIter.hasNext() && !nodeQueue.isEmpty()) {
				// process next node in queue
				BNode nextNode = nodeQueue.poll();
				currentValueExprIter.close();
				switch (currentMode) {
					case OUTGOING_LINKS:
						currentValueExprIter = createNextIteration(nextNode, null);
						break;
					case INCOMING_LINKS:
						currentValueExprIter = createNextIteration(null, nextNode);
						break;

				}
				processedNodes.add(nextNode);
			}
		}

		if (currentValueExprIter.hasNext()) {
			BindingSet bs = currentValueExprIter.next();

			String varname = currentMode == Mode.OUTGOING_LINKS ? VARNAME_OBJECT : VARNAME_SUBJECT;

			Value v = bs.getValue(varname);
			if (v instanceof BNode) {
				if (!processedNodes.contains(v)) { // duplicate/cycle detection
					nodeQueue.add((BNode)v);
				}
			}

			if (!currentValueExprIter.hasNext() && nodeQueue.isEmpty()) {
				currentValueExprIter.close();
				currentValueExprIter = null;

				if (currentMode == Mode.OUTGOING_LINKS) {
					currentMode = Mode.INCOMING_LINKS;
				}
				else {
					// done with this valueExpr, reset to initialize next in value
					// expression queue.
					currentMode = Mode.OUTGOING_LINKS;
					startValue = null;
				}
			}

			return bs;
		}

		return null;
	}

	private CloseableIteration<BindingSet, QueryEvaluationException> createNextIteration(Value subject,
			Value object)
		throws QueryEvaluationException
	{
		if (subject == null && object == null) {
			return new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

		Var subjVar = new Var(VARNAME_SUBJECT, subject);
		Var predVar = new Var(VARNAME_PREDICATE);
		Var objVar = new Var(VARNAME_OBJECT, object);

		StatementPattern pattern = new StatementPattern(subjVar, predVar, objVar);
		return strategy.evaluate(pattern, parentBindings);
	}

}
