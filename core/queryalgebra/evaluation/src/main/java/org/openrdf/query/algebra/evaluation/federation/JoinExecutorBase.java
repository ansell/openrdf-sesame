/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.sparql.query.QueueCursor;



/**
 * Base class for any join parallel join executor. 
 * 
 * Note that this class extends {@link LookAheadIteration} and thus any implementation of this 
 * class is applicable for pipelining when used in a different thread (access to shared
 * variables is synchronized).
 * 
 * @author Andreas Schwarte
 */
public abstract class JoinExecutorBase<T> extends LookAheadIteration<T, QueryEvaluationException> {


	
	protected static int NEXT_JOIN_ID = 1;
	
	/* Constants */
	protected final TupleExpr rightArg;						// the right argument for the join
	protected final BindingSet bindings;					// the bindings

	
	/* Variables */
	protected volatile Thread evaluationThread;
	protected CloseableIteration<T, QueryEvaluationException> leftIter;
	protected CloseableIteration<T, QueryEvaluationException> rightIter;
	protected volatile boolean closed;
	protected boolean finished = false;
	
	protected QueueCursor<CloseableIteration<T, QueryEvaluationException>> rightQueue = new QueueCursor<CloseableIteration<T, QueryEvaluationException>>(1024);

	
	public JoinExecutorBase(CloseableIteration<T, QueryEvaluationException> leftIter, TupleExpr rightArg,
			BindingSet bindings) throws QueryEvaluationException	{
		this.leftIter = leftIter;
		this.rightArg = rightArg;
		this.bindings = bindings;
	}
	

	public final void run() {
		
		try {
			handleBindings();
		} catch (Exception e) {
			toss(e);
		} finally {
			finished=true;
			rightQueue.done();
		}

	}
	
	/**
	 * Implementations must implement this method to handle bindings.
	 * 
	 * Use the following as a template
	 * <code>
	 * while (!closed && leftIter.hasNext()) {
	 * 		// your code
	 * }
	 * </code>
	 * 
	 * and add results to rightQueue. Note that addResult() is implemented synchronized
	 * and thus thread safe. In case you can guarantee sequential access, it is also
	 * possible to directly access rightQueue
	 * 
	 */
	protected abstract void handleBindings() throws Exception;
	
	
	public void addResult(CloseableIteration<T, QueryEvaluationException> res)  {
		/* optimization: avoid adding empty results */
		if (res instanceof EmptyIteration<?,?>)
			return;

		try {
			rightQueue.put(res);
		} catch (InterruptedException e) {
			throw new RuntimeException("Error adding element to right queue", e);
		}
	}
		
	public void done() {
		;	// no-op
	}
	

	public void toss(Exception e) {
		rightQueue.toss(e);
	}
	
	
	@Override
	public T getNextElement() throws QueryEvaluationException	{
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
	public void handleClose() throws QueryEvaluationException {
		closed = true;
		if (evaluationThread != null) {
			evaluationThread.interrupt();
		}
		
		if (rightIter != null) {
			rightIter.close();
			rightIter = null;
		}

		if (leftIter!=null)
			leftIter.close();
	}
	
	/**
	 * Return true if this executor is finished or aborted
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}
	

}
