/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import info.aduna.lang.FileFormat;

/**
 * Represents the concept of an tuple query result serialization format. Tuple
 * query result formats are identified by a {@link #getName() name} and can have
 * one or more associated MIME types, zero or more associated file extensions
 * and can specify a (default) character encoding.
 * 
 * @author Arjohn Kampman
 */
public class TupleQueryResultFormat extends FileFormat {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * SPARQL Query Results XML Format.
	 */
	public static final TupleQueryResultFormat SPARQL = new TupleQueryResultFormat("SPARQL/XML",
			Arrays.asList("application/sparql-results+xml", "application/xml"), Charset.forName("UTF-8"), Arrays.asList("srx", "xml"));

	/**
	 * Binary RDF results table format.
	 */
	public static final TupleQueryResultFormat BINARY = new TupleQueryResultFormat("BINARY",
			"application/x-binary-rdf-results-table", null, "brt");

	/**
	 * SPARQL Query Results JSON Format.
	 */
	public static final TupleQueryResultFormat JSON = new TupleQueryResultFormat("SPARQL/JSON",
			"application/sparql-results+json", Charset.forName("UTF-8"), "srj");

	/**
	 * SPARQL Query Result CSV Format.
	 */
	public static final TupleQueryResultFormat CSV =  new TupleQueryResultFormat("SPARQL/CSV",
			"text/csv", Charset.forName("UTF-8"), "csv");
	
	
	/**
	 * SPARQL Query Result TSV Format.
	 */
//	public static final TupleQueryResultFormat TSV =  new TupleQueryResultFormat("SPARQL/TSV",
//			"text/tab-separated-values", Charset.forName("UTF-8"), "tsv");

	
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
		register(CSV);
//		register(TSV);
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
	 * @see #getMIMETypes
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
	 * @see #getMIMETypes
	 */
	public static TupleQueryResultFormat forMIMEType(String mimeType, TupleQueryResultFormat fallback) {
		return matchMIMEType(mimeType, VALUES, fallback);
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
	 * @see #getFileExtensions
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
	 * @see #getFileExtensions
	 */
	public static TupleQueryResultFormat forFileName(String fileName, TupleQueryResultFormat fallback) {
		return matchFileName(fileName, VALUES, fallback);
	}

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
	public TupleQueryResultFormat(String name, String mimeType, String fileExt) {
		this(name, mimeType, null, fileExt);
	}

	/**
	 * Creates a new TupleQueryResultFormat object.
	 * 
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML
	 *        format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExt
	 *        The (default) file extension for the format, e.g. <tt>srx</tt>
	 *        for SPARQL/XML.
	 */
	public TupleQueryResultFormat(String name, String mimeType, Charset charset, String fileExt) {
		super(name, mimeType, charset, fileExt);
	}

	/**
	 * Creates a new TupleQueryResultFormat object.
	 * 
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeTypes
	 *        The MIME types of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML
	 *        format. The first item in the list is interpreted as the default
	 *        MIME type for the format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExtensions
	 *        The format's file extensions, e.g. <tt>srx</tt> for SPARQL/XML
	 *        files. The first item in the list is interpreted as the default
	 *        file extension for the format.
	 */
	public TupleQueryResultFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions)
	{
		super(name, mimeTypes, charset, fileExtensions);
	}
}