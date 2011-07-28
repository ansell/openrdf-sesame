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

	/** fn:concat */
	public static final URI CONCAT;

	/** fn:contains */
	public static final URI CONTAINS;

	/** fn:encode-for-uri */
	public static final URI ENCODE_FOR_URI;
	
	/** fn:ends-with */
	public static final URI ENDS_WITH;
	
	/** fn:lower-case */
	public static final URI LOWER_CASE;
	
	/** fn:starts-with */
	public static final URI STARTS_WITH;
	
	/** fn:string-length */
	public static final URI STRING_LENGTH;
	
	/** fn:substring */
	public static final URI SUBSTRING;
	
	/** fn:upper-case */
	public static final URI UPPER_CASE;

	static {
		ValueFactory f = new ValueFactoryImpl();

		CONCAT = f.createURI(NAMESPACE, "concat");

		CONTAINS = f.createURI(NAMESPACE, "contains");

		ENCODE_FOR_URI = f.createURI(NAMESPACE, "encode-for-uri");
		
		ENDS_WITH = f.createURI(NAMESPACE, "ends-with");
		
		LOWER_CASE = f.createURI(NAMESPACE, "lower-case");
		
		STARTS_WITH = f.createURI(NAMESPACE, "starts-with");
		
		STRING_LENGTH = f.createURI(NAMESPACE, "string-length");
		
		SUBSTRING = f.createURI(NAMESPACE, "substring");
		
		UPPER_CASE = f.createURI(NAMESPACE, "upper-case");
	}
}
