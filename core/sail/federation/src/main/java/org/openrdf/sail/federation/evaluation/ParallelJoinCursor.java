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

	private final QueueCursor<Cursor<BindingSet>> cursorQueue = new QueueCursor<Cursor<BindingSet>>(1024);

	/*-----------*
	 * Variables *
	 *-----------*/

	private volatile Thread evaluationThread;

	private Cursor<BindingSet> currentCursor;

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
			BindingSet bindings;
			while (!closed && (bindings = leftIter.next()) != null) {
				Cursor<BindingSet> nextCursor = strategy.evaluate(rightArg, bindings);
				cursorQueue.put(nextCursor);
			}
		}
		catch (RuntimeException e) {
			cursorQueue.toss(e);
		}
		catch (StoreException e) {
			cursorQueue.toss(e);
		}
		catch (InterruptedException e) {
			// stop
		}
		finally {
			evaluationThread = null;
			cursorQueue.done();
		}
	}

	public BindingSet next()
		throws StoreException
	{
		while (currentCursor != null || (currentCursor = cursorQueue.next()) != null) {
			BindingSet result = currentCursor.next();
			if (result != null) {
				return result;
			}
			else {
				currentCursor.close();
				currentCursor = null;
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
		
		if (currentCursor != null) {
			currentCursor.close();
			currentCursor = null;
		}

		leftIter.close();
	}

	@Override
	public String toString() {
		String left = leftIter.toString().replace("\n", "\n\t");
		String right = rightArg.toString();
		if (currentCursor != null) {
			right = currentCursor.toString();
		}
		return "ParallelJoin\n\t" + left + "\n\t" + right.replace("\n", "\n\t");
	}
}
