/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage;

import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.Query;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.util.log.ThreadLog;
import org.openrdf.util.reflect.KeyedObjectFactory;
import org.openrdf.util.reflect.NoSuchTypeException;
import org.openrdf.util.reflect.TypeInstantiationException;

/**
 * Utility class for creating query parsers and parsing queries in various query
 * languages.
 */
public class QueryParserUtil {

	private static KeyedObjectFactory<QueryLanguage, QueryParser> _parserFactory = new KeyedObjectFactory<QueryLanguage, QueryParser>();

	static {
		// TODO: initialize based on config file
		_registerParser(QueryLanguage.SERQL, "org.openrdf.querylanguage.serql.SeRQLParser");
		_registerParser(QueryLanguage.SPARQL, "org.openrdf.querylanguage.sparql.SPARQLParser");
	}

	private static void _registerParser(QueryLanguage ql, String className) {
		try {
			Class parserClass = Class.forName(className);
			_parserFactory.addType(ql, (Class<? extends QueryParser>)parserClass);
		}
		catch (ClassNotFoundException e) {
			ThreadLog.log("Unable to load query parser class: " + className, e);
		}
		catch (SecurityException e) {
			ThreadLog.warning("Not allowed to load query parser class: " + className, e);
		}
		catch (ClassCastException e) {
			ThreadLog.error("Parser class does not implement queryParser interface: " + className, e);
		}
		catch (IllegalArgumentException e) {
			ThreadLog.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			ThreadLog.error("Unexpected error while trying to register query parser", e);
		}
	}

	public static KeyedObjectFactory<QueryLanguage, QueryParser> getQueryParserFactory() {
		return _parserFactory;
	}

	public static QueryParser createParser(QueryLanguage ql)
		throws UnsupportedQueryLanguageException
	{
		try {
			return _parserFactory.createInstance(ql);
		}
		catch (NoSuchTypeException e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (TypeInstantiationException e) {
			throw new UnsupportedQueryLanguageException(e);
		}
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The langauge in which the query is formulated.
	 * @param query
	 *        The query.
	 * @return The query model for the parsed query.
	 * @throws MalformedQueryException
	 *         If the supplied query was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static Query parseQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryParser parser = QueryParserUtil.createParser(ql);
		return parser.parseQuery(query);
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The langauge in which the query is formulated.
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
	public static TupleQuery parseTupleQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		Query q = parseQuery(ql, query);

		if (q instanceof TupleQuery) {
			return (TupleQuery)q;
		}
		else {
			throw new IllegalArgumentException("query is not a tuple query: " + query);
		}
	}

	/**
	 * Parses the supplied query into a query model.
	 * 
	 * @param ql
	 *        The langauge in which the query is formulated.
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
	public static GraphQuery parseGraphQuery(QueryLanguage ql, String query)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		Query q = parseQuery(ql, query);

		if (q instanceof GraphQuery) {
			return (GraphQuery)q;
		}
		else {
			throw new IllegalArgumentException("query is not a graph query: " + query);
		}
	}
}
