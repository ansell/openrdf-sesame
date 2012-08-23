/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the standard <a
 * href="http://www.w3.org/TR/xpath-functions/">XPath functions</a>.
 * 
 * @see http://www.w3.org/TR/xpath-functions/
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
	
	/** fn:concat */
	public static final URI CONCAT;

	/** fn:contains */
	public static final URI CONTAINS;

	/** fn:day-from-dateTime */
	public static final URI DAY_FROM_DATETIME;
	
	/** fn:encode-for-uri */
	public static final URI ENCODE_FOR_URI;
	
	/** fn:ends-with */
	public static final URI ENDS_WITH;
	
	/** fn:hours-from-dateTime */
	public static final URI HOURS_FROM_DATETIME;
	
	/** fn:lower-case */
	public static final URI LOWER_CASE;

	/** fn:minutes-from-dateTime */
	public static final URI MINUTES_FROM_DATETIME;
	
	/** fn:month-from-dateTime */
	public static final URI MONTH_FROM_DATETIME;
	
	/** fn:numeric-abs */
	public static final URI NUMERIC_ABS;
	
	/** fn:numeric-ceil */
	public static final URI NUMERIC_CEIL;
	
	/** fn:numeric-floor */
	public static final URI NUMERIC_FLOOR;
	
	/** fn:numeric-round */
	public static final URI NUMERIC_ROUND;
	
	/** fn:replace */
	public static final URI REPLACE;
	
	/** fn:seconds-from-dateTime */
	public static final URI SECONDS_FROM_DATETIME;
	
	/** fn:starts-with */
	public static final URI STARTS_WITH;
	
	/** fn:string-length */
	public static final URI STRING_LENGTH;
	
	/** fn:substring */
	public static final URI SUBSTRING;
	
	/** fn:substring-before */
	public static final URI SUBSTRING_BEFORE;
	
	/** fn:substring-after */
	public static final URI SUBSTRING_AFTER;
	
	/** fn:timezone-from-dateTime */
	public static final URI TIMEZONE_FROM_DATETIME;
	
	/** fn:upper-case */
	public static final URI UPPER_CASE;

	/** fn:year-from-dateTime */
	public static final URI YEAR_FROM_DATETIME;
	


	static {
		ValueFactory f = new ValueFactoryImpl();

		CONCAT = f.createURI(NAMESPACE, "concat");

		CONTAINS = f.createURI(NAMESPACE, "contains");

		DAY_FROM_DATETIME = f.createURI(NAMESPACE, "day-from-dateTime");
		
		ENCODE_FOR_URI = f.createURI(NAMESPACE, "encode-for-uri");
		
		ENDS_WITH = f.createURI(NAMESPACE, "ends-with");
		
		HOURS_FROM_DATETIME = f.createURI(NAMESPACE, "hours-from-dateTime");
		
		LOWER_CASE = f.createURI(NAMESPACE, "lower-case");
		
		MINUTES_FROM_DATETIME = f.createURI(NAMESPACE, "minutes-from-dateTime");
		
		MONTH_FROM_DATETIME = f.createURI(NAMESPACE, "month-from-dateTime");
		
		NUMERIC_ABS = f.createURI(NAMESPACE, "numeric-abs");
		
		NUMERIC_CEIL = f.createURI(NAMESPACE, "numeric-ceil");

		NUMERIC_FLOOR = f.createURI(NAMESPACE, "numeric-floor");
		
		NUMERIC_ROUND = f.createURI(NAMESPACE, "numeric-round");
		
		REPLACE = f.createURI(NAMESPACE, "replace");
		
		SECONDS_FROM_DATETIME = f.createURI(NAMESPACE, "seconds-from-dateTime");
		
		STARTS_WITH = f.createURI(NAMESPACE, "starts-with");
		
		STRING_LENGTH = f.createURI(NAMESPACE, "string-length");
		
		SUBSTRING = f.createURI(NAMESPACE, "substring");
		
		SUBSTRING_BEFORE = f.createURI(NAMESPACE, "substring-before");
		
		SUBSTRING_AFTER = f.createURI(NAMESPACE, "substring-after");
		
		TIMEZONE_FROM_DATETIME = f.createURI(NAMESPACE, "timezone-from-dateTime");
		
		UPPER_CASE = f.createURI(NAMESPACE, "upper-case");
		
		YEAR_FROM_DATETIME = f.createURI(NAMESPACE, "year-from-dateTime");
	}
}
