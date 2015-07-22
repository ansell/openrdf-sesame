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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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

	/**
	 * Opens a connection to this repository that can be used for querying and
	 * updating the contents of the repository. Created connections need to be
	 * closed to make sure that any resources they keep hold of are released. The
	 * best way to do this is to use a try-finally-block as follows:
	 * 
	 * <pre>
	 * Connection con = repository.getConnection();
	 * try {
	 * 	// perform operations on the connection
	 * }
	 * finally {
	 * 	con.close();
	 * }
	 * </pre>
	 * 
	 * Note that {@link RepositoryConnection} is not guaranteed to be
	 * thread-safe! The recommended pattern for repository access in a
	 * multithreaded application is to share the Repository object between
	 * threads, but have each thread create and use its own
	 * {@link RepositoryConnection}s.
	 * 
	 * @return A connection that allows operations on this repository.
	 * @throws RepositoryException
	 *         If something went wrong during the creation of the Connection.
	 */
	public NotifyingRepositoryConnection getConnection()
		throws RepositoryException;

}
