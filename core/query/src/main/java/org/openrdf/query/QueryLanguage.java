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
package org.openrdf.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A type-safe enumeration for RDF query languages such as {@link #SERQL SeRQL}
 * and {@link #SPARQL}. QueryLanguage objects are identified by their name,
 * which is treated in as case-insensitive way.
 */
public class QueryLanguage {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * SeRQL (Sesame RDF Query Language), a Sesame-specific query language for
	 * RDF that predates SPARQL.
	 * 
	 * @see <a href="http://rdf4j.org/doc/serql">The SeRQL user manual</a>
	 */
	public static final QueryLanguage SERQL = new QueryLanguage("SeRQL");

	/**
	 * SPARQL (Simple Protocol and RDF Query Language) is a W3C Recommendation
	 * for query and update of RDF datasets.
	 * 
	 * @see <a href="http://www.w3.org/TR/sparql11-overview/">SPARQL 1.1
	 *      Overview</a>
	 */
	public static final QueryLanguage SPARQL = new QueryLanguage("SPARQL");

	/**
	 * SeRQO (Sesame RDF Query Language - Objects) is a Sesame-specific query
	 * language using a syntax suited less for human editing but for easy
	 * transfer over the wire.
	 * 
	 * @deprecated since 4.0. This language is no longer actively supported.
	 */
	@Deprecated
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
