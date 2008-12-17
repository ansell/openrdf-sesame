/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.cursor.SingletonCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.cursors.FilterCursor;
import org.openrdf.sail.helpers.SailUtil;
import org.openrdf.store.StoreException;

/**
 * Transform the condition into a filter and the right side into an
 * {@link AlternativeCursor}, then evaluate as a {@link ParallelJoinCursor}.
 * 
 * @author James Leigh
 */
public class ParallelLeftJoinCursor implements Cursor<BindingSet>, Runnable {

	/*-----------*
	 * Constants *
	 *-----------*/

	private EvaluationStrategy strategy;

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

	private Cursor<BindingSet> leftIter;

	private Cursor<BindingSet> rightIter;

	private volatile boolean closed;

	private volatile Exception exception;

	private BlockingQueue<Cursor<BindingSet>> rightQueue = new ArrayBlockingQueue<Cursor<BindingSet>>(1024);

	private Cursor<BindingSet> end = EmptyCursor.getInstance();

	StoreException source;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ParallelLeftJoinCursor(EvaluationStrategy strategy, LeftJoin join, BindingSet bindings)
		throws StoreException
	{
		this.strategy = strategy;
		this.join = join;
		this.scopeBindingNames = join.getBindingNames();

		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
		this.source = SailUtil.isDebugEnabled() ? new StoreException() : null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void run() {
		try {
			BindingSet leftBindings;
			ValueExpr condition = join.getCondition();
			while (!closed && (leftBindings = leftIter.next()) != null) {
				Cursor<BindingSet> result = strategy.evaluate(join.getRightArg(), leftBindings);
				if (condition != null) {
					result = new FilterCursor(result, condition, scopeBindingNames, strategy);
				}
				Cursor<BindingSet> alt = new SingletonCursor<BindingSet>(leftBindings);
				rightQueue.put(new AlternativeCursor<BindingSet>(result, alt));
			}
		}
		catch (RuntimeException e) {
			handle(e);
		}
		catch (StoreException e) {
			handle(e);
		}
		catch (InterruptedException e) {
			handle(e);
		}
		finally {
			try {
				rightQueue.put(end);
			}
			catch (InterruptedException e) {
				// The other thread will also need to be interrupted
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
			throwException(exception);
		}
		catch (InterruptedException e) {
			throw new StoreException(e);
		}

		return null;
	}

	private void handle(Exception e) {
		if (source != null) {
			source.initCause(e);
			exception = source;
		}
		else {
			exception = e;
		}
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
		String right = join.getRightArg().toString();
		if (rightIter != null) {
			right = rightIter.toString();
		}
		ValueExpr condition = join.getCondition();
		String filter = "";
		if (condition != null) {
			filter = condition.toString().trim().replace("\n", "\n\t");
		}
		return "ParallelLeftJoin " + filter + "\n\t" + left + "\n\t" + right.replace("\n", "\n\t");
	}
}
