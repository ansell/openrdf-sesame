/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.nativerdf;

import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.nativerdf.model.NativeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arjohn Kampman
 * @author Enrico Minack
 */
class NativeEvaluationStatistics extends EvaluationStatistics {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ValueStore valueStore;

	private final TripleStore tripleStore;

	public NativeEvaluationStatistics(ValueStore valueStore, TripleStore tripleStore) {
		this.valueStore = valueStore;
		this.tripleStore = tripleStore;
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
				return cardinality((Resource)subj, (IRI)pred, obj, (Resource)context);
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

	private double cardinality(Resource subj, IRI pred, Value obj, Resource context)
		throws IOException
	{
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);
			if (objID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int contextID = NativeValue.UNKNOWN_ID;
		if (context != null) {
			contextID = valueStore.getID(context);
			if (contextID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		return tripleStore.cardinality(subjID, predID, objID, contextID);
	}
}
