/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.results.Cursor;
import org.openrdf.results.impl.EmptyCursor;
import org.openrdf.sail.helpers.SailUtil;
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

	private final TupleExpr rightArg;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Cursor<BindingSet> leftIter;

	private Cursor<BindingSet> rightIter;

	private volatile boolean closed;

	private volatile Exception exception;

	private BlockingQueue<Cursor<BindingSet>> rightQueue = new ArrayBlockingQueue<Cursor<BindingSet>>(1024);

	private Cursor<BindingSet> end = EmptyCursor.emptyCursor();

	StoreException source;

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
		this.source = SailUtil.isDebugEnabled() ? new StoreException() : null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void run() {
		try {
			BindingSet leftNext;
			while (!closed && (leftNext = leftIter.next()) != null) {
				rightQueue.put(strategy.evaluate(rightArg, leftNext));
			}
			rightQueue.put(end);
		}
		catch (RuntimeException e) {
			if (source != null) {
				source.initCause(e);
				exception = source;
			} else {
				exception = e;
			}
		}
		catch (StoreException e) {
			if (source != null) {
				source.initCause(e);
				exception = source;
			} else {
				exception = e;
			}
		}
		catch (InterruptedException e) {
			if (source != null) {
				source.initCause(e);
				exception = source;
			} else {
				exception = e;
			}
		}
	}

	public BindingSet next()
		throws StoreException
	{
		try {
			while (rightIter != null || (rightIter = rightQueue.take()) != end) {
				throwException(exception);
				BindingSet rightNext = rightIter.next();
				if (rightNext != null) {
					return rightNext;
				}
				else {
					rightIter.close();
					rightIter = null;
				}
			}
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}

		return null;
	}

	private void throwException(Exception exception)
		throws StoreException, InterruptedException
	{
		if (exception instanceof RuntimeException)
			throw (RuntimeException)exception;
		if (exception instanceof StoreException)
			throw (StoreException)exception;
		if (exception instanceof InterruptedException)
			throw (InterruptedException)exception;
	}

	public void close()
		throws StoreException
	{
		closed = true;
		rightQueue.clear();
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
