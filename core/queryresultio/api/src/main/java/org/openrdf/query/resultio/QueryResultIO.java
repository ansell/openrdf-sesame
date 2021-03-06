/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.resultio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Supplier;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
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
	 * @return An RDFFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getParserFormatForMIMEType(String, TupleQueryResultFormat)
	 */
	public static Optional<QueryResultFormat> getParserFormatForMIMEType(String mimeType) {
		return TupleQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(mimeType);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getParserFormatForFileName(String, TupleQueryResultFormat)
	 */
	public static Optional<QueryResultFormat> getParserFormatForFileName(String fileName) {
		return TupleQueryResultParserRegistry.getInstance().getFileFormatForFileName(fileName);
	}

	/**
	 * Tries to match a MIME type against the list of tuple query result formats
	 * that can be written.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getWriterFormatForMIMEType(String, TupleQueryResultFormat)
	 */
	public static Optional<QueryResultFormat> getWriterFormatForMIMEType(String mimeType) {
		return TupleQueryResultWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An TupleQueryResultFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getWriterFormatForFileName(String, TupleQueryResultFormat)
	 */
	public static Optional<QueryResultFormat> getWriterFormatForFileName(String fileName) {
		return TupleQueryResultWriterRegistry.getInstance().getFileFormatForFileName(fileName);
	}

	/**
	 * Tries to match a MIME type against the list of boolean query result
	 * formats that can be parsed.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An RDFFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getBooleanParserFormatForMIMEType(String, BooleanQueryResultFormat)
	 * @since 2.7.0
	 */
	public static Optional<QueryResultFormat> getBooleanParserFormatForMIMEType(String mimeType) {
		return BooleanQueryResultParserRegistry.getInstance().getFileFormatForMIMEType(mimeType);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be parsed.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An BooleanQueryResultFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getBooleanParserFormatForFileName(String, BooleanQueryResultFormat)
	 * @since 2.7.0
	 */
	public static Optional<QueryResultFormat> getBooleanParserFormatForFileName(String fileName) {
		return BooleanQueryResultParserRegistry.getInstance().getFileFormatForFileName(fileName);
	}

	/**
	 * Tries to match a MIME type against the list of boolean query result
	 * formats that can be written.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "application/sparql-results+xml".
	 * @return An BooleanQueryResultFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getBooleanWriterFormatForMIMEType(String, BooleanQueryResultFormat)
	 * @since 2.7.0
	 */
	public static Optional<QueryResultFormat> getBooleanWriterFormatForMIMEType(String mimeType) {
		return BooleanQueryResultWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
	}

	/**
	 * Tries to match the extension of a file name against the list of RDF
	 * formats that can be written.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return An BooleanQueryResultFormat object if a match was found, or
	 *         {@link Optional#empty()} otherwise.
	 * @see #getBooleanWriterFormatForFileName(String, BooleanQueryResultFormat)
	 * @since 2.7.0
	 */
	public static Optional<QueryResultFormat> getBooleanWriterFormatForFileName(String fileName) {
		return BooleanQueryResultWriterRegistry.getInstance().getFileFormatForFileName(fileName);
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
	public static TupleQueryResultParser createTupleParser(QueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultParserFactory factory = TupleQueryResultParserRegistry.getInstance().get(format).orElseThrow(
				() -> new UnsupportedQueryResultFormatException(
						"No parser factory available for tuple query result format " + format));

		return factory.getParser();
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
	public static TupleQueryResultParser createTupleParser(QueryResultFormat format, ValueFactory valueFactory)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createTupleParser(format);
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
	public static TupleQueryResultWriter createTupleWriter(QueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultWriterFactory factory = TupleQueryResultWriterRegistry.getInstance().get(format).orElseThrow(
				() -> new UnsupportedQueryResultFormatException(
						"No writer factory available for tuple query result format " + format));

		return factory.getWriter(out);
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
	public static BooleanQueryResultParser createBooleanParser(QueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		BooleanQueryResultParserFactory factory = BooleanQueryResultParserRegistry.getInstance().get(format).orElseThrow(
				() -> new UnsupportedQueryResultFormatException(
						"No parser factory available for boolean query result format " + format));

		return factory.getParser();
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
	public static BooleanQueryResultWriter createBooleanWriter(QueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		BooleanQueryResultWriterFactory factory = BooleanQueryResultWriterRegistry.getInstance().get(format).orElseThrow(
				() -> new UnsupportedQueryResultFormatException(
						"No writer factory available for boolean query result format " + format));

		return factory.getWriter(out);
	}

	/**
	 * Convenience methods for creating QueryResultWriter objects. This method
	 * uses the registry returned by
	 * {@link TupleQueryResultWriterRegistry#getInstance()} to get a factory for
	 * the specified format and uses this factory to create the appropriate
	 * writer.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no writer is available for the specified tuple query result
	 *         format.
	 * @since 2.7.0
	 */
	public static QueryResultWriter createWriter(QueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		Supplier<UnsupportedQueryResultFormatException> exception = () -> new UnsupportedQueryResultFormatException(
				"No writer factory available for query result format " + format);

		if (format instanceof TupleQueryResultFormat) {

			TupleQueryResultWriterFactory factory = TupleQueryResultWriterRegistry.getInstance().get(
					(TupleQueryResultFormat)format).orElseThrow(exception);

			return factory.getWriter(out);
		}
		else if (format instanceof BooleanQueryResultFormat) {
			BooleanQueryResultWriterFactory factory = BooleanQueryResultWriterRegistry.getInstance().get(
					(BooleanQueryResultFormat)format).orElseThrow(exception);

			return factory.getWriter(out);
		}

		throw exception.get();
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
	public static void parseTuple(InputStream in, QueryResultFormat format, TupleQueryResultHandler handler,
			ValueFactory valueFactory)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createTupleParser(format);
		parser.setValueFactory(valueFactory);
		parser.setQueryResultHandler(handler);
		try {
			parser.parseQueryResult(in);
		}
		catch (QueryResultHandlerException e) {
			if (e instanceof TupleQueryResultHandlerException) {
				throw (TupleQueryResultHandlerException)e;
			}
			else {
				throw new TupleQueryResultHandlerException(e);
			}
		}
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
	public static TupleQueryResult parseTuple(InputStream in, QueryResultFormat format)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException
	{
		TupleQueryResultParser parser = createTupleParser(format);

		TupleQueryResultBuilder qrBuilder = new TupleQueryResultBuilder();
		parser.setQueryResultHandler(qrBuilder);

		try {
			parser.parseQueryResult(in);
		}
		catch (QueryResultHandlerException e) {
			if (e instanceof TupleQueryResultHandlerException) {
				throw (TupleQueryResultHandlerException)e;
			}
			else {
				throw new TupleQueryResultHandlerException(e);
			}
		}

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
	public static boolean parseBoolean(InputStream in, QueryResultFormat format)
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException
	{
		BooleanQueryResultParser parser = createBooleanParser(format);
		try {

			QueryResultCollector handler = new QueryResultCollector();
			parser.setQueryResultHandler(handler);
			parser.parseQueryResult(in);

			if (handler.getHandledBoolean()) {
				return handler.getBoolean();
			}
			else {
				throw new QueryResultParseException("Did not find a boolean result");
			}
		}
		catch (QueryResultHandlerException e) {
			throw new QueryResultParseException(e);
		}
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
	public static void writeTuple(TupleQueryResult tqr, QueryResultFormat format, OutputStream out)
		throws IOException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException,
		QueryEvaluationException
	{
		TupleQueryResultWriter writer = createTupleWriter(format, out);
		try {
			writer.startDocument();
			writer.startHeader();
			QueryResults.report(tqr, writer);
		}
		catch (QueryResultHandlerException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			else if (e instanceof TupleQueryResultHandlerException) {
				throw (TupleQueryResultHandlerException)e;
			}
			else {
				throw new TupleQueryResultHandlerException(e);
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
	 * @since 2.7.0
	 */
	public static void writeBoolean(boolean value, QueryResultFormat format, OutputStream out)
		throws QueryResultHandlerException, UnsupportedQueryResultFormatException
	{
		BooleanQueryResultWriter writer = createBooleanWriter(format, out);
		writer.startDocument();
		writer.startHeader();
		writer.handleBoolean(value);
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
	public static void writeGraph(GraphQueryResult gqr, RDFFormat format, OutputStream out)
		throws IOException, RDFHandlerException, UnsupportedRDFormatException, QueryEvaluationException
	{
		RDFWriter writer = Rio.createWriter(format, out);
		try {
			QueryResults.report(gqr, writer);
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
