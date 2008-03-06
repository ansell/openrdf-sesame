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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.queryresult.impl.TupleQueryResultBuilder;
import org.openrdf.util.log.ThreadLog;
import org.openrdf.util.reflect.KeyedObjectFactory;
import org.openrdf.util.reflect.NoSuchTypeException;
import org.openrdf.util.reflect.TypeInstantiationException;

/**
 * Class offering utility methods related to query results.
 */
public class TupleQueryResultUtil {

	/*-----------*
	 * Variables *
	 *-----------*/

	private static KeyedObjectFactory<TupleQueryResultFormat, TupleQueryResultParser> _parserFactory = new KeyedObjectFactory<TupleQueryResultFormat, TupleQueryResultParser>();

	private static KeyedObjectFactory<TupleQueryResultFormat, TupleQueryResultWriter> _writerFactory = new KeyedObjectFactory<TupleQueryResultFormat, TupleQueryResultWriter>();

	/*-------------*
	 * Initializer *
	 *-------------*/

	static {
		// TODO: initialize based on config file
		_registerParser(TupleQueryResultFormat.BINARY, "org.openrdf.queryresult.binary.BinaryQueryResultParser");
		_registerWriter(TupleQueryResultFormat.BINARY, "org.openrdf.queryresult.binary.BinaryQueryResultWriter");

		// _registerParser(TupleQueryResultFormat.JSON,
		// "org.openrdf.queryresult.json.SPARQLResultsJSONParser");
		_registerWriter(TupleQueryResultFormat.JSON, "org.openrdf.queryresult.json.SPARQLResultsJSONWriter");

		_registerParser(TupleQueryResultFormat.SPARQL, "org.openrdf.queryresult.xml.SPARQLResultsXMLParser");
		_registerWriter(TupleQueryResultFormat.SPARQL, "org.openrdf.queryresult.xml.SPARQLResultsXMLWriter");
	}

	private static void _registerParser(TupleQueryResultFormat format, String className) {
		try {
			Class parserClass = Class.forName(className);
			_parserFactory.addType(format, (Class<? extends TupleQueryResultParser>)parserClass);
		}
		catch (ClassNotFoundException e) {
			ThreadLog.log("Unable to load query result parser class: " + className, e);
		}
		catch (SecurityException e) {
			ThreadLog.warning("Not allowed to load query result parser class: " + className, e);
		}
		catch (ClassCastException e) {
			ThreadLog.error("Parser class does not implement TupleQueryResultParser interface: " + className, e);
		}
		catch (IllegalArgumentException e) {
			ThreadLog.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			ThreadLog.error("Unexpected error while trying to register query result parser", e);
		}
	}

	private static void _registerWriter(TupleQueryResultFormat format, String className) {
		try {
			Class writerClass = Class.forName(className);
			_writerFactory.addType(format, (Class<? extends TupleQueryResultWriter>)writerClass);
		}
		catch (ClassNotFoundException e) {
			ThreadLog.log("Unable to load query result writer class: " + className, e);
		}
		catch (SecurityException e) {
			ThreadLog.warning("Not allowed to load query result writer class: " + className, e);
		}
		catch (ClassCastException e) {
			ThreadLog.error("Parser class does not implement TupleQueryResultWriter interface: " + className, e);
		}
		catch (IllegalArgumentException e) {
			ThreadLog.error(e.getMessage(), e);
		}
		catch (RuntimeException e) {
			ThreadLog.error("Unexpected error while trying to register query result writer", e);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the factory that is used to create {@link TupleQueryResultParser}s.
	 */
	public static KeyedObjectFactory<TupleQueryResultFormat, TupleQueryResultParser> getParserFactory() {
		return _parserFactory;
	}

	/**
	 * Gets the factory that is used to create {@link TupleQueryResultWriter}s.
	 */
	public static KeyedObjectFactory<TupleQueryResultFormat, TupleQueryResultWriter> getWriterFactory() {
		return _writerFactory;
	}

	/**
	 * Convenience methods for creating RDFParser objects. This method calls
	 * <tt>getParserFactory().createInstance(format)</tt>.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified RDF format.
	 */
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		try {
			return _parserFactory.createInstance(format);
		}
		catch (NoSuchTypeException e) {
			throw new UnsupportedQueryResultFormatException(e);
		}
		catch (TypeInstantiationException e) {
			throw new UnsupportedQueryResultFormatException(e);
		}
	}

	/**
	 * Convenience methods for creating RDFParser objects. This method calls
	 * <tt>getParserFactory().createInstance(format)</tt>.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified RDF format.
	 */
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format, ValueFactory valueFactory)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser rdfParser = createParser(format);
		rdfParser.setValueFactory(valueFactory);
		return rdfParser;
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method calls
	 * <tt>getWriterFactory().createInstance(format)</tt>.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static TupleQueryResultWriter createWriter(TupleQueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		try {
			return _writerFactory.createInstance(format);
		}
		catch (NoSuchTypeException e) {
			throw new UnsupportedQueryResultFormatException(e);
		}
		catch (TypeInstantiationException e) {
			throw new UnsupportedQueryResultFormatException(e);
		}
	}

	/**
	 * Convenience methods for creating RDFWriter objects. This method calls
	 * <tt>createWriter(format)</tt> and set the supplied OutputStream on the
	 * created RDF writer.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no writer is available for the specified RDF format.
	 */
	public static TupleQueryResultWriter createWriter(TupleQueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultWriter rdfWriter = createWriter(format);
		rdfWriter.setOutputStream(out);
		return rdfWriter;
	}

	/**
	 * Parses a query result document, reporting the parsed solutions to the
	 * supplied TupleQueryResultHandler.
	 * 
	 * @param in
	 *        An InputStream to read the query result document from.
	 * @param format
	 *        The query result format of the document to parse. Supported formats
	 *        are {@link TupleQueryResultFormat#SPARQL} and
	 *        {@link TupleQueryResultFormat#BINARY}.
	 * @param handler
	 *        The TupleQueryResultHandler to report the parse results to.
	 * @throws IOException
	 *         If an I/O error occured while reading the query result document
	 *         from the stream.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the supplied
	 *         TupleQueryResultHandler.
	 * @throws UnsupportedQueryResultFormatException
	 * @throws IllegalArgumentException
	 *         If an unsupported query result file format was specified.
	 */
	public static void parse(InputStream in, TupleQueryResultFormat format, TupleQueryResultHandler handler,
			ValueFactory valueFactory)
		throws IOException, TupleQueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createParser(format);
		parser.setValueFactory(valueFactory);
		parser.setTupleQueryResultHandler(handler);
		parser.parse(in);
	}

	/**
	 * Parses a query result document and returns it as a TupleQueryResult
	 * object.
	 * 
	 * @param in
	 *        An InputStream to read the query result document from.
	 * @param format
	 *        The query result format of the document to parse. Supported formats
	 *        are {@link TupleQueryResultFormat#SPARQL} and
	 *        {@link TupleQueryResultFormat#BINARY}.
	 * @throws IOException
	 *         If an I/O error occured while reading the query result document
	 *         from the stream.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result parser.
	 * @throws UnsupportedQueryResultFormatException
	 * @throws IllegalArgumentException
	 *         If an unsupported query result file format was specified.
	 */
	public static TupleQueryResult parse(InputStream in, TupleQueryResultFormat format)
		throws IOException, TupleQueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createParser(format);

		TupleQueryResultBuilder qrBuilder = new TupleQueryResultBuilder();
		parser.setTupleQueryResultHandler(qrBuilder);

		parser.parse(in);

		return qrBuilder.getQueryResult();
	}

	/**
	 * Writes a query result document in a specific query result format to an
	 * output stream.
	 * 
	 * @param qr
	 *        The query result to write.
	 * @param format
	 *        The query result file format of the document to parse. Supported
	 *        formats are {@link TupleQueryResultFormat#SPARQL},
	 *        {@link TupleQueryResultFormat#BINARY} and
	 *        {@link TupleQueryResultFormat#JSON}.
	 * @param out
	 *        An OutputStream to write the query result document to.
	 * @throws IOException
	 *         If an I/O error occured while writing the query result document to
	 *         the stream.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result writer.
	 * @throws UnsupportedQueryResultFormatException
	 * @throws IllegalArgumentException
	 *         If an unsupported query result file format was specified.
	 */
	public static void write(TupleQueryResult qr, TupleQueryResultFormat format, OutputStream out)
		throws IOException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException
	{
		TupleQueryResultWriter writer = createWriter(format, out);

		writer.startQueryResult(qr.getBindingNames(), qr.isDistinct(), qr.isOrdered());

		for (Solution solution : qr) {
			writer.handleSolution(solution);
		}

		writer.endQueryResult();
	}
}
