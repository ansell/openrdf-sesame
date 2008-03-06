/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Class offering utility methods related to query results.
 * 
 * @author Arjohn Kampman
 */
public class QueryResultUtil {

	private static TupleQueryResultParserRegistry parserRegistry = new TupleQueryResultParserRegistry();

	private static TupleQueryResultWriterRegistry writerRegistry = new TupleQueryResultWriterRegistry();

	/**
	 * Gets the registry for {@link TupleQueryResultParserFactory}s that is used
	 * to create tuple query result parsers.
	 */
	public static TupleQueryResultParserRegistry getTupleQueryResultParserRegistry() {
		return parserRegistry;
	}

	/**
	 * Gets the registry for {@link RDFWriterFactory}s that is used to create
	 * tuple query result writers.
	 */
	public static TupleQueryResultWriterRegistry getTupleQueryResultWriterRegistry() {
		return writerRegistry;
	}

	/**
	 * Convenience methods for creating TupleQueryResultParser objects. This
	 * method uses the registry returned by
	 * {@link #getTupleQueryResultParserRegistry()} to get a factory for the
	 * specified format and uses this factory to create the appropriate parser.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no parser is available for the specified tuple query result
	 *         format.
	 */
	public static TupleQueryResultParser createParser(TupleQueryResultFormat format)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultParserFactory factory = getTupleQueryResultParserRegistry().get(format);

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
	 * {@link #getTupleQueryResultWriterRegistry()} to get a factory for the
	 * specified format and uses this factory to create the appropriate writer.
	 * 
	 * @throws UnsupportedQueryResultFormatException
	 *         If no writer is available for the specified tuple query result
	 *         format.
	 */
	public static TupleQueryResultWriter createWriter(TupleQueryResultFormat format, OutputStream out)
		throws UnsupportedQueryResultFormatException
	{
		TupleQueryResultWriterFactory factory = getTupleQueryResultWriterRegistry().get(format);

		if (factory != null) {
			return factory.getWriter(out);
		}

		throw new UnsupportedQueryResultFormatException(
				"No writer factory available for tuple query result format " + format);
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
	 */
	public static void write(TupleQueryResult tqr, TupleQueryResultFormat format, OutputStream out)
		throws IOException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException,
		QueryEvaluationException
	{
		TupleQueryResultWriter writer = createWriter(format, out);
		try {
			report(tqr, writer);
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
	 * Reports a tuple query result to a {@link TupleQueryResultHandler}.
	 * 
	 * @param tqr
	 *        The query result to report.
	 * @param handler
	 *        The handler to report the query result to.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result writer.
	 */
	public static void report(TupleQueryResult tqr, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, QueryEvaluationException
	{
		handler.startQueryResult(tqr.getBindingNames(), tqr.isDistinct(), tqr.isOrdered());
		try {
			while (tqr.hasNext()) {
				BindingSet bindingSet = tqr.next();
				handler.handleSolution(bindingSet);
			}
		}
		finally {
			tqr.close();
		}
		handler.endQueryResult();
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
	 */
	public static void write(GraphQueryResult gqr, RDFFormat format, OutputStream out)
		throws IOException, RDFHandlerException, UnsupportedRDFormatException, QueryEvaluationException
	{
		RDFWriter writer = Rio.createWriter(format, out);
		try {
			report(gqr, writer);
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
	 */
	public static void report(GraphQueryResult gqr, RDFHandler rdfHandler)
		throws RDFHandlerException, QueryEvaluationException
	{
		try {
			rdfHandler.startRDF();

			for (Map.Entry<String, String> entry : gqr.getNamespaces().entrySet()) {
				String prefix = entry.getKey();
				String namespace = entry.getValue();
				rdfHandler.handleNamespace(prefix, namespace);
			}

			while (gqr.hasNext()) {
				Statement st = gqr.next();
				rdfHandler.handleStatement(st);
			}

			rdfHandler.endRDF();
		}
		finally {
			gqr.close();
		}
	}
}
