/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.openrdf.model.ValueFactory;

/**
 * An interface for RDF parsers. All implementing classes should define a public
 * zero-argument constructor to allow them to be created through reflection.
 */
public interface RDFParser {

	/*-----------*
	 * Constants *
	 *-----------*/

	public enum DatatypeHandling {
		/**
		 * Indicates that datatype semantics should be ignored.
		 */
		IGNORE,

		/**
		 * Indicates that values of datatyped literals should be verified.
		 */
		VERIFY,

		/**
		 * Indicates that values of datatyped literals should be normalized to
		 * their canonical representation.
		 */
		NORMALIZE
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the RDF format that this parser can parse.
	 */
	public RDFFormat getRDFFormat();

	/**
	 * Sets the ValueFactory that the parser will use to create Value objects for
	 * the parsed RDF data.
	 * 
	 * @param valueFactory
	 *        The value factory that the parser should use.
	 */
	public void setValueFactory(ValueFactory valueFactory);

	/**
	 * Sets the RDFHandler that will handle the parsed RDF data.
	 */
	public void setRDFHandler(RDFHandler handler);

	/**
	 * Sets the ParseErrorListener that will be notified of any errors that this
	 * parser finds during parsing.
	 */
	public void setParseErrorListener(ParseErrorListener el);

	/**
	 * Sets the ParseLocationListener that will be notified of the parser's
	 * progress during the parse process.
	 */
	public void setParseLocationListener(ParseLocationListener ll);

	/**
	 * Sets whether the parser should verify the data it parses (default value is
	 * <tt>true</tt>).
	 */
	public void setVerifyData(boolean verifyData);

	/**
	 * Set whether the parser should preserve bnode identifiers specified in the
	 * source (default is <tt>false</tt>).
	 */
	public void setPreserveBNodeIDs(boolean preserveBNodeIDs);

	/**
	 * Sets whether the parser should stop immediately if it finds an error in
	 * the data (default value is <tt>true</tt>).
	 */
	public void setStopAtFirstError(boolean stopAtFirstError);

	/**
	 * Sets the datatype handling mode. There are three modes for handling
	 * datatyped literals: <em>ignore</em>, <em>verify</em>and
	 * <em>normalize</em>. If set to <em>ignore</em>, no special action
	 * will be taken to handle datatyped literals. If set to <em>verify</em>,
	 * any literals with known (XML Schema built-in) datatypes are checked to see
	 * if their values are valid. If set to <em>normalize</em>, the literal
	 * values are not only checked, but also normalized to their canonical
	 * representation. The default value is <em>verify</em>.
	 * 
	 * @param datatypeHandling
	 *        A datatype handling option.
	 */
	public void setDatatypeHandling(DatatypeHandling datatypeHandling);

	/**
	 * Parses the data from the supplied InputStream, using the supplied baseURI
	 * to resolve any relative URI references.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @param baseURI
	 *        The URI associated with the data in the InputStream.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws RDFParseException
	 *         If the parser has found an unrecoverable parse error.
	 * @throws RDFHandlerException
	 *         If the configured statement handler has encountered an
	 *         unrecoverable error.
	 */
	public void parse(InputStream in, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException;

	/**
	 * Parses the data from the supplied Reader, using the supplied baseURI to
	 * resolve any relative URI references.
	 * 
	 * @param reader
	 *        The Reader from which to read the data.
	 * @param baseURI
	 *        The URI associated with the data in the InputStream.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws RDFParseException
	 *         If the parser has found an unrecoverable parse error.
	 * @throws RDFHandlerException
	 *         If the configured statement handler has encountered an
	 *         unrecoverable error.
	 */
	public void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException;
}
