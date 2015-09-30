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
package org.eclipse.rdf4j.query.algebra.evaluation.federation;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.common.iteration.LookAheadIteration;
import org.eclipse.rdf4j.http.client.QueueCursor;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;


/**
 * Base class for any join parallel join executor. Note that this class extends
 * {@link LookAheadIteration} and thus any implementation of this class is
 * applicable for pipelining when used in a different thread (access to shared
 * variables is synchronized).
 * 
 * @author Andreas Schwarte
 */
public abstract class JoinExecutorBase<T> extends LookAheadIteration<T, QueryEvaluationException> {

	protected static int NEXT_JOIN_ID = 1;

	/* Constants */
	protected final TupleExpr rightArg; // the right argument for the join

	protected final BindingSet bindings; // the bindings

	protected CloseableIteration<T, QueryEvaluationException> leftIter;

	protected CloseableIteration<T, QueryEvaluationException> rightIter;

	protected volatile boolean closed;

	protected boolean finished = false;

	protected final QueueCursor<CloseableIteration<T, QueryEvaluationException>> rightQueue = new QueueCursor<CloseableIteration<T, QueryEvaluationException>>(
			1024);

	public JoinExecutorBase(CloseableIteration<T, QueryEvaluationException> leftIter, TupleExpr rightArg,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		this.leftIter = leftIter;
		this.rightArg = rightArg;
		this.bindings = bindings;
	}

	public final void run() {

		try {
			handleBindings();
		}
		catch (Exception e) {
			toss(e);
		}
		finally {
			finished = true;
			rightQueue.done();
		}

	}

	/**
	 * Implementations must implement this method to handle bindings. Use the
	 * following as a template <code>
	 * while (!closed && leftIter.hasNext()) {
	 * 		// your code
	 * }
	 * </code> and add results to rightQueue. Note that addResult() is
	 * implemented synchronized and thus thread safe. In case you can guarantee
	 * sequential access, it is also possible to directly access rightQueue
	 */
	protected abstract void handleBindings()
		throws Exception;

	public void addResult(CloseableIteration<T, QueryEvaluationException> res) {
		/* optimization: avoid adding empty results */
		if (res instanceof EmptyIteration<?, ?>)
			return;

		try {
			rightQueue.put(res);
		}
		catch (InterruptedException e) {
			throw new RuntimeException("Error adding element to right queue", e);
		}
	}

	public void done() {
		; // no-op
	}

	public void toss(Exception e) {
		rightQueue.toss(e);
	}

	@Override
	public T getNextElement()
		throws QueryEvaluationException
	{
		// TODO check if we need to protect rightQueue from synchronized access
		// wasn't done in the original implementation either
		// if we see any weird behavior check here !!

		while (rightIter != null || rightQueue.hasNext()) {
			if (rightIter == null) {
				rightIter = rightQueue.next();
			}
			if (rightIter.hasNext()) {
				return rightIter.next();
			}
			else {
				rightIter.close();
				rightIter = null;
			}
		}

		return null;
	}

	@Override
	public void handleClose()
		throws QueryEvaluationException
	{
		closed = true;
		rightQueue.close();

		if (rightIter != null) {
			rightIter.close();
			rightIter = null;
		}

		if (leftIter != null)
			leftIter.close();
	}

	/**
	 * Gets whether this executor is finished or aborted.
	 * 
	 * @return true if this executor is finished or aborted
	 */
	public boolean isFinished() {
		return finished;
	}

}
