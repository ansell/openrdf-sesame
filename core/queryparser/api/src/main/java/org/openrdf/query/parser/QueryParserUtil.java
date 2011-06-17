/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.IncompatibleOperationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;

/**
 * Utility class for creating query parsers and parsing queries in various query
 * languages.
 */
public class QueryParserUtil {

	public static QueryParser createParser(QueryLanguage ql)
		throws UnsupportedQueryLanguageException
	{
		QueryParserFactory factory = QueryParserRegistry.getInstance().get(ql);

		if (factory != null) {
			return factory.getParser();
		}

		throw new UnsupportedQueryLanguageException("No factory available for query language " + ql);
	}

	/**
	 * Parses the supplied operation into a query model.
	 * 
	 * @param ql
	 *        The language in which the operation is formulated.
	 * @param operation
	 *        The operation.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the operation
	 *        against, can be <tt>null</tt> if the operation does not contain any
	 *        relative URIs.
	 * @return The model for the parsed operation.
	 * @throws MalformedQueryException
	 *         If the supplied operation was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedOperation parseOperation(QueryLanguage ql, String operation, String baseURI)
		throws MalformedQueryException
	{
		QueryParser parser = createParser(ql);
		ParsedOperation parsedOperation = null;
		try {
			parsedOperation = parser.parseQuery(operation, baseURI);
		}
		catch (IncompatibleOperationException e) {
			parsedOperation = parser.parseUpdate(operation, baseURI);
		}
		return parsedOperation;
	}

	
	/**
	 * Parses the supplied update operation into a query model.
	 * 
	 * @param ql
	 *        The language in which the update operation is formulated.
	 * @param update
	 *        The update operation.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the operation
	 *        against, can be <tt>null</tt> if the update operation does not contain any
	 *        relative URIs.
	 * @return The model for the parsed update operation.
	 * @throws MalformedQueryException
	 *         If the supplied update operation was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedUpdate parseUpdate(QueryLanguage ql, String update, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryParser parser = createParser(ql);
		return parser.parseUpdate(update, baseURI);
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the query
	 *        against, can be <tt>null</tt> if the query does not contain any
	 *        relative URIs.
	 * @return The query model for the parsed query.
	 * @throws MalformedQueryException
	 *         If the supplied query was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedQuery parseQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryParser parser = createParser(ql);
		return parser.parseQuery(query, baseURI);
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @return The query model for the parsed query.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a tuple query.
	 * @throws MalformedQueryException
	 *         If the supplied query was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedTupleQuery parseTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		ParsedOperation q = parseQuery(ql, query, baseURI);

		if (q instanceof ParsedTupleQuery) {
			return (ParsedTupleQuery)q;
		}

		throw new IllegalArgumentException("query is not a tuple query: " + query);
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @return The query model for the parsed query.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a graph query.
	 * @throws MalformedQueryException
	 *         If the supplied query was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedGraphQuery parseGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		ParsedOperation q = parseQuery(ql, query, baseURI);

		if (q instanceof ParsedGraphQuery) {
			return (ParsedGraphQuery)q;
		}

		throw new IllegalArgumentException("query is not a graph query: " + query);
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The language in which the query is formulated.
	 * @param query
	 *        The query.
	 * @return The query model for the parsed query.
	 * @throws IllegalArgumentException
	 *         If the supplied query is not a graph query.
	 * @throws MalformedQueryException
	 *         If the supplied query was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedBooleanQuery parseBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		ParsedOperation q = parseQuery(ql, query, baseURI);

		if (q instanceof ParsedBooleanQuery) {
			return (ParsedBooleanQuery)q;
		}

		throw new IllegalArgumentException("query is not a boolean query: " + query);
	}
}
