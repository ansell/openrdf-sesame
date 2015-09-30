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
package org.eclipse.rdf4j.query.algebra.evaluation.iterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.common.iteration.LookAheadIteration;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;

/**
 * Iteration that implements a simplified version of Symmetric Concise Bounded
 * Description (omitting reified statements).
 * 
 * @author Jeen Broekstra
 * @since 2.7.4
 * @see <a href="http://www.w3.org/Submission/CBD/#alternatives">Concise Bounded Description - alternatives</a> 
 */
public class DescribeIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	private final static String VARNAME_SUBJECT = "subject";

	private final static String VARNAME_PREDICATE = "predicate";

	private final static String VARNAME_OBJECT = "object";

	private final List<String> describeExprNames;

	private final EvaluationStrategy strategy;

	private Value startValue;

	private final Queue<BNode> nodeQueue = new ArrayDeque<BNode>();

	private final Set<BNode> processedNodes = new HashSet<BNode>();

	private CloseableIteration<BindingSet, QueryEvaluationException> currentDescribeExprIter;

	private enum Mode {
		OUTGOING_LINKS,
		INCOMING_LINKS
	}

	private Mode currentMode = Mode.OUTGOING_LINKS;

	private Iteration<BindingSet, QueryEvaluationException> sourceIter;

	public DescribeIteration(Iteration<BindingSet, QueryEvaluationException> sourceIter,
			EvaluationStrategy strategy, Set<String> describeExprNames, BindingSet parentBindings)
	{
		this.strategy = strategy;
		this.sourceIter = sourceIter;
		this.describeExprNames = new ArrayList<String>(describeExprNames);
		this.parentBindings = parentBindings;
	}

	private BindingSet currentBindings;

	private int describeExprsIndex;

	private BindingSet parentBindings;

	
	private void resetCurrentDescribeExprIter() throws QueryEvaluationException {
		while (currentDescribeExprIter == null) {
			if (currentBindings == null && startValue == null) {
				if (sourceIter.hasNext()) {
					currentBindings = sourceIter.next();
				}
				else {
					// no more bindings, therefore no more results to return.
					return;
				}
			}

			if (startValue == null) {
				String nextValueExpr = describeExprNames.get(describeExprsIndex++);
				if (nextValueExpr != null) {
					startValue = currentBindings.getValue(nextValueExpr);
					if (describeExprsIndex == describeExprNames.size()) {
						// reached the end of the list of valueExprs, reset to
						// read next value from source iterator if any.
						currentBindings = null;
						describeExprsIndex = 0;
					}
					currentMode = Mode.OUTGOING_LINKS;
				}
			}

			switch (currentMode) {
				case OUTGOING_LINKS:
					currentDescribeExprIter = createNextIteration(startValue, null);
					if (!currentDescribeExprIter.hasNext()) {
						// start value has no outgoing links.
						currentDescribeExprIter.close();
						currentDescribeExprIter = null;
						currentMode = Mode.INCOMING_LINKS;
					}
					break;
				case INCOMING_LINKS:
					currentDescribeExprIter = createNextIteration(null, startValue);
					if (!currentDescribeExprIter.hasNext()) {
						// no incoming links for this start value.
						currentDescribeExprIter.close();
						currentDescribeExprIter = null;
						startValue = null;
						currentMode = Mode.OUTGOING_LINKS;
					}
					break;
			}
		} // end while
	}
	
	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		resetCurrentDescribeExprIter();
		if (currentDescribeExprIter == null) {
			return null;
		}
		
		while (!currentDescribeExprIter.hasNext() && !nodeQueue.isEmpty()) {
			// process next node in queue
			BNode nextNode = nodeQueue.poll();
			currentDescribeExprIter.close();
			switch (currentMode) {
				case OUTGOING_LINKS:
					currentDescribeExprIter = createNextIteration(nextNode, null);
					break;
				case INCOMING_LINKS:
					currentDescribeExprIter = createNextIteration(null, nextNode);
					break;

			}
			processedNodes.add(nextNode);

			if (nodeQueue.isEmpty() && !currentDescribeExprIter.hasNext()) {
				// we have hit a blank node that has no further expansion. reset to
				// initialize next in value expression queue.
				currentDescribeExprIter.close();
				currentDescribeExprIter = null;
				
				if (currentMode == Mode.OUTGOING_LINKS) {
					currentMode = Mode.INCOMING_LINKS;
				}
				else {
					// done with this valueExpr, reset to initialize next in value
					// expression queue.
					currentMode = Mode.OUTGOING_LINKS;
					startValue = null;
				}
				
				resetCurrentDescribeExprIter();
				if (currentDescribeExprIter == null) {
					return null;
				}
			}

		}

		if (currentDescribeExprIter.hasNext()) {
			BindingSet bs = currentDescribeExprIter.next();

			String varname = currentMode == Mode.OUTGOING_LINKS ? VARNAME_OBJECT : VARNAME_SUBJECT;

			Value v = bs.getValue(varname);
			if (v instanceof BNode) {
				if (!processedNodes.contains(v)) { // duplicate/cycle detection
					nodeQueue.add((BNode)v);
				}
			}

			if (!currentDescribeExprIter.hasNext() && nodeQueue.isEmpty()) {
				currentDescribeExprIter.close();
				currentDescribeExprIter = null;

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
