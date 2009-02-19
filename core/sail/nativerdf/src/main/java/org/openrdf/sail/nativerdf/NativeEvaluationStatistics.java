/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;

/**
 * @author Arjohn Kampman
 * @author Enrico Minack
 */
class NativeEvaluationStatistics extends EvaluationStatistics {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final NativeStore nativeStore;

	public NativeEvaluationStatistics(NativeStore nativeStore) {
		this.nativeStore = nativeStore;
	}

	@Override
	protected CardinalityCalculator createCardinalityCalculator() {
		return new NativeCardinalityCalculator();
	}

	protected class NativeCardinalityCalculator extends CardinalityCalculator {

		@Override
		protected double getCardinality(StatementPattern sp) {
			Resource subj = (Resource)getConstantValue(sp.getSubjectVar());
			URI pred = (URI)getConstantValue(sp.getPredicateVar());
			Value obj = getConstantValue(sp.getObjectVar());
			Resource context = (Resource)getConstantValue(sp.getContextVar());

			try {
				return nativeStore.cardinality(subj, pred, obj, context);
			}
			catch (IOException e) {
				log.error(
						"Failed to estimate statement pattern cardinality, falling back to generic implementation",
						e);
				return super.getCardinality(sp);
			}
		}

		protected Value getConstantValue(Var var) {
			return (var != null) ? var.getValue() : null;
		}
	}
}
