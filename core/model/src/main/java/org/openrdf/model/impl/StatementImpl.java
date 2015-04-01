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
package org.openrdf.model.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;

/**
 * An implementation of the {@link Statement} interface for statements that
 * don't have an associated context. For statements that do have an associated
 * context, {@link ContextStatementImpl} can be used.
 */
public class StatementImpl implements Statement {

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
	 * Creates a new Statement with the supplied subject, predicate and object.
	 * 
	 * @param subject
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param predicate
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param object
	 *        The statement's object, must not be <tt>null</tt>.
	 */
	public StatementImpl(Resource subject, IRI predicate, Value object) {
		assert (subject != null);
		assert (predicate != null);
		assert (object != null);

		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
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
			Statement otherSt = (Statement)other;

			// The object is potentially the cheapest to check, as types
			// of these references might be different.

			// In general the number of different predicates in sets of
			// statements is the smallest, so predicate equality is checked
			// last.
			return object.equals(otherSt.getObject()) && subject.equals(otherSt.getSubject())
					&& predicate.equals(otherSt.getPredicate());
		}

		return false;
	}

	// Overrides Object.hashCode(), implements Statement.hashCode()
	@Override
	public int hashCode() {
		return 961 * subject.hashCode() + 31 * predicate.hashCode() + object.hashCode();
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
