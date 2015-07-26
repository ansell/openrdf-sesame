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
