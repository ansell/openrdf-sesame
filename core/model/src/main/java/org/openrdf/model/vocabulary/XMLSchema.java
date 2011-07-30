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
 * Defines constants for the standard XML Schema datatypes.
 */
public class XMLSchema {

	/*
	 * The XML Schema namespace
	 */

	/** The XML Schema namespace (<tt>http://www.w3.org/2001/XMLSchema#</tt>). */
	public static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

	/*
	 * Primitive datatypes
	 */

	/** <tt>http://www.w3.org/2001/XMLSchema#duration</tt> */
	public final static URI DURATION;

	/** <tt>http://www.w3.org/2001/XMLSchema#dateTime</tt> */
	public final static URI DATETIME;

	/** <tt>http://www.w3.org/2001/XMLSchema#dayTimeDuration</tt> */
	public static final URI DAYTIMEDURATION;
	
	/** <tt>http://www.w3.org/2001/XMLSchema#time</tt> */
	public final static URI TIME;

	/** <tt>http://www.w3.org/2001/XMLSchema#date</tt> */
	public final static URI DATE;

	/** <tt>http://www.w3.org/2001/XMLSchema#gYearMonth</tt> */
	public final static URI GYEARMONTH;

	/** <tt>http://www.w3.org/2001/XMLSchema#gYear</tt> */
	public final static URI GYEAR;

	/** <tt>http://www.w3.org/2001/XMLSchema#gMonthDay</tt> */
	public final static URI GMONTHDAY;

	/** <tt>http://www.w3.org/2001/XMLSchema#gDay</tt> */
	public final static URI GDAY;

	/** <tt>http://www.w3.org/2001/XMLSchema#gMonth</tt> */
	public final static URI GMONTH;

	/** <tt>http://www.w3.org/2001/XMLSchema#string</tt> */
	public final static URI STRING;

	/** <tt>http://www.w3.org/2001/XMLSchema#boolean</tt> */
	public final static URI BOOLEAN;

	/** <tt>http://www.w3.org/2001/XMLSchema#base64Binary</tt> */
	public final static URI BASE64BINARY;

	/** <tt>http://www.w3.org/2001/XMLSchema#hexBinary</tt> */
	public final static URI HEXBINARY;

	/** <tt>http://www.w3.org/2001/XMLSchema#float</tt> */
	public final static URI FLOAT;

	/** <tt>http://www.w3.org/2001/XMLSchema#decimal</tt> */
	public final static URI DECIMAL;

	/** <tt>http://www.w3.org/2001/XMLSchema#double</tt> */
	public final static URI DOUBLE;

	/** <tt>http://www.w3.org/2001/XMLSchema#anyURI</tt> */
	public final static URI ANYURI;

	/** <tt>http://www.w3.org/2001/XMLSchema#QName</tt> */
	public final static URI QNAME;

	/** <tt>http://www.w3.org/2001/XMLSchema#NOTATION</tt> */
	public final static URI NOTATION;

	/*
	 * Derived datatypes
	 */

	/** <tt>http://www.w3.org/2001/XMLSchema#normalizedString</tt> */
	public final static URI NORMALIZEDSTRING;

	/** <tt>http://www.w3.org/2001/XMLSchema#token</tt> */
	public final static URI TOKEN;

	/** <tt>http://www.w3.org/2001/XMLSchema#language</tt> */
	public final static URI LANGUAGE;

	/** <tt>http://www.w3.org/2001/XMLSchema#NMTOKEN</tt> */
	public final static URI NMTOKEN;

	/** <tt>http://www.w3.org/2001/XMLSchema#NMTOKENS</tt> */
	public final static URI NMTOKENS;

	/** <tt>http://www.w3.org/2001/XMLSchema#Name</tt> */
	public final static URI NAME;

	/** <tt>http://www.w3.org/2001/XMLSchema#NCName</tt> */
	public final static URI NCNAME;

	/** <tt>http://www.w3.org/2001/XMLSchema#ID</tt> */
	public final static URI ID;

	/** <tt>http://www.w3.org/2001/XMLSchema#IDREF</tt> */
	public final static URI IDREF;

	/** <tt>http://www.w3.org/2001/XMLSchema#IDREFS</tt> */
	public final static URI IDREFS;

	/** <tt>http://www.w3.org/2001/XMLSchema#ENTITY</tt> */
	public final static URI ENTITY;

	/** <tt>http://www.w3.org/2001/XMLSchema#ENTITIES</tt> */
	public final static URI ENTITIES;

	/** <tt>http://www.w3.org/2001/XMLSchema#integer</tt> */
	public final static URI INTEGER;

	/** <tt>http://www.w3.org/2001/XMLSchema#long</tt> */
	public final static URI LONG;

	/** <tt>http://www.w3.org/2001/XMLSchema#int</tt> */
	public final static URI INT;

	/** <tt>http://www.w3.org/2001/XMLSchema#short</tt> */
	public final static URI SHORT;

	/** <tt>http://www.w3.org/2001/XMLSchema#byte</tt> */
	public final static URI BYTE;

	/** <tt>http://www.w3.org/2001/XMLSchema#nonPositiveInteger</tt> */
	public final static URI NON_POSITIVE_INTEGER;

	/** <tt>http://www.w3.org/2001/XMLSchema#negativeInteger</tt> */
	public final static URI NEGATIVE_INTEGER;

	/** <tt>http://www.w3.org/2001/XMLSchema#nonNegativeInteger</tt> */
	public final static URI NON_NEGATIVE_INTEGER;

	/** <tt>http://www.w3.org/2001/XMLSchema#positiveInteger</tt> */
	public final static URI POSITIVE_INTEGER;

	/** <tt>http://www.w3.org/2001/XMLSchema#unsignedLong</tt> */
	public final static URI UNSIGNED_LONG;

	/** <tt>http://www.w3.org/2001/XMLSchema#unsignedInt</tt> */
	public final static URI UNSIGNED_INT;

	/** <tt>http://www.w3.org/2001/XMLSchema#unsignedShort</tt> */
	public final static URI UNSIGNED_SHORT;

	/** <tt>http://www.w3.org/2001/XMLSchema#unsignedByte</tt> */
	public final static URI UNSIGNED_BYTE;

	static {
		ValueFactory factory = new ValueFactoryImpl();
		
		DURATION = factory.createURI(XMLSchema.NAMESPACE, "duration");

		DATETIME = factory.createURI(XMLSchema.NAMESPACE, "dateTime");

		DAYTIMEDURATION = factory.createURI(NAMESPACE, "dayTimeDuration");
		
		TIME = factory.createURI(XMLSchema.NAMESPACE, "time");

		DATE = factory.createURI(XMLSchema.NAMESPACE, "date");

		GYEARMONTH = factory.createURI(XMLSchema.NAMESPACE, "gYearMonth");

		GYEAR = factory.createURI(XMLSchema.NAMESPACE, "gYear");

		GMONTHDAY = factory.createURI(XMLSchema.NAMESPACE, "gMonthDay");

		GDAY = factory.createURI(XMLSchema.NAMESPACE, "gDay");

		GMONTH = factory.createURI(XMLSchema.NAMESPACE, "gMonth");

		STRING = factory.createURI(XMLSchema.NAMESPACE, "string");

		BOOLEAN = factory.createURI(XMLSchema.NAMESPACE, "boolean");

		BASE64BINARY = factory.createURI(XMLSchema.NAMESPACE, "base64Binary");

		HEXBINARY = factory.createURI(XMLSchema.NAMESPACE, "hexBinary");

		FLOAT = factory.createURI(XMLSchema.NAMESPACE, "float");

		DECIMAL = factory.createURI(XMLSchema.NAMESPACE, "decimal");

		DOUBLE = factory.createURI(XMLSchema.NAMESPACE, "double");

		ANYURI = factory.createURI(XMLSchema.NAMESPACE, "anyURI");

		QNAME = factory.createURI(XMLSchema.NAMESPACE, "QName");

		NOTATION = factory.createURI(XMLSchema.NAMESPACE, "NOTATION");

		NORMALIZEDSTRING = factory.createURI(XMLSchema.NAMESPACE, "normalizedString");

		TOKEN = factory.createURI(XMLSchema.NAMESPACE, "token");

		LANGUAGE = factory.createURI(XMLSchema.NAMESPACE, "language");

		NMTOKEN = factory.createURI(XMLSchema.NAMESPACE, "NMTOKEN");

		NMTOKENS = factory.createURI(XMLSchema.NAMESPACE, "NMTOKENS");

		NAME = factory.createURI(XMLSchema.NAMESPACE, "Name");

		NCNAME = factory.createURI(XMLSchema.NAMESPACE, "NCName");

		ID = factory.createURI(XMLSchema.NAMESPACE, "ID");

		IDREF = factory.createURI(XMLSchema.NAMESPACE, "IDREF");

		IDREFS = factory.createURI(XMLSchema.NAMESPACE, "IDREFS");

		ENTITY = factory.createURI(XMLSchema.NAMESPACE, "ENTITY");

		ENTITIES = factory.createURI(XMLSchema.NAMESPACE, "ENTITIES");

		INTEGER = factory.createURI(XMLSchema.NAMESPACE, "integer");

		LONG = factory.createURI(XMLSchema.NAMESPACE, "long");

		INT = factory.createURI(XMLSchema.NAMESPACE, "int");

		SHORT = factory.createURI(XMLSchema.NAMESPACE, "short");

		BYTE = factory.createURI(XMLSchema.NAMESPACE, "byte");

		NON_POSITIVE_INTEGER = factory.createURI(XMLSchema.NAMESPACE, "nonPositiveInteger");

		NEGATIVE_INTEGER = factory.createURI(XMLSchema.NAMESPACE, "negativeInteger");

		NON_NEGATIVE_INTEGER = factory.createURI(XMLSchema.NAMESPACE, "nonNegativeInteger");

		POSITIVE_INTEGER = factory.createURI(XMLSchema.NAMESPACE, "positiveInteger");

		UNSIGNED_LONG = factory.createURI(XMLSchema.NAMESPACE, "unsignedLong");

		UNSIGNED_INT = factory.createURI(XMLSchema.NAMESPACE, "unsignedInt");

		UNSIGNED_SHORT = factory.createURI(XMLSchema.NAMESPACE, "unsignedShort");

		UNSIGNED_BYTE = factory.createURI(XMLSchema.NAMESPACE, "unsignedByte");
	}
}
