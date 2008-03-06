/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.util.log.ThreadLog;
import org.openrdf.util.reflect.KeyedObjectFactory;
import org.openrdf.util.reflect.NoSuchTypeException;
import org.openrdf.util.reflect.TypeInstantiationException;

import org.openrdf.model.ValueFactory;

/**
 * Factory class providing static methods for creating RDF parsers and -writers
 * for various RDF file formats.
 */
public class Rio {

	/*-----------*
	 * Variables *
	 *-----------*/

	private static KeyedObjectFactory<RDFFormat, RDFParser> _parserFactory = new KeyedObjectFactory<RDFFormat, RDFParser>();

	private static KeyedObjectFactory<RDFFormat, RDFWriter> _writerFactory = new KeyedObjectFactory<RDFFormat, RDFWriter>();

	/*-------------*
	 * Initializer *
	 *-------------*/

	static {
		// TODO: initialize based on config file
		_registerParser(RDFFormat.RDFXML, "org.openrdf.rio.rdfxml.RDFXMLParser");
		_registerWriter(RDFFormat.RDFXML, "org.openrdf.rio.rdfxml.RDFXMLWriter");

		_registerParser(RDFFormat.NTRIPLES, "org.openrdf.rio.ntriples.NTriplesParser");
		_registerWriter(RDFFormat.NTRIPLES, "org.openrdf.rio.ntriples.NTriplesWriter");

		_registerParser(RDFFormat.TURTLE, "org.openrdf.rio.turtle.TurtleParser");
		_registerWriter(RDFFormat.TURTLE, "org.openrdf.rio.turtle.TurtleWriter");

		_registerWriter(RDFFormat.N3, "org.openrdf.rio.n3.N3Writer");

		_registerParser(RDFFormat.TRIX, "org.openrdf.rio.trix.TriXParser");
		_registerWriter(RDFFormat.TRIX, "org.openrdf.rio.trix.TriXWriter");
	}

	private static void _registerParser(RDFFormat rdfFormat, String className) {
		try {
			Class parserClass = Class.forName(className);
			_parserFactory.addType(rdfFormat, (Class<? extends RDFParser>)parserClass);
		}
		catch (ClassNotFoundException e) {
			ThreadLog.log("Unable to load RDF parser class: " + className, e);
		}
		catch (SecurityException e) {
			ThreadLog.warning("Not allowed to load RDF parser class: " + className, e);
		}
		catch (ClassCastException e) {
			ThreadLog.error("Parser class does not implement RDFParser interface: " + className, e);
		}
		catch (IllegalArgumentException e) {
			ThreadLog.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			ThreadLog.error("Unexpected error while trying to register RDF parser", e);
		}
	}

	private static void _registerWriter(RDFFormat rdfFormat, String className) {
		try {
			Class writerClass = Class.forName(className);
			_writerFactory.addType(rdfFormat, (Class<? extends RDFWriter>)writerClass);
		}
		catch (ClassNotFoundException e) {
			ThreadLog.log("Unable to load RDF writer class: " + className, e);
		}
		catch (SecurityException e) {
			ThreadLog.warning("Not allowed to load RDF writer class: " + className, e);
		}
		catch (ClassCastException e) {
			ThreadLog.error("Parser class does not implement RDFWriter interface: " + className, e);
		}
		catch (IllegalArgumentException e) {
			ThreadLog.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			ThreadLog.error("Unexpected error while trying to register RDF writer", e);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the factory that is used to create {@link RDFParser}s.
	 */
	public static KeyedObjectFactory<RDFFormat, RDFParser> getParserFactory() {
		return _parserFactory;
	}

	/**
	 * Gets the factory that is used to create {@link RDFWriter}s.
	 */
	public static KeyedObjectFactory<RDFFormat, RDFWriter> getWriterFactory() {
		return _writerFactory;
	}

	/**
	 * Convenience methods for creating RDFParser objects. This method calls
	 * <tt>getParserFactory().createInstance(format)</tt>.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 */
	public static RDFParser createParser(RDFFormat format)
		throws UnsupportedRDFormatException
	{
		try {
			return _parserFactory.createInstance(format);
		}
		catch (NoSuchTypeException e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (TypeInstantiationException e) {
			throw new UnsupportedRDFormatException(e);
		}
	}

	/**
	 * Convenience methods for creating RDFParser objects. This method calls
	 * <tt>getParserFactory().createInstance(format)</tt>.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 */
	public static RDFParser createParser(RDFFormat format, ValueFactory valueFactory)
		throws UnsupportedRDFormatException
	{
		RDFParser rdfParser = createParser(format);
		rdfParser.setValueFactory(valueFactory);
		return rdfParser;
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method calls
	 * <tt>getWriterFactory().createInstance(format)</tt>.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format)
		throws UnsupportedRDFormatException
	{
		try {
			return _writerFactory.createInstance(format);
		}
		catch (NoSuchTypeException e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (TypeInstantiationException e) {
			throw new UnsupportedRDFormatException(e);
		}
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method calls
	 * <tt>createWriter(format)</tt> and set the supplied OutputStream on the
	 * created RDF writer.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format, OutputStream out)
		throws UnsupportedRDFormatException
	{
		RDFWriter rdfWriter = createWriter(format);
		rdfWriter.setOutputStream(out);
		return rdfWriter;
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method calls
	 * <tt>createWriter(format)</tt> and set the supplied OutputStream on the
	 * created RDF writer.
	 * 
	 * @throws UnsupportedRDFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static RDFWriter createWriter(RDFFormat format, Writer writer)
		throws UnsupportedRDFormatException
	{
		RDFWriter rdfWriter = createWriter(format);
		rdfWriter.setWriter(writer);
		return rdfWriter;
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
