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
package org.openrdf;

import org.openrdf.model.URI;

/**
 * A Transaction Isolation Level. Defaul levels supported by Sesame are provided
 * by {@link IsolationLevels}, third-party triplestore implementors may choose
 * to add additional IsolationLevel implementations if their triplestore's
 * isolation contract is different from what is provided by default.
 * 
 * @author Jeen Broekstra
 * @since 2.8
 */
public interface IsolationLevel {

	/**
	 * Verifies if this transaction isolation level is compatible with the
	 * supplied other isolation level - that is, if this transaction isolation
	 * level offers at least the same guarantees as the other level. By
	 * definition, every transaction isolation level is compatible with itself.
	 * 
	 * @param otherLevel
	 *        an other isolation level to check compatibility against.
	 * @return true iff this isolation level is compatible with the supplied
	 *         other isolation level, false otherwise.
	 */
	boolean isCompatibleWith(IsolationLevel otherLevel);
	
	/**
	 * Get a URI uniquely representing this isolation level.
	 * 
	 * @return a URI that uniquely represents this isolation level. 
	 */
	URI getURI();
	
}
