/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.lang.reflect.KeyedObjectFactory;
import info.aduna.lang.reflect.NoSuchTypeException;
import info.aduna.lang.reflect.TypeInstantiationException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;

/**
 * Utility class for creating query parsers and parsing queries in various query
 * languages.
 */
public class QueryParserUtil {

	private static KeyedObjectFactory<QueryLanguage, QueryParser> _parserFactory = new KeyedObjectFactory<QueryLanguage, QueryParser>();

	static {
		// TODO: initialize based on config file
		_registerParser(QueryLanguage.SERQL, "org.openrdf.query.parser.serql.SeRQLParser");
		_registerParser(QueryLanguage.SPARQL, "org.openrdf.query.parser.sparql.SPARQLParser");
		_registerParser(QueryLanguage.SERQO, "org.openrdf.query.parser.serqo.SeRQOParser");
	}

	private static void _registerParser(QueryLanguage ql, String className) {
		final Logger logger = LoggerFactory.getLogger(QueryParserUtil.class);

		try {
			@SuppressWarnings("unchecked")
			Class<? extends QueryParser> parserClass = (Class)Class.forName(className);
			_parserFactory.addType(ql, parserClass);
		}
		catch (ClassNotFoundException e) {
			logger.info("Unable to load query parser class: " + className);
		}
		catch (SecurityException e) {
			logger.warn("Not allowed to load query parser class: " + className, e);
		}
		catch (ClassCastException e) {
			logger.error("Parser class does not implement queryParser interface: " + className, e);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			logger.error("Unexpected error while trying to register query parser", e);
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
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the query
	 *        against, can be <tt>null</tt> if the query does not contain any relative URIs.
	 * @return The query model for the parsed query.
	 * @throws MalformedQueryException
	 *         If the supplied query was malformed.
	 * @throws UnsupportedQueryLanguageException
	 *         If the specified query language is not supported.
	 */
	public static ParsedQuery parseQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		QueryParser parser = QueryParserUtil.createParser(ql);
		return parser.parseQuery(query, baseURI);
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
	public static ParsedTupleQuery parseTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		ParsedQuery q = parseQuery(ql, query, baseURI);

		if (q instanceof ParsedTupleQuery) {
			return (ParsedTupleQuery)q;
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
	public static ParsedGraphQuery parseGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		ParsedQuery q = parseQuery(ql, query, baseURI);

		if (q instanceof ParsedGraphQuery) {
			return (ParsedGraphQuery)q;
		}
		else {
			throw new IllegalArgumentException("query is not a graph query: " + query);
		}
	}
}
