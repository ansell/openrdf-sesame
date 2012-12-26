/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.QueryEvaluationException;

/**
 * If the primary cursor is empty, use the alternative cursor.
 * 
 * @author James Leigh
 */
public class AlternativeCursor<E> extends LookAheadIteration<E, QueryEvaluationException> {

	private CloseableIteration<? extends E, QueryEvaluationException> delegate;

	private CloseableIteration<? extends E, QueryEvaluationException> primary;

	private CloseableIteration<? extends E, QueryEvaluationException> alternative;

	public AlternativeCursor(CloseableIteration<? extends E, QueryEvaluationException> primary, CloseableIteration<? extends E, QueryEvaluationException> alternative) {
		this.alternative = alternative;
		this.primary = primary;
	}

	public void handleClose()
		throws QueryEvaluationException
	{
		primary.close();
		alternative.close();
	}

	public E getNextElement()
		throws QueryEvaluationException
	{
		if (delegate == null) {
			if (!primary.hasNext()) {
				delegate = alternative;
			}
			else {
				delegate = primary;
				return primary.next();
			}
		}
		if (!delegate.hasNext())
			return null;
		return delegate.next();
	}

	@Override
	public String toString() {
		String name = getClass().getName().replaceAll("^.*\\.|Cursor$", "");
		return name + "\n\t" + primary.toString() + "\n\t" + alternative.toString();
	}
}
