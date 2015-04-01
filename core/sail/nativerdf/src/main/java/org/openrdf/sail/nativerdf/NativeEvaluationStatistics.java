/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
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
				if (!(pred instanceof IRI)) {
					//  can happen when a previous optimizer has inlined a comparison operator. See SES-970 
					pred = null;
				}
				Value obj = getConstantValue(sp.getObjectVar());
				Value context = getConstantValue(sp.getContextVar());
				if (!(context instanceof Resource)) {
					//  can happen when a previous optimizer has inlined a comparison operator. See SES-970 
					context = null;
				}
				return nativeStore.cardinality((Resource)subj, (IRI)pred, obj, (Resource)context);
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
