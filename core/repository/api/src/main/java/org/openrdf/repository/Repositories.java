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
package org.openrdf.repository;

import java.util.function.Consumer;

/**
 * Utility for dealing with {@link Repository} and {@link RepositoryConnection}
 * objects.
 * 
 * @author Peter Ansell
 */
public final class Repositories {

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Consumer}, before either rolling back the
	 * transaction if it failed, or committing the transaction if it was
	 * successful.
	 * 
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param processFunction
	 *        A {@link Consumer} that performs an action on the connection.
	 * @throws RepositoryException
	 *         If there was an exception dealing with the Repository.
	 * @since 4.0
	 */
	public static void commitOrRollback(Repository repository, Consumer<RepositoryConnection> processFunction)
	{
		RepositoryConnection conn = repository.getConnection();

		try {
			conn.begin();
			processFunction.accept(conn);
			conn.commit();
		}
		catch (RepositoryException e) {
			if (conn != null) {
				conn.rollback();
			}
			throw e;
		}
		finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Consumer}, before either rolling back the
	 * transaction if it failed, or committing the transaction if it was
	 * successful.
	 * 
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param processFunction
	 *        A {@link Consumer} that performs an action on the connection.
	 * @param exceptionHandler
	 *        A {@link Consumer} that handles an exception if one was generated.
	 * @throws RepositoryException
	 *         If there was an exception dealing with the Repository.
	 * @since 4.0
	 */
	public static void commitOrRollback(Repository repository, Consumer<RepositoryConnection> processFunction,
			Consumer<RepositoryException> exceptionHandler)
	{
		try {
			commitOrRollback(repository, processFunction);
		}
		catch (RepositoryException e) {
			exceptionHandler.accept(e);
		}
	}

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Consumer}, before either rolling back the
	 * transaction if it failed, or committing the transaction if it was
	 * successful.
	 * 
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param processFunction
	 *        A {@link Consumer} that performs an action on the connection.
	 * @since 4.0
	 */
	public static void commitOrRollbackSilent(Repository repository,
			Consumer<RepositoryConnection> processFunction)
	{
		commitOrRollback(repository, processFunction, e -> {
		});
	}

	/**
	 * Private constructor to prevent instantiation, this is a static helper
	 * class.
	 */
	private Repositories() {
	}

}
