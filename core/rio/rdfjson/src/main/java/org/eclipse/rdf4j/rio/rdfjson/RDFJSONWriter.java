/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.rio.rdfjson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Lf2SpacesIndenter;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

/**
 * {@link RDFWriter} implementation for the RDF/JSON format
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public class RDFJSONWriter extends AbstractRDFWriter implements RDFWriter {

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
	 * @param contexts
	 *        The set of contexts that are relevant to this object, including
	 *        null contexts as they are found.
	 * @param jg
	 *        the {@link JsonGenerator} to write to.
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
				jg.writeObjectField(RDFJSONUtility.LANG, l.getLanguage().orElse(null));
			}
			else {
				jg.writeObjectField(RDFJSONUtility.DATATYPE, l.getDatatype().stringValue());
			}
		}
		else if (object instanceof BNode) {
			jg.writeObjectField(RDFJSONUtility.VALUE, resourceToString((BNode)object));

			jg.writeObjectField(RDFJSONUtility.TYPE, RDFJSONUtility.BNODE);
		}
		else if (object instanceof IRI) {
			jg.writeObjectField(RDFJSONUtility.VALUE, resourceToString((IRI)object));

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
		if (uriOrBnode instanceof IRI) {
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
			Lf2SpacesIndenter indenter = Lf2SpacesIndenter.instance.withLinefeed("\n");
			// By default Jackson does not pretty print, so enable this unless
			// PRETTY_PRINT setting is disabled
			DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withArrayIndenter(indenter).withObjectIndenter(
					indenter);
			jg.setPrettyPrinter(pp);
		}
		jg.writeStartObject();
		for (final Resource nextSubject : graph.subjects()) {
			jg.writeObjectFieldStart(RDFJSONWriter.resourceToString(nextSubject));
			for (final IRI nextPredicate : graph.filter(nextSubject, null, null).predicates()) {
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
