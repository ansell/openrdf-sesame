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
package org.eclipse.rdf4j.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * An RDF graph, represented as a collection of {@link Statement}s.
 * 
 * @see GraphUtil
 * @author Arjohn Kampman
 * @deprecated Use {@link Model} instead.
 */
@Deprecated
public interface Graph extends Collection<Statement>, Serializable {

	/**
	 * Gets the value factory for this graph.
	 * 
	 * @deprecated {@link org.eclipse.rdf4j.model.impl.SimpleValueFactory#getInstance}
	 *             will obtain a default value factory implementation. If you are
	 *             working with the Repository API, then <tt>Repository</tt>
	 *             instances also supply a <tt>getValueFactory()</tt> method.
	 */
	@Deprecated
	public ValueFactory getValueFactory();

	/**
	 * Adds one or more statements to the graph. This method creates a statement
	 * for each specified context and adds those to the graph. If no contexts are
	 * specified, a single statement with no associated context is added.
	 * 
	 * @param subj
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param pred
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param obj
	 *        The statement's object, must not be <tt>null</tt>.
	 * @param contexts
	 *        The contexts to add statements to.
	 */
	public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts);

	/**
	 * Gets the statements with the specified subject, predicate, object and
	 * (optionally) context. The <tt>subject</tt>, <tt>predicate</tt> and
	 * <tt>object</tt> parameters can be <tt>null</tt> to indicate wildcards. The
	 * <tt>contexts</tt> parameter is a wildcard and accepts zero or more values.
	 * If no contexts are specified, statements will match disregarding their
	 * context. If one or more contexts are specified, statements with a context
	 * matching one of these will match. Note: to match statements without an
	 * associated context, specify the value <tt>null</tt> and explicitly cast it
	 * to type <tt>Resource</tt>.
	 * <p>
	 * Examples: <tt>graph.match(s1, null, null)</tt> matches all statements that
	 * have subject <tt>s1</tt>,<br>
	 * <tt>graph.match(null, null, null, c1)</tt> matches all statements that
	 * have context <tt>c1</tt>,<br>
	 * <tt>graph.match(null, null, null, (Resource)null)</tt> matches all
	 * statements that have no associated context,<br>
	 * <tt>graph.match(null, null, null, c1, c2, c3)</tt> matches all statements
	 * that have context <tt>c1</tt>, <tt>c2</tt> or <tt>c3</tt>.
	 * 
	 * @deprecated The preferred alternative is
	 *             {@link org.eclipse.rdf4j.model.Model#filter}.iterator().
	 * @param subj
	 *        The subject of the statements to match, <tt>null</tt> to match
	 *        statements with any subject.
	 * @param pred
	 *        The predicate of the statements to match, <tt>null</tt> to match
	 *        statements with any predicate.
	 * @param obj
	 *        The object of the statements to match, <tt>null</tt> to match
	 *        statements with any object.
	 * @param contexts
	 *        The contexts of the statements to match. If no contexts are
	 *        specified, statements will match disregarding their context. If one
	 *        or more contexts are specified, statements with a context matching
	 *        one of these will match.
	 * @return The statements that match the specified pattern.
	 * @throws IllegalArgumentException
	 *         If a <tt>null</tt>-array is specified as the value for
	 *         <tt>contexts</tt>. See
	 *         {@link OpenRDFUtil#verifyContextNotNull(Resource[])} for more
	 *         info.
	 */
	@Deprecated
	public Iterator<Statement> match(Resource subj, IRI pred, Value obj, Resource... contexts);
}
