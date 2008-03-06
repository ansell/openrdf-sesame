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
 */
public class Rio {

	private static RDFParserRegistry parserRegistry = new RDFParserRegistry();

	private static RDFWriterRegistry writerRegistry = new RDFWriterRegistry();

	/**
	 * Gets the registry for {@link RDFParserFactory}s that is used to create
	 * RDF parsers.
	 */
	public static RDFParserRegistry getRDFParserRegistry() {
		return parserRegistry;
	}

	/**
	 * Gets the registry for {@link RDFWriterFactory}s that is used to create
	 * RDF writers.
	 */
	public static RDFWriterRegistry getRDFWriterRegistry() {
		return writerRegistry;
	}

	/**
	 * Convenience methods for creating RDFParser objects. This method uses the
	 * registry returned by {@link #getRDFParserRegistry()} to get a factory for
	 * the specified format and uses this factory to create the appropriate
	 * parser.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 */
	public static RDFParser createParser(RDFFormat format)
		throws UnsupportedRDFormatException
	{
		RDFParserFactory factory = getRDFParserRegistry().get(format);

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
	 * registry returned by {@link #getRDFWriterRegistry()} to get a factory for
	 * the specified format and uses this factory to create the appropriate
	 * writer.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format, OutputStream out)
		throws UnsupportedRDFormatException
	{
		RDFWriterFactory factory = getRDFWriterRegistry().get(format);

		if (factory != null) {
			return factory.getWriter(out);
		}

		throw new UnsupportedRDFormatException("No writer factory available for RDF format " + format);
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method uses the
	 * registry returned by {@link #getRDFWriterRegistry()} to get a factory for
	 * the specified format and uses this factory to create the appropriate
	 * writer.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format, Writer writer)
		throws UnsupportedRDFormatException
	{
		RDFWriterFactory factory = getRDFWriterRegistry().get(format);

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
		RDFFormat inputFormat = RDFFormat.forFileName(inputFile, RDFFormat.RDFXML);
		RDFParser rdfParser = createParser(inputFormat);

		// Create writer for output file
		String outputFile = args[1];
		FileOutputStream outStream = new FileOutputStream(outputFile);
		RDFFormat outputFormat = RDFFormat.forFileName(outputFile, RDFFormat.RDFXML);
		RDFWriter rdfWriter = createWriter(outputFormat, outStream);

		rdfParser.setRDFHandler(rdfWriter);
		rdfParser.parse(inStream, "file:" + inputFile);

		inStream.close();
		outStream.close();
	}
}
