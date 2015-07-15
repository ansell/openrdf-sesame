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
package org.openrdf.repository.event;

import org.openrdf.repository.Repository;

/**
 *
 * @author Herko ter Horst
 */
public interface NotifyingRepository extends Repository {

	/**
	 * Registers a <tt>RepositoryListener</tt> that will receive notifications
	 * of operations that are performed on this repository.
	 */
	public void addRepositoryListener(RepositoryListener listener);

	/**
	 * Removes a registered <tt>RepositoryListener</tt> from this repository.
	 */
	public void removeRepositoryListener(RepositoryListener listener);

	/**
	 * Registers a <tt>RepositoryConnectionListener</tt> that will receive
	 * notifications of operations that are performed on any< connections that
	 * are created by this repository.
	 */
	public void addRepositoryConnectionListener(RepositoryConnectionListener listener);

	/**
	 * Removes a registered <tt>RepositoryConnectionListener</tt> from this
	 * repository.
	 */
	public void removeRepositoryConnectionListener(RepositoryConnectionListener listener);

}
