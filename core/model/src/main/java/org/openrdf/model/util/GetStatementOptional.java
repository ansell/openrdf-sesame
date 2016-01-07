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
package org.openrdf.model.util;

import java.util.Optional;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * Either supplies a statement matching the given pattern, or
 * {@link Optional#empty()} otherwise.
 * 
 * @author Peter Ansell
 */
@FunctionalInterface
public interface GetStatementOptional {

	/**
	 * Either supplies a statement matching the given pattern, or
	 * {@link Optional#empty()} otherwise.
	 * 
	 * @param subject
	 *            A {@link Resource} to be used to match to statements.
	 * @param predicate
	 *            An {@link IRI} to be used to match to statements.
	 * @param object
	 *            A {@link Value} to be used to match to statements.
	 * @param contexts
	 *            An array of context {@link Resource} objects, or left out (not
	 *            null) to select from all contexts.
	 * @return An {@link Optional} either containing a single statement matching
	 *         the pattern or {@link Optional#empty()} otherwise.
	 */
	Optional<Statement> get(Resource subject, IRI predicate, Value object, Resource... contexts);

}
