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
package org.openrdf.rio.rdfjson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.WriterConfig;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * A utility class to help converting Sesame Models to and from RDF/JSON using
 * Jackson.
 * 
 * @author Peter Ansell
 */
public class RDFJSONUtility {

	private static final String NULL = "null";

	private static final String GRAPHS = "graphs";

	private static final String URI = "uri";

	private static final String BNODE = "bnode";

	private static final String DATATYPE = "datatype";

	private static final String LITERAL = "literal";

	private static final String LANG = "lang";

	private static final String TYPE = "type";

	private static final String VALUE = "value";

	private static final JsonFactory JSON_FACTORY = new JsonFactory();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	static {
		// Disable features that may work for most JSON where the field names are
		// in limited supply,
		// but does not work for RDF/JSON where a wide range of URIs are used for
		// subjects and predicates
		JSON_FACTORY.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
		JSON_FACTORY.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
		JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

	/**
	 * Outputs a {@link Model} directly to JSON.
	 * 
	 * @param graph
	 *        A model containing all of the statements to be rendered to
	 *        RDF/JSON.
	 * @param writer
	 *        The output stream to use.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public static void modelToRdfJson(final Model graph, final OutputStream writer)
		throws JsonGenerationException, IOException
	{
		modelToRdfJson(graph, writer, new WriterConfig());
	}

	/**
	 * Outputs a {@link Model} directly to JSON.
	 * 
	 * @param graph
	 *        A model containing all of the statements to be rendered to
	 *        RDF/JSON.
	 * @param writer
	 *        The output stream to use.
	 * @param writerConfig
	 *        The {@link WriterConfig} to use for accessing specific settings.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public static void modelToRdfJson(final Model graph, final OutputStream writer,
			final WriterConfig writerConfig)
		throws JsonGenerationException, IOException
	{
		final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(writer);
		modelToRdfJsonInternal(graph, writerConfig, jg);

		jg.close();
	}

	/**
	 * Outputs a {@link Model} directly to JSON.
	 * 
	 * @param graph
	 *        A model containing all of the statements to be rendered to
	 *        RDF/JSON.
	 * @param writer
	 *        The output writer to use.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public static void modelToRdfJson(final Model graph, final Writer writer)
		throws JsonGenerationException, IOException
	{
		modelToRdfJson(graph, writer, new WriterConfig());
	}

	/**
	 * Outputs a {@link Model} directly to JSON.
	 * 
	 * @param graph
	 *        A model containing all of the statements to be rendered to
	 *        RDF/JSON.
	 * @param writer
	 *        The output writer to use.
	 * @param writerConfig
	 *        The {@link WriterConfig} to use for accessing specific settings.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public static void modelToRdfJson(final Model graph, final Writer writer, final WriterConfig writerConfig)
		throws JsonGenerationException, IOException
	{
		final JsonGenerator jg = JSON_FACTORY.createJsonGenerator(writer);
		modelToRdfJsonInternal(graph, writerConfig, jg);

		jg.close();
	}

	private static void modelToRdfJsonInternal(final Model graph, final WriterConfig writerConfig,
			final JsonGenerator jg)
		throws IOException, JsonGenerationException
	{
		if (writerConfig.get(BasicWriterSettings.PRETTY_PRINT)) {
			// By default Jackson does not pretty print, so enable this unless
			// PRETTY_PRINT setting is disabled
			jg.useDefaultPrettyPrinter();
		}
		jg.writeStartObject();
		for (final Resource nextSubject : graph.subjects()) {
			jg.writeObjectFieldStart(resourceToString(nextSubject));
			for (final URI nextPredicate : graph.filter(nextSubject, null, null).predicates()) {
				jg.writeArrayFieldStart(nextPredicate.stringValue());
				for (final Value nextObject : graph.filter(nextSubject, nextPredicate, null).objects()) {
					// contexts are optional, so this may return empty in some
					// scenarios depending on the interpretation of the way contexts
					// work
					final Set<Resource> contexts = graph.filter(nextSubject, nextPredicate, nextObject).contexts();

					writeObject(nextObject, contexts, jg);
				}
				jg.writeEndArray();
			}
			jg.writeEndObject();
		}
		jg.writeEndObject();
	}

	/**
	 * RDF/JSON Parser implementation using the Jackson API.
	 * 
	 * @param json
	 *        The RDF/JSON {@link InputStream} to be parsed.
	 * @param handler
	 *        The {@link RDFHandler} to handle the resulting triples.
	 * @throws RDFHandlerException
	 */
	public static void rdfJsonToHandler(final InputStream json, final RDFHandler handler, final ValueFactory vf)
		throws RDFParseException, RDFHandlerException
	{
		JsonParser jp = null;

		try {
			jp = JSON_FACTORY.createJsonParser(json);
			rdfJsonToHandlerInternal(handler, vf, jp);
		}
		catch (final IOException e) {
			if (jp != null) {
				throw new RDFParseException(e, jp.getCurrentLocation().getLineNr(),
						jp.getCurrentLocation().getColumnNr());
			}
			else {
				throw new RDFParseException(e);
			}
		}
		finally {
			if (jp != null) {
				try {
					jp.close();
				}
				catch (final IOException e) {
					throw new RDFParseException("Found exception while closing JSON parser", e,
							jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
				}
			}
		}
	}

	/**
	 * RDF/JSON Parser implementation using the Jackson API.
	 * 
	 * @param json
	 *        The RDF/JSON {@link Reader} to be parsed.
	 * @param handler
	 *        The {@link RDFHandler} to handle the resulting triples.
	 * @throws RDFHandlerException
	 */
	public static void rdfJsonToHandler(final Reader json, final RDFHandler handler, final ValueFactory vf)
		throws RDFParseException, RDFHandlerException
	{
		JsonParser jp = null;

		try {
			jp = JSON_FACTORY.createJsonParser(json);
			rdfJsonToHandlerInternal(handler, vf, jp);
		}
		catch (final IOException e) {
			if (jp != null) {
				throw new RDFParseException(e, jp.getCurrentLocation().getLineNr(),
						jp.getCurrentLocation().getColumnNr());
			}
			else {
				throw new RDFParseException(e);
			}
		}
		finally {
			if (jp != null) {
				try {
					jp.close();
				}
				catch (final IOException e) {
					throw new RDFParseException("Found exception while closing JSON parser", e,
							jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
				}
			}
		}
	}

	private static void rdfJsonToHandlerInternal(final RDFHandler handler, final ValueFactory vf,
			final JsonParser jp)
		throws IOException, JsonParseException, RDFParseException, RDFHandlerException
	{
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new RDFParseException("Expected RDF/JSON document to start with an Object",
					jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
		}

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			final String subjStr = jp.getCurrentName();
			Resource subject = null;

			subject = subjStr.startsWith("_:") ? vf.createBNode(subjStr.substring(2)) : vf.createURI(subjStr);
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new RDFParseException("Expected subject value to start with an Object",
						jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				final String predStr = jp.getCurrentName();

				final URI predicate = vf.createURI(predStr);
				if (jp.nextToken() != JsonToken.START_ARRAY) {
					throw new RDFParseException("Expected predicate value to start with an array",
							jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
				}

				while (jp.nextToken() != JsonToken.END_ARRAY) {
					if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
						throw new RDFParseException("Expected object value to start with an Object: subject=<"
								+ subjStr + "> predicate=<" + predStr + ">", jp.getCurrentLocation().getLineNr(),
								jp.getCurrentLocation().getColumnNr());
					}

					String nextValue = null;
					String nextType = null;
					String nextDatatype = null;
					String nextLanguage = null;
					final Set<String> nextContexts = new HashSet<String>(2);

					while (jp.nextToken() != JsonToken.END_OBJECT) {
						final String fieldName = jp.getCurrentName();
						if (VALUE.equals(fieldName)) {
							if (nextValue != null) {
								throw new RDFParseException("Multiple values found for a single object: subject="
										+ subjStr + " predicate=" + predStr, jp.getCurrentLocation().getLineNr(),
										jp.getCurrentLocation().getColumnNr());
							}

							jp.nextToken();

							nextValue = jp.getText();
						}
						else if (TYPE.equals(fieldName)) {
							if (nextType != null) {
								throw new RDFParseException("Multiple types found for single object: subject="
										+ subjStr + " predicate=" + predStr, jp.getCurrentLocation().getLineNr(),
										jp.getCurrentLocation().getColumnNr());
							}

							jp.nextToken();

							nextType = jp.getText();
						}
						else if (LANG.equals(fieldName)) {
							if (nextLanguage != null) {
								throw new RDFParseException("Multiple languages found for single object: subject="
										+ subjStr + " predicate=" + predStr, jp.getCurrentLocation().getLineNr(),
										jp.getCurrentLocation().getColumnNr());
							}

							jp.nextToken();

							nextLanguage = jp.getText();
						}
						else if (DATATYPE.equals(fieldName)) {
							if (nextDatatype != null) {
								throw new RDFParseException("Multiple datatypes found for single object: subject="
										+ subjStr + " predicate=" + predStr, jp.getCurrentLocation().getLineNr(),
										jp.getCurrentLocation().getColumnNr());
							}

							jp.nextToken();

							nextDatatype = jp.getText();
						}
						else if (GRAPHS.equals(fieldName)) {
							if (jp.nextToken() != JsonToken.START_ARRAY) {
								throw new RDFParseException("Expected graphs to start with an array",
										jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
							}

							while (jp.nextToken() != JsonToken.END_ARRAY) {
								final String nextGraph = jp.getText();
								nextContexts.add(nextGraph);
							}
						}
						else {
							throw new RDFParseException("Unrecognised JSON field name for object: subject="
									+ subjStr + " predicate=" + predStr + " fieldname=" + fieldName,
									jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
						}
					}

					Value object = null;

					if (nextType == null) {
						throw new RDFParseException("No type for object: subject=" + subjStr + " predicate="
								+ predStr, jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
					}

					if (nextValue == null) {
						throw new RDFParseException("No value for object: subject=" + subjStr + " predicate="
								+ predStr, jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
					}

					if (LITERAL.equals(nextType)) {
						if (nextLanguage != null) {
							object = vf.createLiteral(nextValue, nextLanguage);
						}
						else if (nextDatatype != null) {
							object = vf.createLiteral(nextValue, vf.createURI(nextDatatype));
						}
						else {
							object = vf.createLiteral(nextValue);
						}
					}
					else if (BNODE.equals(nextType)) {
						object = vf.createBNode(nextValue.substring(2));
					}
					else if (URI.equals(nextType)) {
						object = vf.createURI(nextValue);
					}

					if (!nextContexts.isEmpty()) {
						for (final String nextContext : nextContexts) {
							final Resource context = nextContext.equals(NULL) ? null : vf.createURI(nextContext);
							handler.handleStatement(vf.createStatement(subject, predicate, object, context));
						}
					}
					else {
						handler.handleStatement(vf.createStatement(subject, predicate, object));
					}
				}
			}
		}
	}

	/**
	 * Returns the correct syntax for a Resource, depending on whether it is a
	 * URI or a Blank Node (ie, BNode)
	 * 
	 * @param uriOrBnode
	 *        The resource to serialise to a string
	 * @return The string value of the sesame resource
	 */
	private static String resourceToString(final Resource uriOrBnode) {
		if (uriOrBnode instanceof URI) {
			return uriOrBnode.stringValue();
		}
		else {
			return "_:" + ((BNode)uriOrBnode).getID();
		}
	}

	/**
	 * Helper method to reduce complexity of the JSON serialisation algorithm Any
	 * null contexts will only be serialised to JSON if there are also non-null
	 * contexts in the contexts array
	 * 
	 * @param object
	 *        The RDF value to serialise
	 * @param valueArray
	 *        The JSON Array to serialise the object to
	 * @param contexts
	 *        The set of contexts that are relevant to this object, including
	 *        null contexts as they are found.
	 * @throws IOException
	 * @throws JsonGenerationException
	 * @throws JSONException
	 */
	private static void writeObject(final Value object, final Set<Resource> contexts, final JsonGenerator jg)
		throws JsonGenerationException, IOException
	{
		jg.writeStartObject();
		if (object instanceof Literal) {
			jg.writeObjectField(VALUE, object.stringValue());

			jg.writeObjectField(TYPE, LITERAL);
			final Literal l = (Literal)object;

			if (l.getLanguage() != null) {
				jg.writeObjectField(LANG, l.getLanguage());
			}

			if (l.getDatatype() != null) {
				jg.writeObjectField(DATATYPE, l.getDatatype().stringValue());
			}
		}
		else if (object instanceof BNode) {
			jg.writeObjectField(VALUE, resourceToString((BNode)object));

			jg.writeObjectField(TYPE, BNODE);
		}
		else if (object instanceof URI) {
			jg.writeObjectField(VALUE, resourceToString((URI)object));

			jg.writeObjectField(TYPE, URI);
		}

		if (contexts != null && !contexts.isEmpty()
				&& !(contexts.size() == 1 && contexts.iterator().next() == null))
		{
			jg.writeArrayFieldStart(GRAPHS);
			for (final Resource nextContext : contexts) {
				if (nextContext == null) {
					jg.writeNull();
				}
				else {
					jg.writeString(nextContext.stringValue());
				}
			}
			jg.writeEndArray();
		}

		jg.writeEndObject();
	}

}