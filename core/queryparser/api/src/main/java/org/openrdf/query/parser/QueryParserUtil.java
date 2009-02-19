/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.algebra.QueryModel;

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
	public static QueryModel parseQuery(QueryLanguage ql, String query, String baseURI)
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
	public static TupleQueryModel parseTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryModel q = parseQuery(ql, query, baseURI);

		if (q instanceof TupleQueryModel) {
			return (TupleQueryModel)q;
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
	public static GraphQueryModel parseGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryModel q = parseQuery(ql, query, baseURI);

		if (q instanceof GraphQueryModel) {
			return (GraphQueryModel)q;
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
	public static BooleanQueryModel parseBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryModel q = parseQuery(ql, query, baseURI);

		if (q instanceof BooleanQueryModel) {
			return (BooleanQueryModel)q;
		}

		throw new IllegalArgumentException("query is not a boolean query: " + query);
	}
}
