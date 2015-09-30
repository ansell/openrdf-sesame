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
package org.openrdf.sail.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemIRI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;

/**
 * Uses the MemoryStore's statement sizes to give cost estimates based on the
 * size of the expected results. This process could be improved with
 * repository statistics about size and distribution of statements.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
class MemEvaluationStatistics extends EvaluationStatistics {

	private final MemValueFactory valueFactory;

	MemEvaluationStatistics(MemValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	protected CardinalityCalculator createCardinalityCalculator() {
		return new MemCardinalityCalculator();
	}

	protected class MemCardinalityCalculator extends CardinalityCalculator {

		@Override
		public double getCardinality(StatementPattern sp) {

			Value subj = getConstantValue(sp.getSubjectVar());
			if (!(subj instanceof Resource)) {
				// can happen when a previous optimizer has inlined a comparison
				// operator.
				// this can cause, for example, the subject variable to be
				// equated to a literal value.
				// See SES-970 / SES-998
				subj = null;
			}
			Value pred = getConstantValue(sp.getPredicateVar());
			if (!(pred instanceof IRI)) {
				// can happen when a previous optimizer has inlined a comparison
				// operator. See SES-970 / SES-998
				pred = null;
			}
			Value obj = getConstantValue(sp.getObjectVar());
			Value context = getConstantValue(sp.getContextVar());
			if (!(context instanceof Resource)) {
				// can happen when a previous optimizer has inlined a comparison
				// operator. See SES-970 / SES-998
				context = null;
			}

			// Perform look-ups for value-equivalents of the specified values
			MemResource memSubj = valueFactory.getMemResource((Resource)subj);
			MemIRI memPred = valueFactory.getMemURI((IRI)pred);
			MemValue memObj = valueFactory.getMemValue(obj);
			MemResource memContext = valueFactory.getMemResource((Resource)context);

			if (subj != null && memSubj == null || pred != null && memPred == null || obj != null
					&& memObj == null || context != null && memContext == null)
			{
				// non-existent subject, predicate, object or context
				return 0.0;
			}

			// Search for the smallest list that can be used by the iterator
			List<Integer> listSizes = new ArrayList<Integer>(4);
			if (memSubj != null) {
				listSizes.add(memSubj.getSubjectStatementCount());
			}
			if (memPred != null) {
				listSizes.add(memPred.getPredicateStatementCount());
			}
			if (memObj != null) {
				listSizes.add(memObj.getObjectStatementCount());
			}
			if (memContext != null) {
				listSizes.add(memContext.getContextStatementCount());
			}

			double cardinality;

			if (listSizes.isEmpty()) {
				// all wildcards
				cardinality = Integer.MAX_VALUE;
			}
			else {
				cardinality = (double)Collections.min(listSizes);

				// List<Var> vars = getVariables(sp);
				// int constantVarCount = countConstantVars(vars);
				//
				// // Subtract 1 from var count as this was used for the list
				// size
				// double unboundVarFactor = (double)(vars.size() -
				// constantVarCount) / (vars.size() - 1);
				//
				// cardinality = Math.pow(cardinality, unboundVarFactor);
			}

			return cardinality;
		}

		protected Value getConstantValue(Var var) {
			if (var != null) {
				return var.getValue();
			}

			return null;
		}
	}
} // end inner class MemCardinalityCalculator
