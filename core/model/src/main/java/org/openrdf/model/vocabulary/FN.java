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
package org.openrdf.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Defines constants for the standard <a
 * href="http://www.w3.org/TR/xpath-functions/">XPath functions</a>.
 * 
 * @see <a href="http://www.w3.org/TR/xpath-functions/">XPath functions</a>
 * @author Jeen Broekstra
 */
public class FN {

	/**
	 * The XPath functions namespace (
	 * <tt>	http://www.w3.org/2005/xpath-functions#</tt>).
	 */
	public static final String NAMESPACE = "http://www.w3.org/2005/xpath-functions#";

	/**
	 * Recommended prefix for the XPath Functions namespace: "fn"
	 */
	public static final String PREFIX = "fn";

	/**
	 * An immutable {@link Namespace} constant that represents the XPath
	 * Functions namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	/** fn:concat */
	public static final IRI CONCAT;

	/** fn:contains */
	public static final IRI CONTAINS;

	/** fn:day-from-dateTime */
	public static final IRI DAY_FROM_DATETIME;

	/** fn:encode-for-uri */
	public static final IRI ENCODE_FOR_URI;

	/** fn:ends-with */
	public static final IRI ENDS_WITH;

	/** fn:hours-from-dateTime */
	public static final IRI HOURS_FROM_DATETIME;

	/** fn:lower-case */
	public static final IRI LOWER_CASE;

	/** fn:minutes-from-dateTime */
	public static final IRI MINUTES_FROM_DATETIME;

	/** fn:month-from-dateTime */
	public static final IRI MONTH_FROM_DATETIME;

	/** fn:numeric-abs */
	public static final IRI NUMERIC_ABS;

	/** fn:numeric-ceil */
	public static final IRI NUMERIC_CEIL;

	/** fn:numeric-floor */
	public static final IRI NUMERIC_FLOOR;

	/** fn:numeric-round */
	public static final IRI NUMERIC_ROUND;

	/** fn:replace */
	public static final IRI REPLACE;

	/** fn:seconds-from-dateTime */
	public static final IRI SECONDS_FROM_DATETIME;

	/** fn:starts-with */
	public static final IRI STARTS_WITH;

	/** fn:string-length */
	public static final IRI STRING_LENGTH;

	/** fn:substring */
	public static final IRI SUBSTRING;

	/** fn:substring-before */
	public static final IRI SUBSTRING_BEFORE;

	/** fn:substring-after */
	public static final IRI SUBSTRING_AFTER;

	/** fn:timezone-from-dateTime */
	public static final IRI TIMEZONE_FROM_DATETIME;

	/** fn:upper-case */
	public static final IRI UPPER_CASE;

	/** fn:year-from-dateTime */
	public static final IRI YEAR_FROM_DATETIME;

	static {
		ValueFactory f = SimpleValueFactory.getInstance();

		CONCAT = f.createIRI(NAMESPACE, "concat");

		CONTAINS = f.createIRI(NAMESPACE, "contains");

		DAY_FROM_DATETIME = f.createIRI(NAMESPACE, "day-from-dateTime");

		ENCODE_FOR_URI = f.createIRI(NAMESPACE, "encode-for-uri");

		ENDS_WITH = f.createIRI(NAMESPACE, "ends-with");

		HOURS_FROM_DATETIME = f.createIRI(NAMESPACE, "hours-from-dateTime");

		LOWER_CASE = f.createIRI(NAMESPACE, "lower-case");

		MINUTES_FROM_DATETIME = f.createIRI(NAMESPACE, "minutes-from-dateTime");

		MONTH_FROM_DATETIME = f.createIRI(NAMESPACE, "month-from-dateTime");

		NUMERIC_ABS = f.createIRI(NAMESPACE, "numeric-abs");

		NUMERIC_CEIL = f.createIRI(NAMESPACE, "numeric-ceil");

		NUMERIC_FLOOR = f.createIRI(NAMESPACE, "numeric-floor");

		NUMERIC_ROUND = f.createIRI(NAMESPACE, "numeric-round");

		REPLACE = f.createIRI(NAMESPACE, "replace");

		SECONDS_FROM_DATETIME = f.createIRI(NAMESPACE, "seconds-from-dateTime");

		STARTS_WITH = f.createIRI(NAMESPACE, "starts-with");

		STRING_LENGTH = f.createIRI(NAMESPACE, "string-length");

		SUBSTRING = f.createIRI(NAMESPACE, "substring");

		SUBSTRING_BEFORE = f.createIRI(NAMESPACE, "substring-before");

		SUBSTRING_AFTER = f.createIRI(NAMESPACE, "substring-after");

		TIMEZONE_FROM_DATETIME = f.createIRI(NAMESPACE, "timezone-from-dateTime");

		UPPER_CASE = f.createIRI(NAMESPACE, "upper-case");

		YEAR_FROM_DATETIME = f.createIRI(NAMESPACE, "year-from-dateTime");
	}
}
