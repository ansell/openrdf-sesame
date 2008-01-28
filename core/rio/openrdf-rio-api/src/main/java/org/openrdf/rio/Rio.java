/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.ValueFactory;

/**
 * Factory class providing static methods for creating RDF parsers and -writers
 * for various RDF file formats.
 * 
 * @author Arjohn Kampman
 */
public class Rio {

	/**
	 * Tries to match a MIME type against the list of RDF formats that can be
	 * parsed.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/rdf+xml".
	 * @return An RDFFormat object if a match was found, or <tt>null</tt>
	 *         otherwise.
	 * @see #getParserFormatForMIMEType(String, RDFFormat)
	 */
	public static RDFFormat getParserFormatForMIMEType(String mimeType) {
		return getParserFormatForMIMEType(mimeType, null);
	}

	/**
	 * Tries to match a MIME type against the list of RDF formats that can be
	 * parsed. This method calls
	 * {@link RDFFormat#matchMIMEType(String, Iterable)} with the specified MIME
	 * type, the keys of {@link RDFParserRegistry#getInstance()} and the fallback
	 * format as parameters.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/rdf+xml".
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching RDFFormat, or <tt>fallback</tt> if no match was
	 *         found.
	 */
	public static RDFFormat getParserFormatForMIMEType(String mimeType, RDFFormat fallback) {
		return RDFFormat.matchMIMEType(mimeType, RDFParserRegistry.getInstance().getKeys(), fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An RDFFormat object if a match was found, or <tt>null</tt>
	 *         otherwise.
	 * @see #getParserFormatForFileName(String, RDFFormat)
	 */
	public static RDFFormat getParserFormatForFileName(String fileName) {
		return getParserFormatForFileName(fileName, null);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed. This method calls
	 * {@link RDFFormat#matchFileName(String, Iterable, info.aduna.lang.FileFormat)}
	 * with the specified MIME type, the keys of
	 * {@link RDFParserRegistry#getInstance()} and the fallback format as
	 * parameters.
	 * 
	 * @param fileName
	 *        A file name.
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching RDFFormat, or <tt>fallback</tt> if no match was
	 *         found.
	 */
	public static RDFFormat getParserFormatForFileName(String fileName, RDFFormat fallback) {
		return RDFFormat.matchFileName(fileName, RDFParserRegistry.getInstance().getKeys(), fallback);
	}

	/**
	 * Tries to match a MIME type against the list of RDF formats that can be
	 * written.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/rdf+xml".
	 * @return An RDFFormat object if a match was found, or <tt>null</tt>
	 *         otherwise.
	 * @see #getWriterFormatForMIMEType(String, RDFFormat)
	 */
	public static RDFFormat getWriterFormatForMIMEType(String mimeType) {
		return getWriterFormatForMIMEType(mimeType, null);
	}

	/**
	 * Tries to match a MIME type against the list of RDF formats that can be
	 * written. This method calls
	 * {@link RDFFormat#matchMIMEType(String, Iterable, info.aduna.lang.FileFormat)}
	 * with the specified MIME type, the keys of
	 * {@link RDFWriterRegistry#getInstance()} and the fallback format as
	 * parameters.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/rdf+xml".
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching RDFFormat, or <tt>fallback</tt> if no match was
	 *         found.
	 */
	public static RDFFormat getWriterFormatForMIMEType(String mimeType, RDFFormat fallback) {
		return RDFFormat.matchMIMEType(mimeType, RDFWriterRegistry.getInstance().getKeys(), fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An RDFFormat object if a match was found, or <tt>null</tt>
	 *         otherwise.
	 * @see #getWriterFormatForFileName(String, RDFFormat)
	 */
	public static RDFFormat getWriterFormatForFileName(String fileName) {
		return getWriterFormatForFileName(fileName, null);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written. This method calls
	 * {@link RDFFormat#matchFileName(String, Iterable, info.aduna.lang.FileFormat)}
	 * with the specified MIME type, the keys of
	 * {@link RDFWriterRegistry#getInstance()} and the fallback format as
	 * parameters.
	 * 
	 * @param fileName
	 *        A file name.
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching RDFFormat, or <tt>fallback</tt> if no match was
	 *         found.
	 */
	public static RDFFormat getWriterFormatForFileName(String fileName, RDFFormat fallback) {
		return RDFFormat.matchFileName(fileName, RDFWriterRegistry.getInstance().getKeys(), fallback);
	}

	/**
	 * Convenience methods for creating RDFParser objects. This method uses the
	 * registry returned by {@link RDFParserRegistry#getInstance()} to get a
	 * factory for the specified format and uses this factory to create the
	 * appropriate parser.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 */
	public static RDFParser createParser(RDFFormat format)
		throws UnsupportedRDFormatException
	{
		RDFParserFactory factory = RDFParserRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getParser();
		}

		throw new UnsupportedRDFormatException("No parser factory available for RDF format " + format);
	}

	/**
	 * Convenience methods for creating RDFParser objects that use the specified
	 * ValueFactory to create RDF model objects.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @see #createParser(RDFFormat)
	 * @see RDFParser#setValueFactory(ValueFactory)
	 */
	public static RDFParser createParser(RDFFormat format, ValueFactory valueFactory)
		throws UnsupportedRDFormatException
	{
		RDFParser rdfParser = createParser(format);
		rdfParser.setValueFactory(valueFactory);
		return rdfParser;
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method uses the
	 * registry returned by {@link RDFWriterRegistry#getInstance()} to get a
	 * factory for the specified format and uses this factory to create the
	 * appropriate writer.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format, OutputStream out)
		throws UnsupportedRDFormatException
	{
		RDFWriterFactory factory = RDFWriterRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getWriter(out);
		}

		throw new UnsupportedRDFormatException("No writer factory available for RDF format " + format);
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method uses the
	 * registry returned by {@link RDFWriterRegistry#getInstance()} to get a
	 * factory for the specified format and uses this factory to create the
	 * appropriate writer.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format, Writer writer)
		throws UnsupportedRDFormatException
	{
		RDFWriterFactory factory = RDFWriterRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getWriter(writer);
		}

		throw new UnsupportedRDFormatException("No writer factory available for RDF format " + format);
	}

	public static void main(String[] args)
		throws IOException, RDFParseException, RDFHandlerException, UnsupportedRDFormatException
	{
		if (args.length < 2) {
			System.out.println("Usage: java org.openrdf.rio.Rio <inputFile> <outputFile>");
			return;
		}

		// Create parser for input file
		String inputFile = args[0];
		FileInputStream inStream = new FileInputStream(inputFile);
		RDFFormat inputFormat = getParserFormatForFileName(inputFile, RDFFormat.RDFXML);
		RDFParser rdfParser = createParser(inputFormat);

		// Create writer for output file
		String outputFile = args[1];
		FileOutputStream outStream = new FileOutputStream(outputFile);
		RDFFormat outputFormat = getWriterFormatForFileName(outputFile, RDFFormat.RDFXML);
		RDFWriter rdfWriter = createWriter(outputFormat, outStream);

		rdfParser.setRDFHandler(rdfWriter);
		rdfParser.parse(inStream, "file:" + inputFile);

		inStream.close();
		outStream.close();
	}
}
