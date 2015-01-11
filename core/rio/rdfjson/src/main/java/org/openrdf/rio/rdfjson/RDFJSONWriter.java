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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.util.Literals;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.RDFWriterBase;

/**
 * {@link RDFWriter} implementation for the RDF/JSON format
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public class RDFJSONWriter extends RDFWriterBase implements RDFWriter {

	private Writer writer;

	private OutputStream outputStream;

	private Model graph;

	private final RDFFormat actualFormat;

	public RDFJSONWriter(final OutputStream out, final RDFFormat actualFormat) {
		this.outputStream = out;
		this.actualFormat = actualFormat;
	}

	public RDFJSONWriter(final Writer writer, final RDFFormat actualFormat) {
		this.writer = writer;
		this.actualFormat = actualFormat;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		try {
			if (this.writer != null) {
				final JsonGenerator jg = RDFJSONUtility.JSON_FACTORY.createJsonGenerator(this.writer);
				RDFJSONWriter.modelToRdfJsonInternal(this.graph, this.getWriterConfig(), jg);

				jg.close();
				this.writer.flush();
			}
			else if (this.outputStream != null) {
				final JsonGenerator jg = RDFJSONUtility.JSON_FACTORY.createJsonGenerator(this.outputStream);
				RDFJSONWriter.modelToRdfJsonInternal(this.graph, this.getWriterConfig(), jg);

				jg.close();
				this.outputStream.flush();
			}
			else {
				throw new IllegalStateException("The output stream and the writer were both null.");
			}
		}
		catch (final IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public RDFFormat getRDFFormat() {
		return this.actualFormat;
	}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		final Set<RioSetting<?>> results = new HashSet<RioSetting<?>>(super.getSupportedSettings());

		results.add(BasicWriterSettings.PRETTY_PRINT);

		return results;
	}

	@Override
	public void handleComment(final String comment)
		throws RDFHandlerException
	{
		// Comments are ignored.
	}

	@Override
	public void handleNamespace(final String prefix, final String uri)
		throws RDFHandlerException
	{
		// Namespace prefixes are not used in RDF/JSON.
	}

	@Override
	public void handleStatement(final Statement statement)
		throws RDFHandlerException
	{
		this.graph.add(statement);
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		this.graph = new TreeModel();
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
	public static void writeObject(final Value object, final Set<Resource> contexts, final JsonGenerator jg)
		throws JsonGenerationException, IOException
	{
		jg.writeStartObject();
		if (object instanceof Literal) {
			jg.writeObjectField(RDFJSONUtility.VALUE, object.stringValue());

			jg.writeObjectField(RDFJSONUtility.TYPE, RDFJSONUtility.LITERAL);
			final Literal l = (Literal)object;

			if (Literals.isLanguageLiteral(l)) {
				jg.writeObjectField(RDFJSONUtility.LANG, l.getLanguage());
			}
			else {
				jg.writeObjectField(RDFJSONUtility.DATATYPE, l.getDatatype().stringValue());
			}
		}
		else if (object instanceof BNode) {
			jg.writeObjectField(RDFJSONUtility.VALUE, resourceToString((BNode)object));

			jg.writeObjectField(RDFJSONUtility.TYPE, RDFJSONUtility.BNODE);
		}
		else if (object instanceof URI) {
			jg.writeObjectField(RDFJSONUtility.VALUE, resourceToString((URI)object));

			jg.writeObjectField(RDFJSONUtility.TYPE, RDFJSONUtility.URI);
		}

		if (contexts != null && !contexts.isEmpty()
				&& !(contexts.size() == 1 && contexts.iterator().next() == null))
		{
			jg.writeArrayFieldStart(RDFJSONUtility.GRAPHS);
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

	/**
	 * Returns the correct syntax for a Resource, depending on whether it is a
	 * URI or a Blank Node (ie, BNode)
	 * 
	 * @param uriOrBnode
	 *        The resource to serialise to a string
	 * @return The string value of the sesame resource
	 */
	public static String resourceToString(final Resource uriOrBnode) {
		if (uriOrBnode instanceof URI) {
			return uriOrBnode.stringValue();
		}
		else {
			return "_:" + ((BNode)uriOrBnode).getID();
		}
	}

	public static void modelToRdfJsonInternal(final Model graph, final WriterConfig writerConfig,
			final JsonGenerator jg)
		throws IOException, JsonGenerationException
	{
		if (writerConfig.get(BasicWriterSettings.PRETTY_PRINT)) {
			// SES-2011: Always use \n for consistency
			DefaultIndenter indenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.withLinefeed("\n");
			// By default Jackson does not pretty print, so enable this unless
			// PRETTY_PRINT setting is disabled
			DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withArrayIndenter(indenter).withObjectIndenter(
					indenter);
			jg.setPrettyPrinter(pp);
		}
		jg.writeStartObject();
		for (final Resource nextSubject : graph.subjects()) {
			jg.writeObjectFieldStart(RDFJSONWriter.resourceToString(nextSubject));
			for (final URI nextPredicate : graph.filter(nextSubject, null, null).predicates()) {
				jg.writeArrayFieldStart(nextPredicate.stringValue());
				for (final Value nextObject : graph.filter(nextSubject, nextPredicate, null).objects()) {
					// contexts are optional, so this may return empty in some
					// scenarios depending on the interpretation of the way contexts
					// work
					final Set<Resource> contexts = graph.filter(nextSubject, nextPredicate, nextObject).contexts();

					RDFJSONWriter.writeObject(nextObject, contexts, jg);
				}
				jg.writeEndArray();
			}
			jg.writeEndObject();
		}
		jg.writeEndObject();
	}

}
