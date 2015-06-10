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
package org.openrdf.repository.util;

import java.util.function.Consumer;
import java.util.function.Function;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.UnknownTransactionStateException;

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
	public static void consume(Repository repository, Consumer<RepositoryConnection> processFunction)
		throws RepositoryException, UnknownTransactionStateException
	{
		get(repository, conn -> {
			processFunction.accept(conn);
			return null;
		});
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
	public static void consume(Repository repository, Consumer<RepositoryConnection> processFunction,
			Consumer<RepositoryException> exceptionHandler)
		throws RepositoryException, UnknownTransactionStateException
	{
		try {
			consume(repository, processFunction);
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
	public static void consumeSilent(Repository repository, Consumer<RepositoryConnection> processFunction) {
		consume(repository, processFunction, e -> {
		});
	}

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Function}, before either rolling back the
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
	public static <T> T get(Repository repository, Function<RepositoryConnection, T> processFunction)
		throws RepositoryException, UnknownTransactionStateException
	{
		RepositoryConnection conn = null;

		try {
			conn = repository.getConnection();
			conn.begin();
			T result = processFunction.apply(conn);
			conn.commit();
			return result;
		}
		catch (RepositoryException e) {
			if (conn != null && conn.isActive()) {
				conn.rollback();
			}
			throw e;
		}
		finally {
			if (conn != null && conn.isOpen()) {
				conn.close();
			}
		}
	}

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Function}, before either rolling back the
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
	public static <T> T get(Repository repository, Function<RepositoryConnection, T> processFunction,
			Consumer<RepositoryException> exceptionHandler)
		throws RepositoryException, UnknownTransactionStateException
	{
		try {
			return get(repository, processFunction);
		}
		catch (RepositoryException e) {
			exceptionHandler.accept(e);
			return null;
		}
	}

	/**
	 * Opens a {@link RepositoryConnection} to the given Repository, sends the
	 * connection to the given {@link Function}, before either rolling back the
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
	public static <T> T getSilent(Repository repository, Function<RepositoryConnection, T> processFunction) {
		return get(repository, processFunction, e -> {
		});
	}

	/**
	 * Performs a SPARQL Select query on the given Repository and passes the
	 * results to the given {@link Function} with the result from the function
	 * returned by the method.
	 * 
	 * @param <T>
	 *        The type of the return value.
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param query
	 *        The SPARQL Select query to execute.
	 * @param processFunction
	 *        A {@link Function} that performs an action on the results of the
	 *        query and returns a result.
	 * @return The result of processing the query results.
	 * @throws RepositoryException
	 *         If there was an exception dealing with the Repository.
	 * @throws UnknownTransactionStateException
	 *         If the transaction state was not properly recognised. (Optional
	 *         specific exception)
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed
	 * @throws QueryEvaluationException
	 *         If there was an error evaluating the query
	 */
	public static <T> T tupleQuery(Repository repository, String query,
			Function<TupleQueryResult, T> processFunction)
		throws RepositoryException, UnknownTransactionStateException, MalformedQueryException,
		QueryEvaluationException
	{
		return get(repository, conn -> {
			TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			try (TupleQueryResult queryResult = preparedQuery.evaluate();) {
				return processFunction.apply(queryResult);
			}
		});
	}

	/**
	 * Performs a SPARQL Construct or Describe query on the given Repository and
	 * passes the results to the given {@link Function} with the result from the
	 * function returned by the method.
	 * 
	 * @param <T>
	 *        The type of the return value.
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param query
	 *        The SPARQL Construct or Describe query to execute.
	 * @param processFunction
	 *        A {@link Function} that performs an action on the results of the
	 *        query and returns a result.
	 * @return The result of processing the query results.
	 * @throws RepositoryException
	 *         If there was an exception dealing with the Repository.
	 * @throws UnknownTransactionStateException
	 *         If the transaction state was not properly recognised. (Optional
	 *         specific exception)
	 * @throws MalformedQueryException
	 *         If the supplied query is malformed
	 * @throws QueryEvaluationException
	 *         If there was an error evaluating the query
	 */
	public static <T> T graphQuery(Repository repository, String query,
			Function<GraphQueryResult, T> processFunction)
		throws RepositoryException, UnknownTransactionStateException, MalformedQueryException,
		QueryEvaluationException
	{
		return get(repository, conn -> {
			GraphQuery preparedQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
			try (GraphQueryResult queryResult = preparedQuery.evaluate();) {
				return processFunction.apply(queryResult);
			}
		});
	}

	/**
	 * Private constructor to prevent instantiation, this is a static helper
	 * class.
	 */
	private Repositories() {
	}

}
