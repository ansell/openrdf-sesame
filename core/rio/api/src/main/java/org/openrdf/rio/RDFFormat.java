/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import info.aduna.lang.FileFormat;

/**
 * Represents the concept of an RDF data serialization format. RDF formats are
 * identified by a {@link #getName() name} and can have one or more associated
 * MIME types, zero or more associated file extensions and can specify a
 * (default) character encoding. Some formats are able to encode context
 * information while other are not; this is indicated by the value of
 * {@link #supportsContexts}.
 * 
 * @author Arjohn Kampman
 */
public class RDFFormat extends FileFormat {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The RDF/XML file format.
	 */
	public static final RDFFormat RDFXML = new RDFFormat("RDF/XML", Arrays.asList("application/rdf+xml",
			"application/xml"), Charset.forName("UTF-8"), Arrays.asList("rdf", "rdfs", "owl", "xml"), true,
			false);

	/**
	 * The N-Triples file format.
	 */
	public static final RDFFormat NTRIPLES = new RDFFormat("N-Triples", "text/plain",
			Charset.forName("US-ASCII"), "nt", false, false);

	/**
	 * The Turtle file format.
	 */
	public static final RDFFormat TURTLE = new RDFFormat("Turtle", "application/x-turtle",
			Charset.forName("UTF-8"), "ttl", true, false);

	/**
	 * The N3/Notation3 file format.
	 */
	public static final RDFFormat N3 = new RDFFormat("N3", "text/rdf+n3", Charset.forName("UTF-8"), "n3",
			true, false);

	/**
	 * The TriX file format.
	 */
	public static final RDFFormat TRIX = new RDFFormat("TriX", "application/trix", Charset.forName("UTF-8"),
			Arrays.asList("xml", "trix"), false, true);

	/**
	 * The <a
	 * href="http://www.wiwiss.fu-berlin.de/suhl/bizer/TriG/Spec/">TriG</a> file
	 * format.
	 */
	public static final RDFFormat TRIG = new RDFFormat("TriG", "application/x-trig", Charset.forName("UTF-8"),
			"trig", true, true);

	public static final RDFFormat RDFA = new RDFFormat("RDFa", "application/xhtml+xml",
			Charset.forName("UTF-8"), "xhtml", false, false);

	/*------------------*
	 * Static variables *
	 *------------------*/

	/**
	 * List of known RDF file formats.
	 */
	// FIXME: remove/deprecate this list?
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
		register(TRIG);
		register(RDFA);
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
		RDFFormat rdfFormat = new RDFFormat(name, mimeType, charset, fileExt, false, false);
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
	 * @see #getMIMETypes()
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
	 * @see #getMIMETypes()
	 */
	public static RDFFormat forMIMEType(String mimeType, RDFFormat fallback) {
		return matchMIMEType(mimeType, RDF_FORMATS, fallback);
	}

	/**
	 * Tries to determine the appropriate RDF file format based on the extension
	 * of a file name.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An RDFFormat object if the file extension was recognized, or
	 *         <tt>null</tt> otherwise.
	 * @see #forFileName(String,RDFFormat)
	 * @see #getFileExtensions()
	 */
	public static RDFFormat forFileName(String fileName) {
		return forFileName(fileName, null);
	}

	/**
	 * Tries to determine the appropriate RDF file format based on the extension
	 * of a file name. The supplied fallback format will be returned when the
	 * file name extension was not recognized.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An RDFFormat that matches the file name extension, or the fallback
	 *         format if the extension was not recognized.
	 * @see #forFileName(String)
	 * @see #getFileExtensions()
	 */
	public static RDFFormat forFileName(String fileName, RDFFormat fallback) {
		return matchFileName(fileName, RDF_FORMATS, fallback);
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
	 * Flag indicating whether the RDFFormat can encode namespace information.
	 */
	private boolean supportsNamespaces = false;

	/**
	 * Flag indicating whether the RDFFormat can encode context information.
	 */
	private boolean supportsContexts = false;

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
	 * @param charset
	 *        The default character encoding of the RDF file format. Specify
	 *        <tt>null</tt> if not applicable.
	 * @param fileExtension
	 *        The (default) file extension for the RDF file format, e.g.
	 *        <tt>rdf</tt> for RDF/XML files.
	 */
	public RDFFormat(String name, String mimeType, Charset charset, String fileExtension,
			boolean supportsNamespaces, boolean supportsContexts)
	{
		this(name, Arrays.asList(mimeType), charset, Arrays.asList(fileExtension), supportsNamespaces,
				supportsContexts);
	}

	/**
	 * Creates a new RDFFormat object.
	 * 
	 * @param name
	 *        The name of the RDF file format, e.g. "RDF/XML".
	 * @param mimeType
	 *        The MIME type of the RDF file format, e.g.
	 *        <tt>application/rdf+xml</tt> for the RDF/XML file format.
	 * @param charset
	 *        The default character encoding of the RDF file format. Specify
	 *        <tt>null</tt> if not applicable.
	 * @param fileExtensions
	 *        The RDF format's file extensions, e.g. <tt>rdf</tt> for RDF/XML
	 *        files. The first item in the list is interpreted as the default
	 *        file extension for the format.
	 */
	public RDFFormat(String name, String mimeType, Charset charset, Collection<String> fileExtensions,
			boolean supportsNamespaces, boolean supportsContexts)
	{
		this(name, Arrays.asList(mimeType), charset, fileExtensions, supportsNamespaces, supportsContexts);
	}

	/**
	 * Creates a new RDFFormat object.
	 * 
	 * @param name
	 *        The name of the RDF file format, e.g. "RDF/XML".
	 * @param mimeTypes
	 *        The MIME types of the RDF file format, e.g.
	 *        <tt>application/rdf+xml</tt> for the RDF/XML file format. The first
	 *        item in the list is interpreted as the default MIME type for the
	 *        format.
	 * @param charset
	 *        The default character encoding of the RDF file format. Specify
	 *        <tt>null</tt> if not applicable.
	 * @param fileExtensions
	 *        The RDF format's file extensions, e.g. <tt>rdf</tt> for RDF/XML
	 *        files. The first item in the list is interpreted as the default
	 *        file extension for the format.
	 */
	public RDFFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions, boolean supportsNamespaces, boolean supportsContexts)
	{
		super(name, mimeTypes, charset, fileExtensions);

		this.supportsNamespaces = supportsNamespaces;
		this.supportsContexts = supportsContexts;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/*
	 * Return <tt>true</tt> if the RDFFormat supports the encoding of
	 * namespace/prefix information.
	 */
	public boolean supportsNamespaces() {
		return supportsNamespaces;
	}

	/*
	 * Return <tt>true</tt> if the RDFFormat supports the encoding of
	 * contexts/named graphs.
	 */
	public boolean supportsContexts() {
		return supportsContexts;
	}
}
