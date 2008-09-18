/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.StoreException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.base.ConvertingCursor;

public class ExtensionCursor extends ConvertingCursor<BindingSet, BindingSet> {

	private final Extension extension;

	private final EvaluationStrategy strategy;

	public ExtensionCursor(Extension extension,
			Cursor<BindingSet> iter, EvaluationStrategy strategy)
		throws EvaluationException
	{
		super(iter);
		this.extension = extension;
		this.strategy = strategy;
	}

	@Override
	public BindingSet convert(BindingSet sourceBindings)
		throws StoreException
	{
		QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);

		for (ExtensionElem extElem : extension.getElements()) {
			Value targetValue = strategy.evaluate(extElem.getExpr(), sourceBindings);

			if (targetValue != null) {
				// Potentially overwrites bindings from super
				targetBindings.setBinding(extElem.getName(), targetValue);
			}
		}

		return targetBindings;
	}
}
