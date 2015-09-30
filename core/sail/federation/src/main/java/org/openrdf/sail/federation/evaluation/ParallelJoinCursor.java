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

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * Iterate the left side and evaluate the right side in separate thread, only
 * iterate the right side in the controlling thread.
 * 
 * @author James Leigh
 */
public class ParallelJoinCursor extends LookAheadIteration<BindingSet, QueryEvaluationException> implements
		Runnable
{

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final TupleExpr rightArg;

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

	public ParallelJoinCursor(EvaluationStrategy strategy,
			CloseableIteration<BindingSet, QueryEvaluationException> leftIter, TupleExpr rightArg)
		throws QueryEvaluationException
	{
		super();
		this.strategy = strategy;
		this.leftIter = leftIter;
		this.rightArg = rightArg;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void run() {
		evaluationThread = Thread.currentThread();
		try {
			while (!closed && leftIter.hasNext()) {
				rightQueue.put(strategy.evaluate(rightArg, leftIter.next()));
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
		String left = leftIter.toString().replace("\n", "\n\t");
		String right = (null == rightIter) ? rightArg.toString() : rightIter.toString();
		return "ParallelJoin\n\t" + left + "\n\t" + right.replace("\n", "\n\t");
	}
}
