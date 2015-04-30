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
package org.openrdf.http.client;

/**
 * Common interface for objects, such as Repository and RepositoryConnection,
 * that are dependent on {@link SesameClient}.
 * 
 * @author James Leigh
 */
public interface SesameClientDependent {

	/**
	 * {@link SesameClient} that has been assigned or has been used by this
	 * object. The life cycle might not be or might be tied to this object,
	 * depending on whether {@link SesameClient} was passed to or created by this
	 * object respectively.
	 * 
	 * @return a {@link SesameClient} instance or null
	 */
	SesameClient getSesameClient();

	/**
	 * Assign an {@link SesameClient} that this object should use. The life cycle
	 * of the given {@link SesameClient} is independent of this object. Closing
	 * or shutting down this object does not have any impact on the given client.
	 * Callers must ensure that the given client is properly closed elsewhere.
	 * 
	 * @param client
	 */
	void setSesameClient(SesameClient client);
}
