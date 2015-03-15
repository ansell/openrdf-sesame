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
import java.util.function.Function;

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
	 * @throws UnknownTransactionStateException
	 *         If the transaction state was not properly recognised. (Optional
	 *         specific exception)
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
	 * @throws UnknownTransactionStateException
	 *         If the transaction state was not properly recognised. (Optional
	 *         specific exception)
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
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Consumer}, before either rolling back the
	 * transaction if it failed, or committing the transaction if it was
	 * successful.
	 * 
	 * @param <T>
	 *        The type of the return value.
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param processFunction
	 *        A {@link Function} that performs an action on the connection and
	 *        returns a result.
	 * @return The result of applying the function.
	 * @throws RepositoryException
	 *         If there was an exception dealing with the Repository.
	 * @throws UnknownTransactionStateException
	 *         If the transaction state was not properly recognised. (Optional
	 *         specific exception)
	 * @since 4.0
	 */
	public static <T> T commitOrRollback(Repository repository,
			Function<RepositoryConnection, T> processFunction)
	{
		RepositoryConnection conn = repository.getConnection();

		try {
			conn.begin();
			T result = processFunction.apply(conn);
			conn.commit();
			return result;
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
	 * @param <T>
	 *        The type of the return value.
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param processFunction
	 *        A {@link Function} that performs an action on the connection and
	 *        returns a result.
	 * @param exceptionHandler
	 *        A {@link Consumer} that handles an exception if one was generated.
	 * @return The result of applying the function, or <tt>null</tt> if an
	 *         exception occurs and the exception handler does not rethrow the
	 *         exception.
	 * @throws RepositoryException
	 *         If there was an exception dealing with the Repository.
	 * @throws UnknownTransactionStateException
	 *         If the transaction state was not properly recognised. (Optional
	 *         specific exception)
	 * @since 4.0
	 */
	public static <T> T commitOrRollback(Repository repository,
			Function<RepositoryConnection, T> processFunction, Consumer<RepositoryException> exceptionHandler)
	{
		try {
			return commitOrRollback(repository, processFunction);
		}
		catch (RepositoryException e) {
			exceptionHandler.accept(e);
			return null;
		}
	}

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Consumer}, before either rolling back the
	 * transaction if it failed, or committing the transaction if it was
	 * successful.
	 * 
	 * @param <T>
	 *        The type of the return value.
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param processFunction
	 *        A {@link Function} that performs an action on the connection and
	 *        returns a result.
	 * @return The result of applying the function, or <tt>null</tt> if an
	 *         exception is thrown.
	 * @since 4.0
	 */
	public static <T> T commitOrRollbackSilent(Repository repository,
			Function<RepositoryConnection, T> processFunction)
	{
		return commitOrRollback(repository, processFunction, e -> {
		});
	}

	/**
	 * Private constructor to prevent instantiation, this is a static helper
	 * class.
	 */
	private Repositories() {
	}

}
