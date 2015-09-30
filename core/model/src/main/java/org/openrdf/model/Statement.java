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
package org.openrdf.model;

import java.io.Serializable;

/**
 * An RDF statement, with optional associated context. A statement can have an
 * associated context in specific cases, for example when fetched from a
 * repository. The context field does not influence statement equality; a
 * statement is equal to another statement if the subjects, predicates and
 * objects are equal.
 */
public interface Statement extends Serializable {

	/**
	 * Gets the subject of this statement.
	 * 
	 * @return The statement's subject.
	 */
	public Resource getSubject();

	/**
	 * Gets the predicate of this statement.
	 * 
	 * @return The statement's predicate.
	 */
	public IRI getPredicate();

	/**
	 * Gets the object of this statement.
	 * 
	 * @return The statement's object.
	 */
	public Value getObject();

	/**
	 * Gets the context of this statement.
	 * 
	 * @return The statement's context, or <tt>null</tt> in case of the null
	 *         context or if not applicable.
	 */
	public Resource getContext();

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates, objects and
	 *         contexts are equal.
	 */
	public boolean equals(Object other);

	/**
	 * The hash code of a statement. 
	 * 
	 * @return A hash code for the statement.
	 */
	public int hashCode();
}
