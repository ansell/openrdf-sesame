/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A type-safe enumeration for RDF query languages. QueryLanguage objects are
 * identified by their name, which is treated in as case-insensitive way.
 */
public class QueryLanguage {

	/*-----------*
	 * Constants *
	 *-----------*/

	public static final QueryLanguage SERQL = new QueryLanguage("SeRQL");

	public static final QueryLanguage SPARQL = new QueryLanguage("SPARQL");

	public static final QueryLanguage SERQO = new QueryLanguage("SeRQO");

	/*------------------*
	 * Static variables *
	 *------------------*/

	/**
	 * List of known query languages.
	 */
	private static List<QueryLanguage> QUERY_LANGUAGES = new ArrayList<QueryLanguage>(4);

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	static {
		register(SERQL);
		register(SPARQL);
		register(SERQO);
	}

	/*----------------*
	 * Static methods *
	 *----------------*/

	/**
	 * Returns all known/registered query languages.
	 */
	public static Collection<QueryLanguage> values() {
		return Collections.unmodifiableList(QUERY_LANGUAGES);
	}

	/**
	 * Registers the specified query language.
	 * 
	 * @param name
	 *        The name of the query language, e.g. "SPARQL".
	 */
	public static QueryLanguage register(String name) {
		QueryLanguage ql = new QueryLanguage(name);
		register(ql);
		return ql;
	}

	/**
	 * Registers the specified query language.
	 */
	public static void register(QueryLanguage ql) {
		QUERY_LANGUAGES.add(ql);
	}

	/**
	 * Returns the query language whose name matches the specified name.
	 * 
	 * @param qlName
	 *        A query language name.
	 * @return The query language whose name matches the specified name, or
	 *         <tt>null</tt> if there is no such query language.
	 */
	public static QueryLanguage valueOf(String qlName) {
		for (QueryLanguage ql : QUERY_LANGUAGES) {
			if (ql.getName().equalsIgnoreCase(qlName)) {
				return ql;
			}
		}

		return null;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The query language's name.
	 */
	private String name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new QueryLanguage object.
	 * 
	 * @param name
	 *        The (case-insensitive) name of the query language, e.g. "SPARQL".
	 */
	public QueryLanguage(String name) {
		assert name != null : "name must not be null";

		this.name = name;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the name of this query language.
	 * 
	 * @return A human-readable format name, e.g. "SPARQL".
	 */
	public String getName() {
		return name;
	}

	public boolean hasName(String name) {
		return this.name.equalsIgnoreCase(name);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof QueryLanguage) {
			QueryLanguage o = (QueryLanguage)other;
			return this.hasName(o.getName());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getName().toUpperCase(Locale.ENGLISH).hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}
}
