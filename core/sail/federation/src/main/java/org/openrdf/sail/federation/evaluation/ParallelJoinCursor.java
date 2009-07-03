/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.QueueCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.store.StoreException;

/**
 * Iterate the left side and evaluate the right side in separate thread, only
 * iterate the right side in the controlling thread.
 * 
 * @author James Leigh
 */
public class ParallelJoinCursor implements Cursor<BindingSet>, Runnable {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final Cursor<BindingSet> leftIter;

	private final TupleExpr rightArg;

	private final QueueCursor<Cursor<BindingSet>> rightQueue = new QueueCursor<Cursor<BindingSet>>(1024);

	/*-----------*
	 * Variables *
	 *-----------*/

	private volatile Thread evaluationThread;

	private Cursor<BindingSet> rightIter;

	private volatile boolean closed;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ParallelJoinCursor(EvaluationStrategy strategy, Cursor<BindingSet> leftIter, TupleExpr rightArg,
			BindingSet bindings)
		throws EvaluationException
	{
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
			BindingSet leftNext;
			while (!closed && (leftNext = leftIter.next()) != null) {
				rightQueue.put(strategy.evaluate(rightArg, leftNext));
			}
		}
		catch (RuntimeException e) {
			rightQueue.toss(e);
		}
		catch (StoreException e) {
			rightQueue.toss(e);
		}
		catch (InterruptedException e) {
			// stop
		}
		finally {
			evaluationThread = null;
			rightQueue.done();
		}
	}

	public BindingSet next()
		throws StoreException
	{
		while (rightIter != null || (rightIter = rightQueue.next()) != null) {
			BindingSet rightNext = rightIter.next();
			if (rightNext != null) {
				return rightNext;
			}
			else {
				rightIter.close();
				rightIter = null;
			}
		}

		return null;
	}

	public void close()
		throws StoreException
	{
		closed = true;

		Thread t = evaluationThread;
		if (t != null) {
			t.interrupt();
		}
		if (rightIter != null) {
			rightIter.close();
			rightIter = null;
		}

		leftIter.close();
	}

	@Override
	public String toString() {
		String left = leftIter.toString().replace("\n", "\n\t");
		String right = rightArg.toString();
		if (rightIter != null) {
			right = rightIter.toString();
		}
		return "ParallelJoin\n\t" + left + "\n\t" + right.replace("\n", "\n\t");
	}
}
