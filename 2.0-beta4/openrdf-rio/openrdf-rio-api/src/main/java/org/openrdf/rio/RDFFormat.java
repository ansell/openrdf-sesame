/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A type-safe enumeration for RDF data serialization formats.
 */
public class RDFFormat {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The RDF/XML file format.
	 */
	public static final RDFFormat RDFXML = new RDFFormat("RDF/XML", "application/rdf+xml", "rdf",
			Charset.forName("UTF-8"));

	/**
	 * The N-Triples file format.
	 */
	public static final RDFFormat NTRIPLES = new RDFFormat("N-Triples", "text/plain", "nt",
			Charset.forName("US-ASCII"));

	/**
	 * The Turtle file format.
	 */
	public static final RDFFormat TURTLE = new RDFFormat("Turtle", "application/x-turtle", "ttl",
			Charset.forName("UTF-8"));

	/**
	 * The N3/Notation3 file format.
	 */
	public static final RDFFormat N3 = new RDFFormat("N3", "text/rdf+n3", "n3", Charset.forName("UTF-8"));

	/**
	 * The TriX file format.
	 */
	public static final RDFFormat TRIX = new RDFFormat("TriX", "application/trix", "xml",
			Charset.forName("UTF-8"));

	/*------------------*
	 * Static variables *
	 *------------------*/

	/**
	 * List of known RDF file formats.
	 */
	private static List<RDFFormat> RDF_FORMATS = new ArrayList<RDFFormat>(8);

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	static {
		// FIXME: base available format on available parsers/writers?
		register(RDFXML);
		register(NTRIPLES);
		register(TURTLE);
		register(N3);
		register(TRIX);
	}

	/*----------------*
	 * Static methods *
	 *----------------*/

	/**
	 * Returns all known/registered RDF formats.
	 */
	public static Collection<RDFFormat> values() {
		return Collections.unmodifiableList(RDF_FORMATS);
	}

	/**
	 * Registers the specified RDF file format.
	 * 
	 * @param name
	 *        The name of the RDF file format, e.g. "RDF/XML".
	 * @param mimeType
	 *        The MIME type of the RDF file format, e.g.
	 *        <tt>application/rdf+xml</tt> for the RDF/XML file format.
	 * @param fileExt
	 *        The (default) file extension for the RDF file format, e.g.
	 *        <tt>rdf</tt> for RDF/XML files.
	 */
	public static RDFFormat register(String name, String mimeType, String fileExt, Charset charset) {
		RDFFormat rdfFormat = new RDFFormat(name, mimeType, fileExt, charset);
		register(rdfFormat);
		return rdfFormat;
	}

	/**
	 * Registers the specified RDF file format.
	 */
	public static void register(RDFFormat rdfFormat) {
		RDF_FORMATS.add(rdfFormat);
	}

	/**
	 * Tries to determine the appropriate RDF file format based on the a MIME
	 * type that describes the content type.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/rdf+xml".
	 * @return An RDFFormat object if the MIME type was recognized, or
	 *         <tt>null</tt> otherwise.
	 * @see #forMIMEType(String,RDFFormat)
	 * @see #getMIMEType
	 */
	public static RDFFormat forMIMEType(String mimeType) {
		return forMIMEType(mimeType, null);
	}

	/**
	 * Tries to determine the appropriate RDF file format based on the a MIME
	 * type that describes the content type. The supplied fallback format will be
	 * returned when the MIME type was not recognized.
	 * 
	 * @param mimeType
	 *        A file name.
	 * @return An RDFFormat that matches the MIME type, or the fallback format if
	 *         the extension was not recognized.
	 * @see #forMIMEType(String)
	 * @see #getMIMEType
	 */
	public static RDFFormat forMIMEType(String mimeType, RDFFormat fallback) {
		for (RDFFormat format : RDF_FORMATS) {
			if (format.hasMIMEType(mimeType)) {
				return format;
			}
		}

		return fallback;
	}

	/**
	 * Tries to determine the appropriate RDF file format for a file, based on
	 * the extension specified in a file name.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An RDFFormat object if the file extension was recognized, or
	 *         <tt>null</tt> otherwise.
	 * @see #forFileName(String,RDFFormat)
	 * @see #getFileExtension
	 */
	public static RDFFormat forFileName(String fileName) {
		return forFileName(fileName, null);
	}

	/**
	 * Tries to determine the appropriate RDF file format for a file, based on
	 * the extension specified in a file name. The supplied fallback format will
	 * be returned when the file name extension was not recognized.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An RDFFormat that matches the file name extension, or the fallback
	 *         format if the extension was not recognized.
	 * @see #forFileName(String)
	 * @see #getFileExtension
	 */
	public static RDFFormat forFileName(String fileName, RDFFormat fallback) {
		// Strip any directory info from the file name
		int lastPathSepIdx = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		if (lastPathSepIdx >= 0) {
			fileName = fileName.substring(lastPathSepIdx + 1);
		}

		int dotIdx = fileName.lastIndexOf('.');
		if (dotIdx >= 0) {
			String ext = fileName.substring(dotIdx + 1);

			for (RDFFormat format : RDF_FORMATS) {
				if (format.hasFileExtension(ext)) {
					return format;
				}
			}
		}

		return fallback;
	}

	/**
	 * Returns the RDF format whose name matches the specified name.
	 * 
	 * @param formatName
	 *        A format name.
	 * @return The RDF format whose name matches the specified name, or
	 *         <tt>null</tt> if there is no such format.
	 */
	public static RDFFormat valueOf(String formatName) {
		for (RDFFormat format : RDF_FORMATS) {
			if (format.getName().equalsIgnoreCase(formatName)) {
				return format;
			}
		}

		return null;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The RDF Format's name.
	 */
	private String _name;

	/**
	 * The RDF format's MIME type.
	 */
	private String _mimeType;

	/**
	 * The RDF format's (default) file extension.
	 */
	private String _extension;

	/**
	 * The RDF format's (default) charset.
	 */
	private Charset _charset;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFFormat object.
	 * 
	 * @param name
	 *        The name of the RDF file format, e.g. "RDF/XML".
	 * @param mimeType
	 *        The MIME type of the RDF file format, e.g.
	 *        <tt>application/rdf+xml</tt> for the RDF/XML file format.
	 * @param fileExt
	 *        The (default) file extension for the RDF file format, e.g.
	 *        <tt>rdf</tt> for RDF/XML files.
	 */
	public RDFFormat(String name, String mimeType, String extension, Charset charset) {
		assert name != null : "name must not be null";
		assert mimeType != null : "mimeType must not be null";
		assert extension != null : "extension must not be null";

		_name = name;
		_mimeType = mimeType;
		_extension = extension;
		_charset = charset;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the name of this RDF file format.
	 * 
	 * @return A human-readable format name, e.g. "RDF/XML".
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Gets the default MIME type for this RDF file format.
	 * 
	 * @return A MIME type string, e.g. "application/rdf+xml".
	 */
	public String getMIMEType() {
		return _mimeType;
	}

	/**
	 * Checks if the RDFFormat's MIME type is equal to the specified MIME type.
	 * The MIME types are compared ignoring upper/lower-case differences.
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
	 * Gets the default file name extension for this RDF format.
	 * 
	 * @return A file name extension (excluding the dot), e.g. "rdf".
	 */
	public String getFileExtension() {
		return _extension;
	}

	/**
	 * Checks if the RDFFormat's file extension is equal to the specified file
	 * extension. The file extensions are compared ignoring upper/lower-case
	 * differences.
	 * 
	 * @param extension
	 *        The file extension to compare to the RDFFormat's file extension.
	 * @return <tt>true</tt> if the specified file extension is equal to the
	 *         RDFFormat's file extension.
	 */
	public boolean hasFileExtension(String extension) {
		return _extension.equalsIgnoreCase(extension);
	}

	/**
	 * Get the (default) charset for this RDF format.
	 * 
	 * @return the (default) charset for this RDF format, or null if this format
	 *         does not have a default charset.
	 */
	public Charset getCharset() {
		return _charset;
	}

	/**
	 * Checks if the RDFFormat has a (default) charset.
	 * 
	 * @return <tt>true</tt> if the RDFFormat has a (default) charset.
	 */
	public boolean hasCharset() {
		return _charset != null;
	}

	public boolean equals(Object other) {
		if (other instanceof RDFFormat) {
			RDFFormat o = (RDFFormat)other;
			return o.hasMIMEType(_mimeType) && o.hasFileExtension(_extension);
		}

		return false;
	}

	public int hashCode() {
		return _mimeType.toLowerCase(Locale.ENGLISH).hashCode();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(_name);
		sb.append(" (mimeType=").append(_mimeType);
		sb.append("; ext=").append(_extension).append(")");

		return sb.toString();
	}
}
