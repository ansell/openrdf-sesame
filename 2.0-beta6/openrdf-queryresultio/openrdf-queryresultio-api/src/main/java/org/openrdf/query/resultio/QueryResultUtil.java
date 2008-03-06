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
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Class offering utility methods related to query results.
 * 
 * @deprecated The utility methods of this class have been seperated and can now
 *             be found in two new utility classes; I/O-related utility methods
 *             have been moved to {@link QueryResultIO}, all other
 *             (non-I/O-related) methods have been moved to the new
 *             {@link org.openrdf.query.QueryResultUtil} class in the more
 *             general <tt>org.openrdf.query</tt> package.
 * @author Arjohn Kampman
 */
@Deprecated
public class QueryResultUtil
{

	/**
	 * Gets the registry for {@link TupleQueryResultParserFactory}s that is used
	 * to create tuple query result parsers.
	 * 
	 * @deprecated Use {@link TupleQueryResultParserRegistry#getInstance()}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultParserRegistry getTupleQueryResultParserRegistry()
	{
		return TupleQueryResultParserRegistry.getInstance();
	}

	/**
	 * Gets the registry for {@link RDFWriterFactory}s that is used to create
	 * tuple query result writers.
	 * 
	 * @deprecated Use {@link TupleQueryResultWriterRegistry#getInstance()}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultWriterRegistry getTupleQueryResultWriterRegistry()
	{
		return TupleQueryResultWriterRegistry.getInstance();
	}

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be parsed.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An RDFFormat object if a match was found, or <tt>null</tt>
	 *         otherwise.
	 * @deprecated Use {@link QueryResultIO#getParserFormatForMIMEType(String)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getParserFormatForMIMEType(String mimeType)
	{
		return QueryResultIO.getParserFormatForMIMEType(mimeType);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#getParserFormatForMIMEType(String, TupleQueryResultFormat)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getParserFormatForMIMEType(String mimeType,
			TupleQueryResultFormat fallback)
	{
		return QueryResultIO.getParserFormatForMIMEType(mimeType, fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         <tt>null</tt> otherwise.
	 * @deprecated Use {@link QueryResultIO#getParserFormatForFileName(String)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getParserFormatForFileName(String fileName)
	{
		return QueryResultIO.getParserFormatForFileName(fileName);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#getParserFormatForFileName(String, TupleQueryResultFormat)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getParserFormatForFileName(String fileName,
			TupleQueryResultFormat fallback)
	{
		return QueryResultIO.getParserFormatForFileName(fileName, fallback);
	}

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be written.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         <tt>null</tt> otherwise.
	 * @deprecated Use {@link QueryResultIO#getWriterFormatForMIMEType(String)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getWriterFormatForMIMEType(String mimeType)
	{
		return QueryResultIO.getWriterFormatForMIMEType(mimeType);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#getWriterFormatForMIMEType(String, TupleQueryResultFormat)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getWriterFormatForMIMEType(String mimeType,
			TupleQueryResultFormat fallback)
	{
		return QueryResultIO.getWriterFormatForMIMEType(mimeType, fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         <tt>null</tt> otherwise.
	 * @deprecated Use {@link QueryResultIO#getWriterFormatForFileName(String)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getWriterFormatForFileName(String fileName)
	{
		return QueryResultIO.getWriterFormatForFileName(fileName);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#getWriterFormatForFileName(String, TupleQueryResultFormat)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultFormat getWriterFormatForFileName(String fileName,
			TupleQueryResultFormat fallback)
	{
		return QueryResultIO.getWriterFormatForFileName(fileName, fallback);
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
	 * @deprecated Use {@link QueryResultIO#createParser(TupleQueryResultFormat)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		return QueryResultIO.createParser(format);
	}

	/**
	 * Convenience methods for creating TupleQueryResultParser objects that use
	 * the specified ValueFactory to create RDF model objects.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified tuple query result
	 *         format.
	 * @deprecated Use
	 *             {@link QueryResultIO#createParser(TupleQueryResultFormat, ValueFactory)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format, ValueFactory valueFactory)
		throws UnsupportedQueryResultFormatException
	{
		return QueryResultIO.createParser(format, valueFactory);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#createWriter(TupleQueryResultFormat, OutputStream)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResultWriter createWriter(TupleQueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		return QueryResultIO.createWriter(format, out);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#parse(InputStream, TupleQueryResultFormat, TupleQueryResultHandler, ValueFactory)}
	 *             instead.
	 */
	@Deprecated
	public static void parse(InputStream in, TupleQueryResultFormat format, TupleQueryResultHandler handler,
			ValueFactory valueFactory)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		QueryResultIO.parse(in, format, handler, valueFactory);
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
	 * @deprecated Use
	 *             {@link QueryResultIO#parse(InputStream, TupleQueryResultFormat)}
	 *             instead.
	 */
	@Deprecated
	public static TupleQueryResult parse(InputStream in, TupleQueryResultFormat format)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		return QueryResultIO.parse(in, format);
	}

	/**
	 * Writes a query result document in a specific query result format to an
	 * output stream.
	 * 
	 * @param tqr
	 *        The query result to write.
	 * @param format
	 *        The query result file format of the document to write.
	 * @param out
	 *        An OutputStream to write the query result document to.
	 * @throws IOException
	 *         If an I/O error occured while writing the query result document to
	 *         the stream.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result writer.
	 * @throws UnsupportedQueryResultFormatException
	 * @throws QueryEvaluationException
	 * @throws IllegalArgumentException
	 *         If an unsupported query result file format was specified.
	 * @deprecated Use
	 *             {@link QueryResultIO#write(TupleQueryResult, TupleQueryResultFormat, OutputStream)}
	 *             instead.
	 */
	@Deprecated
	public static void write(TupleQueryResult tqr, TupleQueryResultFormat format, OutputStream out)
		throws IOException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException,
		QueryEvaluationException
	{
		QueryResultIO.write(tqr, format, out);
	}

	/**
	 * Reports a tuple query result to a {@link TupleQueryResultHandler}.
	 * 
	 * @param tqr
	 *        The query result to report.
	 * @param handler
	 *        The handler to report the query result to.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result writer.
	 * @deprecated Use
	 *             {@link org.openrdf.query.QueryResultUtil#report(TupleQueryResult, TupleQueryResultHandler)}
	 *             instead.
	 */
	@Deprecated
	public static void report(TupleQueryResult tqr, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, QueryEvaluationException
	{
		org.openrdf.query.QueryResultUtil.report(tqr, handler);
	}

	/**
	 * Writes a graph query result document in a specific RDF format to an output
	 * stream.
	 * 
	 * @param gqr
	 *        The query result to write.
	 * @param format
	 *        The RDF file format of the document to write.
	 * @param out
	 *        An OutputStream to write the query result document to.
	 * @throws IOException
	 *         If an I/O error occured while writing the query result document to
	 *         the stream.
	 * @throws RDFHandlerException
	 *         If such an exception is thrown by the used RDF writer.
	 * @throws UnsupportedRDFormatException
	 * @throws QueryEvaluationException
	 * @throws IllegalArgumentException
	 *         If an unsupported query result file format was specified.
	 * @deprecated Use
	 *             {@link QueryResultIO#write(GraphQueryResult, RDFFormat, OutputStream)}
	 *             instead.
	 */
	@Deprecated
	public static void write(GraphQueryResult gqr, RDFFormat format, OutputStream out)
		throws IOException, RDFHandlerException, UnsupportedRDFormatException, QueryEvaluationException
	{
		QueryResultIO.write(gqr, format, out);
	}

	/**
	 * Reports a graph query result to an {@link RDFHandler}.
	 * 
	 * @param gqr
	 *        The query result to report.
	 * @param rdfHandler
	 *        The handler to report the query result to.
	 * @throws RDFHandlerException
	 *         If such an exception is thrown by the used RDF writer.
	 * @throws QueryEvaluationException
	 * @deprecated Use
	 *             {@link org.openrdf.query.QueryResultUtil#report(GraphQueryResult, RDFHandler)}
	 *             instead.
	 */
	@Deprecated
	public static void report(GraphQueryResult gqr, RDFHandler rdfHandler)
		throws RDFHandlerException, QueryEvaluationException
	{
		org.openrdf.query.QueryResultUtil.report(gqr, rdfHandler);
	}
}
