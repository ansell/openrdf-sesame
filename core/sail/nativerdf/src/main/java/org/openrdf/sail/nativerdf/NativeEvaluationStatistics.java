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
			try {
				Value subj = getConstantValue(sp.getSubjectVar());
				if (!(subj instanceof Resource)) {
					// can happen when a previous optimizer has inlined a comparison operator. 
					// this can cause, for example, the subject variable to be equated to a literal value. 
					// See SES-970 
					subj = null;
				}
				Value pred = getConstantValue(sp.getPredicateVar());
				if (!(pred instanceof URI)) {
					//  can happen when a previous optimizer has inlined a comparison operator. See SES-970 
					pred = null;
				}
				Value obj = getConstantValue(sp.getObjectVar());
				Value context = getConstantValue(sp.getContextVar());
				if (!(context instanceof Resource)) {
					//  can happen when a previous optimizer has inlined a comparison operator. See SES-970 
					context = null;
				}
				return nativeStore.cardinality((Resource)subj, (URI)pred, obj, (Resource)context);
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
