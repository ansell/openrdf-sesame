/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Inserts original bindings into the result, uses ?__rowIdx to resolve original
 * bindings. See {@link ServiceJoinIterator} and {@link SPARQLFederatedService}.
 * 
 * @author Andreas Schwarte
 */
public class ServiceJoinConversionIteration extends
		ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	protected final List<BindingSet> bindings;

	public ServiceJoinConversionIteration(
			CloseableIteration<BindingSet, QueryEvaluationException> iter,
			List<BindingSet> bindings) {
		super(iter);
		this.bindings = bindings;
	}

	@Override
	protected BindingSet convert(BindingSet bIn)
			throws QueryEvaluationException {

		// overestimate the capacity
		QueryBindingSet res = new QueryBindingSet(bIn.size() + bindings.size());

		int bIndex = -1;
		Iterator<Binding> bIter = bIn.iterator();
		while (bIter.hasNext()) {
			Binding b = bIter.next();
			String name = b.getName();
			if (name.equals("__rowIdx")) {
				bIndex = Integer.parseInt(b.getValue().stringValue());
				continue;
			}
			res.addBinding(b.getName(), b.getValue());
		}

		res.addAll(bindings.get(bIndex));
		return res;
	}
}
