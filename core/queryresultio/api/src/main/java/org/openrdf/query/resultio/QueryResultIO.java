/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Class offering utility methods related to query results.
 * 
 * @author Arjohn Kampman
 */
public class QueryResultIO {

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be parsed.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An RDFFormat object if a match was found, or <tt>null</tt>
	 *         otherwise.
	 * @see #getParserFormatForMIMEType(String, TupleQueryResultFormat)
	 */
	public static TupleQueryResultFormat getParserFormatForMIMEType(String mimeType) {
		return TupleQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(mimeType, null);
	}

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be parsed. This method calls
	 * {@link TupleQueryResultFormat#matchMIMEType(String, Iterable)} with the
	 * specified MIME type, the keys of
	 * {@link TupleQueryResultParserRegistry#getInstance()} and the fallback
	 * format as parameters.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching TupleQueryResultFormat, or <tt>fallback</tt> if no
	 *         match was found.
	 */
	public static TupleQueryResultFormat getParserFormatForMIMEType(String mimeType,
			TupleQueryResultFormat fallback)
	{
		return TupleQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(mimeType, fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         <tt>null</tt> otherwise.
	 * @see #getParserFormatForFileName(String, TupleQueryResultFormat)
	 */
	public static TupleQueryResultFormat getParserFormatForFileName(String fileName) {
		return TupleQueryResultParserRegistry.getInstance().getFileFormatForFileName(fileName);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed. This method calls
	 * {@link TupleQueryResultFormat#matchFileName(String, Iterable, info.aduna.lang.FileFormat)}
	 * with the specified MIME type, the keys of
	 * {@link TupleQueryResultParserRegistry#getInstance()} and the fallback
	 * format as parameters.
	 * 
	 * @param fileName
	 *        A file name.
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching TupleQueryResultFormat, or <tt>fallback</tt> if no
	 *         match was found.
	 */
	public static TupleQueryResultFormat getParserFormatForFileName(String fileName,
			TupleQueryResultFormat fallback)
	{
		return TupleQueryResultParserRegistry.getInstance().getFileFormatForFileName(fileName, fallback);
	}

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be written.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         <tt>null</tt> otherwise.
	 * @see #getWriterFormatForMIMEType(String, TupleQueryResultFormat)
	 */
	public static TupleQueryResultFormat getWriterFormatForMIMEType(String mimeType) {
		return TupleQueryResultWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
	}

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be written. This method calls
	 * {@link TupleQueryResultFormat#matchMIMEType(String, Iterable, info.aduna.lang.FileFormat)}
	 * with the specified MIME type, the keys of
	 * {@link TupleQueryResultWriterRegistry#getInstance()} and the fallback
	 * format as parameters.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching TupleQueryResultFormat, or <tt>fallback</tt> if no
	 *         match was found.
	 */
	public static TupleQueryResultFormat getWriterFormatForMIMEType(String mimeType,
			TupleQueryResultFormat fallback)
	{
		return TupleQueryResultWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType, fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         <tt>null</tt> otherwise.
	 * @see #getWriterFormatForFileName(String, TupleQueryResultFormat)
	 */
	public static TupleQueryResultFormat getWriterFormatForFileName(String fileName) {
		return TupleQueryResultWriterRegistry.getInstance().getFileFormatForFileName(fileName);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written. This method calls
	 * {@link TupleQueryResultFormat#matchFileName(String, Iterable, info.aduna.lang.FileFormat)}
	 * with the specified MIME type, the keys of
	 * {@link TupleQueryResultWriterRegistry#getInstance()} and the fallback
	 * format as parameters.
	 * 
	 * @param fileName
	 *        A file name.
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching TupleQueryResultFormat, or <tt>fallback</tt> if no
	 *         match was found.
	 */
	public static TupleQueryResultFormat getWriterFormatForFileName(String fileName,
			TupleQueryResultFormat fallback)
	{
		return TupleQueryResultWriterRegistry.getInstance().getFileFormatForFileName(fileName, fallback);
	}

	/**
	 * Convenience methods for creating TupleQueryResultParser objects. This
	 * method uses the registry returned by
	 * {@link TupleQueryResultParserRegistry#getInstance()} to get a factory for
	 * the specified format and uses this factory to create the appropriate
	 * parser.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified tuple query result
	 *         format.
	 */
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultParserFactory factory = TupleQueryResultParserRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getParser();
		}

		throw new UnsupportedQueryResultFormatException(
				"No parser factory available for tuple query result format " + format);
	}

	/**
	 * Convenience methods for creating TupleQueryResultParser objects that use
	 * the specified ValueFactory to create RDF model objects.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified tuple query result
	 *         format.
	 * @see #createParser(TupleQueryResultFormat)
	 * @see TupleQueryResultParser#setValueFactory(ValueFactory)
	 */
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format, ValueFactory valueFactory)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createParser(format);
		parser.setValueFactory(valueFactory);
		return parser;
	}

	/**
	 * Convenience methods for creating TupleQueryResultWriter objects. This
	 * method uses the registry returned by
	 * {@link TupleQueryResultWriterRegistry#getInstance()} to get a factory for
	 * the specified format and uses this factory to create the appropriate
	 * writer.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no writer is available for the specified tuple query result
	 *         format.
	 */
	public static TupleQueryResultWriter createWriter(TupleQueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultWriterFactory factory = TupleQueryResultWriterRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getWriter(out);
		}

		throw new UnsupportedQueryResultFormatException(
				"No writer factory available for tuple query result format " + format);
	}

	/**
	 * Convenience methods for creating BooleanQueryResultParser objects. This
	 * method uses the registry returned by
	 * {@link BooleanQueryResultParserRegistry#getInstance()} to get a factory
	 * for the specified format and uses this factory to create the appropriate
	 * parser.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified boolean query result
	 *         format.
	 */
	public static BooleanQueryResultParser createParser(BooleanQueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		BooleanQueryResultParserFactory factory = BooleanQueryResultParserRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getParser();
		}

		throw new UnsupportedQueryResultFormatException(
				"No parser factory available for boolean query result format " + format);
	}

	/**
	 * Convenience methods for creating BooleanQueryResultWriter objects. This
	 * method uses the registry returned by
	 * {@link BooleanQueryResultWriterRegistry#getInstance()} to get a factory
	 * for the specified format and uses this factory to create the appropriate
	 * writer.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no writer is available for the specified boolean query result
	 *         format.
	 */
	public static BooleanQueryResultWriter createWriter(BooleanQueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		BooleanQueryResultWriterFactory factory = BooleanQueryResultWriterRegistry.getInstance().get(format);

		if (factory != null) {
			return factory.getWriter(out);
		}

		throw new UnsupportedQueryResultFormatException(
				"No writer factory available for boolean query result format " + format);
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
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
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
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createParser(format);

		TupleQueryResultBuilder qrBuilder = new TupleQueryResultBuilder();
		parser.setTupleQueryResultHandler(qrBuilder);

		parser.parse(in);

		return qrBuilder.getQueryResult();
	}

	/**
	 * Parses a boolean query result document and returns the parsed value.
	 * 
	 * @param in
	 *        An InputStream to read the query result document from.
	 * @param format
	 *        The file format of the document to parse.
	 * @throws IOException
	 *         If an I/O error occured while reading the query result document
	 *         from the stream.
	 * @throws UnsupportedQueryResultFormatException
	 *         If an unsupported query result file format was specified.
	 */
	public static boolean parse(InputStream in, BooleanQueryResultFormat format)
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException
	{
		BooleanQueryResultParser parser = createParser(format);
		return parser.parse(in);
	}

	/**
	 * Writes a query result document in a specific query result format to an
	 * output stream.
	 * 
	 * @param tqr
	 *        The query result to write.
	 * @param format
	 *        The file format of the document to write.
	 * @param out
	 *        An OutputStream to write the document to.
	 * @throws IOException
	 *         If an I/O error occured while writing the query result document to
	 *         the stream.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result writer.
	 * @throws UnsupportedQueryResultFormatException
	 * @throws QueryEvaluationException
	 *         If an unsupported query result file format was specified.
	 */
	public static void write(TupleQueryResult tqr, TupleQueryResultFormat format, OutputStream out)
		throws IOException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException,
		QueryEvaluationException
	{
		TupleQueryResultWriter writer = createWriter(format, out);
		try {
			QueryResultUtil.report(tqr, writer);
		}
		catch (TupleQueryResultHandlerException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			else {
				throw e;
			}
		}
	}

	/**
	 * Writes a boolean query result document in a specific boolean query result
	 * format to an output stream.
	 * 
	 * @param value
	 *        The value to write.
	 * @param format
	 *        The file format of the document to write.
	 * @param out
	 *        An OutputStream to write the document to.
	 * @throws IOException
	 *         If an I/O error occured while writing the query result document to
	 *         the stream.
	 * @throws UnsupportedQueryResultFormatException
	 *         If an unsupported query result file format was specified.
	 */
	public static void write(boolean value, BooleanQueryResultFormat format, OutputStream out)
		throws IOException, UnsupportedQueryResultFormatException
	{
		BooleanQueryResultWriter writer = createWriter(format, out);
		writer.write(value);
	}

	/**
	 * Writes a graph query result document in a specific RDF format to an output
	 * stream.
	 * 
	 * @param gqr
	 *        The query result to write.
	 * @param format
	 *        The file format of the document to write.
	 * @param out
	 *        An OutputStream to write the document to.
	 * @throws IOException
	 *         If an I/O error occured while writing the query result document to
	 *         the stream.
	 * @throws RDFHandlerException
	 *         If such an exception is thrown by the used RDF writer.
	 * @throws QueryEvaluationException
	 * @throws UnsupportedRDFormatException
	 *         If an unsupported query result file format was specified.
	 */
	public static void write(GraphQueryResult gqr, RDFFormat format, OutputStream out)
		throws IOException, RDFHandlerException, UnsupportedRDFormatException, QueryEvaluationException
	{
		RDFWriter writer = Rio.createWriter(format, out);
		try {
			QueryResultUtil.report(gqr, writer);
		}
		catch (RDFHandlerException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			else {
				throw e;
			}
		}
	}
}
