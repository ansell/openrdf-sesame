/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A type-safe enumeration for tuple set (query results) serialization formats.
 */
public class TupleQueryResultFormat {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * SPARQL Query Results XML Format.
	 */
	public static final TupleQueryResultFormat SPARQL = new TupleQueryResultFormat("SPARQL/XML",
			"application/sparql-results+xml", "srx");

	/**
	 * Binary RDF results table format.
	 */
	public static final TupleQueryResultFormat BINARY = new TupleQueryResultFormat("BINARY",
			"application/x-binary-rdf-results-table", "brt");

	/**
	 * SPARQL Query Results JSON Format.
	 */
	public static final TupleQueryResultFormat JSON = new TupleQueryResultFormat("SPARQL/JSON",
			"application/sparql-results+json", "srj");

	/*------------------*
	 * Static variables *
	 *------------------*/

	/**
	 * List of known tuple query result formats.
	 */
	private static List<TupleQueryResultFormat> VALUES = new ArrayList<TupleQueryResultFormat>(8);

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	static {
		register(SPARQL);
		register(BINARY);
		register(JSON);
	}

	/*----------------*
	 * Static methods *
	 *----------------*/

	/**
	 * Returns all known/registered tuple query result formats.
	 */
	public static Collection<TupleQueryResultFormat> values() {
		return Collections.unmodifiableList(VALUES);
	}

	/**
	 * Registers the specified tuple query result format.
	 * 
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML file
	 *        format.
	 * @param fileExt
	 *        The (default) file extension for the format, e.g. <tt>srx</tt>
	 *        for SPARQL/XML files.
	 */
	public static TupleQueryResultFormat register(String name, String mimeType, String fileExt) {
		TupleQueryResultFormat format = new TupleQueryResultFormat(name, mimeType, fileExt);
		register(format);
		return format;
	}

	/**
	 * Registers the specified tuple query result format.
	 */
	public static void register(TupleQueryResultFormat format) {
		VALUES.add(format);
	}

	/**
	 * Tries to determine the appropriate tuple file format based on the a MIME
	 * type that describes the content type.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return A TupleQueryResultFormat object if the MIME type was recognized,
	 *         or <tt>null</tt> otherwise.
	 * @see #forMIMEType(String,TupleQueryResultFormat)
	 * @see #getMIMEType
	 */
	public static TupleQueryResultFormat forMIMEType(String mimeType) {
		return forMIMEType(mimeType, null);
	}

	/**
	 * Tries to determine the appropriate tuple file format based on the a MIME
	 * type that describes the content type. The supplied fallback format will be
	 * returned when the MIME type was not recognized.
	 * 
	 * @param mimeType
	 *        a MIME type, e.g. "application/sparql-results+xml"
	 * @param fallback
	 *        a fallback TupleQueryResultFormat that will be returned by the
	 *        method if no match for the supplied MIME type can be found.
	 * @return A TupleQueryResultFormat that matches the MIME type, or the
	 *         fallback format if the extension was not recognized.
	 * @see #forMIMEType(String)
	 * @see #getMIMEType
	 */
	public static TupleQueryResultFormat forMIMEType(String mimeType, TupleQueryResultFormat fallback) {
		for (TupleQueryResultFormat format : VALUES) {
			if (format.hasMIMEType(mimeType)) {
				return format;
			}
		}

		return fallback;
	}

	/**
	 * Tries to determine the appropriate tuple file format for a file, based on
	 * the extension specified in a file name.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return A TupleQueryResultFormat object if the file extension was
	 *         recognized, or <tt>null</tt> otherwise.
	 * @see #forFileName(String,TupleQueryResultFormat)
	 * @see #getFileExtension
	 */
	public static TupleQueryResultFormat forFileName(String fileName) {
		return forFileName(fileName, null);
	}

	/**
	 * Tries to determine the appropriate tuple file format for a file, based on
	 * the extension specified in a file name. The supplied fallback format will
	 * be returned when the file name extension was not recognized.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return A TupleQueryResultFormat that matches the file name extension, or
	 *         the fallback format if the extension was not recognized.
	 * @see #forFileName(String)
	 * @see #getFileExtension
	 */
	public static TupleQueryResultFormat forFileName(String fileName, TupleQueryResultFormat fallback) {
		// Strip any directory info from the file name
		int lastPathSepIdx = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		if (lastPathSepIdx >= 0) {
			fileName = fileName.substring(lastPathSepIdx + 1);
		}

		int dotIdx = fileName.lastIndexOf('.');
		if (dotIdx >= 0) {
			String ext = fileName.substring(dotIdx + 1);

			for (TupleQueryResultFormat format : VALUES) {
				if (format.hasFileExtension(ext)) {
					return format;
				}
			}
		}

		return fallback;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _name;

	private String _mimeType;

	private String _extension;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TupleQueryResultFormat object.
	 * 
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML
	 *        format.
	 * @param fileExt
	 *        The (default) file extension for the format, e.g. <tt>srx</tt>
	 *        for SPARQL/XML.
	 */
	public TupleQueryResultFormat(String name, String mimeType, String extension) {
		assert name != null : "name must not be null";
		assert mimeType != null : "mimeType must not be null";
		assert extension != null : "extension must not be null";

		_name = name;
		_mimeType = mimeType;
		_extension = extension;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the name of this tuple query result format.
	 * 
	 * @return A human-readable format name, e.g. "SPARQL/XML".
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Gets the default MIME type for this tuple file format.
	 * 
	 * @return A MIME type string, e.g. "application/sparql-results+xml".
	 */
	public String getMIMEType() {
		return _mimeType;
	}

	/**
	 * Checks if the TupleQueryResultFormat's MIME type is equal to the specified
	 * MIME type. The MIME types are compared ignoring upper/lower-case
	 * differences.
	 * 
	 * @param mimeType
	 *        The MIME type to compare to the RDFFormat's MIME type.
	 * @return <tt>true</tt> if the specified MIME type is equal to the
	 *         RDFFormat's MIME type.
	 */
	public boolean hasMIMEType(String mimeType) {
		return _mimeType.equalsIgnoreCase(mimeType);
	}

	/**
	 * Gets the default file name extension for this tuple format.
	 * 
	 * @return A file name extension (excluding the dot), e.g. "srx".
	 */
	public String getFileExtension() {
		return _extension;
	}

	/**
	 * Checks if the TupleQueryResultFormat's file extension is equal to the
	 * specified file extension. The file extensions are compared ignoring
	 * upper/lower-case differences.
	 * 
	 * @param extension
	 *        The file extension to compare to the TupleQueryResultFormat's file
	 *        extension.
	 * @return <tt>true</tt> if the specified file extension is equal to the
	 *         TupleQueryResultFormat's file extension.
	 */
	public boolean hasFileExtension(String extension) {
		return _extension.equalsIgnoreCase(extension);
	}
}