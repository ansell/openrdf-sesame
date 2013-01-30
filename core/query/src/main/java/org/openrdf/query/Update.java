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
package org.openrdf.query;

/**
 * An update operation on a {@link org.openrdf.repository.Repository} that can
 * be formulated in one of the supported query languages (for example SPARQL).
 * 
 * @author Jeen
 */
public interface Update extends Operation {

	/**
	 * Execute this update on the repository.
	 * 
	 * @throws UpdateExecutionException
	 *         if the update could not be successfully completed.
	 */
	void execute()
		throws UpdateExecutionException;

}
