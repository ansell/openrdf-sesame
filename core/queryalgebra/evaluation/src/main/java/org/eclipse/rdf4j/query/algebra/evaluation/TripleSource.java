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
package org.eclipse.rdf4j.query.algebra.evaluation;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;

/**
 * A triple source that can be queried for (the existence of) certain triples in
 * certain contexts. This interface defines the methods that are needed by the
 * Sail Query Model to be able to evaluate itself.
 */
public interface TripleSource {

	/**
	 * Gets all statements that have a specific subject, predicate and/or object.
	 * All three parameters may be null to indicate wildcards. Optionally a (set
	 * of) context(s) may be specified in which case the result will be
	 * restricted to statements matching one or more of the specified contexts.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param contexts
	 *        The context(s) to get the statements from. Note that this parameter
	 *        is a vararg and as such is optional. If no contexts are supplied
	 *        the method operates on the entire repository.
	 * @return An iterator over the relevant statements.
	 * @throws QueryEvaluationException
	 *         If the triple source failed to get the statements.
	 */
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			IRI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException;

	/**
	 * Gets a ValueFactory object that can be used to create URI-, blank node-
	 * and literal objects.
	 * 
	 * @return a ValueFactory object for this TripleSource.
	 */
	public ValueFactory getValueFactory();
}
