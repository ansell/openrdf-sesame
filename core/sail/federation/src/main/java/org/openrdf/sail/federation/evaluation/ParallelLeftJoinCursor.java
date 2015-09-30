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
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;
import info.aduna.iteration.SingletonIteration;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * Transform the condition into a filter and the right side into an
 * {@link AlternativeCursor}, then evaluate as a {@link ParallelJoinCursor}.
 * 
 * @author James Leigh
 */
public class ParallelLeftJoinCursor extends LookAheadIteration<BindingSet, QueryEvaluationException>
		implements Runnable
{

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String LF_TAB = "\n\t";

	private final EvaluationStrategy strategy;

	private final LeftJoin join;

	/**
	 * The set of binding names that are "in scope" for the filter. The filter
	 * must not include bindings that are (only) included because of the
	 * depth-first evaluation strategy in the evaluation of the constraint.
	 */
	private final Set<String> scopeBindingNames;

	/*-----------*
	 * Variables *
	 *-----------*/

	private volatile Thread evaluationThread;

	private final CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	private volatile boolean closed;

	private final QueueCursor<CloseableIteration<BindingSet, QueryEvaluationException>> rightQueue = new QueueCursor<CloseableIteration<BindingSet, QueryEvaluationException>>(
			1024);

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ParallelLeftJoinCursor(EvaluationStrategy strategy, LeftJoin join, BindingSet bindings)
		throws QueryEvaluationException
	{
		super();
		this.strategy = strategy;
		this.join = join;
		this.scopeBindingNames = join.getBindingNames();
		this.leftIter = strategy.evaluate(join.getLeftArg(), bindings);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void run() {
		evaluationThread = Thread.currentThread();
		try {
			ValueExpr condition = join.getCondition();
			while (!closed && leftIter.hasNext()) {
				BindingSet leftBindings = leftIter.next();
				addToRightQueue(condition, leftBindings);
			}
		}
		catch (RuntimeException e) {
			rightQueue.toss(e);
		}
		catch (InterruptedException e) {
			// stop
		}
		finally {
			evaluationThread = null; // NOPMD
			rightQueue.done();
		}
	}

	private void addToRightQueue(ValueExpr condition, BindingSet leftBindings)
		throws QueryEvaluationException, InterruptedException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result = strategy.evaluate(join.getRightArg(),
				leftBindings);
		if (condition != null) {
			result = new FilterCursor(result, condition, scopeBindingNames, strategy);
		}
		CloseableIteration<BindingSet, QueryEvaluationException> alt = new SingletonIteration<BindingSet, QueryEvaluationException>(
				leftBindings);
		rightQueue.put(new AlternativeCursor<BindingSet>(result, alt));
	}

	@Override
	public synchronized BindingSet getNextElement()
		throws QueryEvaluationException
	{
		BindingSet result = null;
		while (rightIter != null || rightQueue.hasNext()) {
			if (rightIter == null) {
				rightIter = rightQueue.next();
			}
			if (rightIter.hasNext()) {
				result = rightIter.next();
				break;
			}
			else {
				rightIter.close();
				rightIter = null; // NOPMD
			}
		}

		return result;
	}

	@Override
	public synchronized void handleClose()
		throws QueryEvaluationException
	{
		closed = true;
		if (evaluationThread != null) {
			evaluationThread.interrupt();
		}
		if (rightIter != null) {
			rightIter.close();
			rightIter = null; // NOPMD
		}

		leftIter.close();
	}

	@Override
	public String toString() {
		String left = leftIter.toString().replace("\n", LF_TAB);
		String right = (null == rightIter) ? join.getRightArg().toString() : rightIter.toString();
		ValueExpr condition = join.getCondition();
		String filter = (null == condition) ? "" : condition.toString().trim().replace("\n", LF_TAB);
		return "ParallelLeftJoin " + filter + LF_TAB + left + LF_TAB + right.replace("\n", LF_TAB);
	}
}
