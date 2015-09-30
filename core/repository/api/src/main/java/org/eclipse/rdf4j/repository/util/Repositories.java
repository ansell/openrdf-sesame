/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.util;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.UnknownTransactionStateException;
import org.eclipse.rdf4j.rio.RDFHandler;

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
	 * Performs a SPARQL Select query on the given Repository and passes the
	 * results to the given {@link TupleQueryResultHandler}.
	 * 
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param query
	 *        The SPARQL Select query to execute.
	 * @param handler
	 *        A {@link TupleQueryResultHandler} that consumes the results.
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
	public static void tupleQuery(Repository repository, String query, TupleQueryResultHandler handler)
		throws RepositoryException, UnknownTransactionStateException, MalformedQueryException,
		QueryEvaluationException
	{
		consume(repository, conn -> {
			TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			preparedQuery.evaluate(handler);
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
	 * Performs a SPARQL Construct or Describe query on the given Repository and
	 * passes the results to the given {@link RDFHandler}.
	 * 
	 * @param repository
	 *        The {@link Repository} to open a connection to.
	 * @param query
	 *        The SPARQL Construct or Describe query to execute.
	 * @param handler
	 *        An {@link RDFHandler} that consumes the results.
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
	public static void graphQuery(Repository repository, String query, RDFHandler handler)
		throws RepositoryException, UnknownTransactionStateException, MalformedQueryException,
		QueryEvaluationException
	{
		consume(repository, conn -> {
			GraphQuery preparedQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
			preparedQuery.evaluate(handler);
		});
	}

	/**
	 * Private constructor to prevent instantiation, this is a static helper
	 * class.
	 */
	private Repositories() {
	}

}
