/*  Copyright (C) 2001-2006 Aduna (http://www.aduna-software.com/)
 *
 *  This software is part of the Sesame Framework. It is licensed under
 *  the following two licenses as alternatives:
 *
 *   1. Open Software License (OSL) v3.0
 *   2. GNU Lesser General Public License (LGPL) v2.1 or any newer
 *      version
 *
 *  By using, modifying or distributing this software you agree to be 
 *  bound by the terms of at least one of the above licenses.
 *
 *  See the file LICENSE.txt that is distributed with this software
 *  for the complete terms and further details.
 */
package org.openrdf.queryresult;

/**
 * A type-safe enumeration for tuple set (query results) serialization
 * formats.
 */
public enum TupleQueryResultFormat {

	/**
	 * Constant representing the SPARQL Query Results XML Format.
	 */
	SPARQL("application/sparql-results+xml", "srx"),

	/**
	 * Constant representing the binary RDF results table format.
	 */
	BINARY("application/x-binary-rdf-results-table", "brt"),
	
	/**
	 * Constant representing the SPARQL Query Results JSON Format.
	 */
	JSON("application/sparql-results+json", "srj");

	
	private String _mimeType;
	private String _extension;

	TupleQueryResultFormat(String mimeType, String extension) {
		_mimeType = mimeType;
		_extension = extension;
	}
	
	/**
	 * Tries to determine the appropriate tuple file format based on the a MIME
	 * type that describes the content type.
	 *
	 * @param mimeType A MIME type, e.g. "application/sparql-results+xml".
	 * @return A TupleQueryResultFormat object if the MIME type was recognized, or
	 * <tt>null</tt> otherwise.
	 *
	 * @see #forMIMEType(String,TupleQueryResultFormat)
	 * @see #getMIMEType
	 */
	public static TupleQueryResultFormat forMIMEType(String mimeType) {
		return forMIMEType(mimeType, null);
	}

	/**
	 * Tries to determine the appropriate tuple file format based on the a MIME
	 * type that describes the content type. The supplied fallback format will
	 * be returned when the MIME type was not recognized.
	 *
	 * @param mimeType a MIME type, e.g. "application/sparql-results+xml"
	 * @param fallback a fallback TupleQueryResultFormat that will be returned by the method
	 *         if no match for the supplied MIME type can be found.
	 * @return A TupleQueryResultFormat that matches the MIME type, or the fallback format
	 * if the extension was not recognized.
	 *
	 * @see #forMIMEType(String)
	 * @see #getMIMEType
	 */
	public static TupleQueryResultFormat forMIMEType(String mimeType, TupleQueryResultFormat fallback) {
		TupleQueryResultFormat result = fallback;
		
		for (TupleQueryResultFormat format: TupleQueryResultFormat.values()) {
			if (format.getMIMEType().equals(mimeType)) {
				result = format;
				break;
			}
		}
		
		return result;
	}
	/**
	 * Tries to determine the appropriate tuple file format for a file, based on
	 * the extension specified in a file name.
	 *
	 * @param fileName A file name.
	 * @return A TupleQueryResultFormat object if the file extension was recognized, or
	 * <tt>null</tt> otherwise.
	 *
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
	 * @param fileName A file name.
	 * @return A TupleQueryResultFormat that matches the file name extension, or the
	 * fallback format if the extension was not recognized.
	 *
	 * @see #forFileName(String)
	 * @see #getFileExtension
	 */
	public static TupleQueryResultFormat forFileName(String fileName, TupleQueryResultFormat fallback) {
		TupleQueryResultFormat result = fallback;

		int lastPathSepIdx = Math.max( fileName.lastIndexOf('/'), fileName.lastIndexOf('\\') );
		if (lastPathSepIdx >= 0) {
			// Strip directory info from file name
			fileName = fileName.substring(lastPathSepIdx + 1);
		}

		int dotIdx = fileName.lastIndexOf('.');
		if (dotIdx >= 0) {
			String ext = fileName.substring(dotIdx + 1);
			
			for (TupleQueryResultFormat format: TupleQueryResultFormat.values()) {
				if (format.getFileExtension().equals(ext)) {
					result = format;
					break;
				}
			}
		}

		return result;
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
	 * Gets the default file name extension for this tuple format.
	 * 
	 * @return A file name extension (excluding the dot), e.g. "srx".
	 */
	public String getFileExtension() {
		return _extension;
	}
}