/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.QueueCursor;
import org.openrdf.cursor.SingletonCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.cursors.FilterCursor;
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

	private volatile Thread evaluationThread;

	private Cursor<BindingSet> leftIter;

	private Cursor<BindingSet> rightIter;

	private volatile boolean closed;

	private QueueCursor<Cursor<BindingSet>> rightQueue = new QueueCursor<Cursor<BindingSet>>(1024);

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
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void run() {
		evaluationThread = Thread.currentThread();
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
		if (evaluationThread != null) {
			evaluationThread.interrupt();
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
