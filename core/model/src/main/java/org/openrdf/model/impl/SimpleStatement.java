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
package org.openrdf.model.impl;

import java.util.Objects;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;

/**
 * A simple default implementation of the {@link Statement} interface for
 * statements that don't have an associated context. For statements that do have
 * an associated context, {@link ContextStatement} can be used.
 * 
 * @see {@link SimpleValueFactory}
 */
public class SimpleStatement implements Statement {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 8707542157460228077L;

	/**
	 * The statement's subject.
	 */
	private final Resource subject;

	/**
	 * The statement's predicate.
	 */
	private final IRI predicate;

	/**
	 * The statement's object.
	 */
	private final Value object;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Statement with the supplied subject, predicate and object. *
	 * <p>
	 * Note that creating SimpleStatement objects directly via this constructor
	 * is not the recommended approach. Instead, use a
	 * {@link org.openrdf.model.ValueFactory ValueFactory} (obtained from your
	 * repository or by using {@link SimpleValueFactory#getInstance()}) to create
	 * new Statement objects.
	 * 
	 * @param subject
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param predicate
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param object
	 *        The statement's object, must not be <tt>null</tt>.
	 * @see {@link SimpleValueFactory#createStatement(Resource, IRI, Value)
	 */
	protected SimpleStatement(Resource subject, IRI predicate, Value object) {
		this.subject = Objects.requireNonNull(subject, "subject must not be null");
		this.predicate = Objects.requireNonNull(predicate, "predicate must not be null");
		this.object = Objects.requireNonNull(object, "object must not be null");
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements Statement.getSubject()
	public Resource getSubject() {
		return subject;
	}

	// Implements Statement.getPredicate()
	public IRI getPredicate() {
		return predicate;
	}

	// Implements Statement.getObject()
	public Value getObject() {
		return object;
	}

	// Implements Statement.getContext()
	public Resource getContext() {
		return null;
	}

	// Overrides Object.equals(Object), implements Statement.equals(Object)
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof Statement) {
			Statement that = (Statement)other;

			/* We check  object equality first since it's most likely to be different.
			 *
			 * In general the number of different predicates and contexts in sets of
			 * statements are the smallest (and therefore most likely to be identical), so 
			 * these are checked last.
			 */
			return object.equals(that.getObject()) && subject.equals(that.getSubject())
					&& predicate.equals(that.getPredicate()) && Objects.equals(getContext(), that.getContext());
		}

		return false;
	}

	// Overrides Object.hashCode(), implements Statement.hashCode()
	@Override
	public int hashCode() {
		return Objects.hash(subject, predicate, object, getContext());
	}

	/**
	 * Gives a String-representation of this Statement that can be used for
	 * debugging.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);

		sb.append("(");
		sb.append(getSubject());
		sb.append(", ");
		sb.append(getPredicate());
		sb.append(", ");
		sb.append(getObject());
		sb.append(")");

		return sb.toString();
	}
}
