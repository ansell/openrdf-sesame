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
package org.eclipse.rdf4j.query.resultio.sparqljson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Lf2SpacesIndenter;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.AbstractQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.BasicQueryWriterSettings;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

/**
 * An abstract class to implement the base functionality for both
 * SPARQLBooleanJSONWriter and SPARQLResultsJSONWriter.
 * 
 * @author Peter Ansell
 */
abstract class AbstractSPARQLJSONWriter extends AbstractQueryResultWriter implements QueryResultWriter {

	private static final JsonFactory JSON_FACTORY = new JsonFactory();

	static {
		// Disable features that may work for most JSON where the field names are
		// in limited supply,
		// but does not work for RDF/JSON where a wide range of URIs are used for
		// subjects and
		// predicates
		JSON_FACTORY.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
		JSON_FACTORY.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
		JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	protected boolean firstTupleWritten = false;

	protected boolean documentOpen = false;

	protected boolean headerOpen = false;

	protected boolean headerComplete = false;

	protected boolean tupleVariablesFound = false;

	protected boolean linksFound = false;

	private final JsonGenerator jg;

	public AbstractSPARQLJSONWriter(OutputStream out) {
		try {
			jg = JSON_FACTORY.createJsonGenerator(new OutputStreamWriter(out, Charset.forName("UTF-8")));
		}
		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void endHeader()
		throws QueryResultHandlerException
	{
		if (!headerComplete) {
			try {
				jg.writeEndObject();

				if (tupleVariablesFound) {
					// Write results
					jg.writeObjectFieldStart("results");

					jg.writeArrayFieldStart("bindings");
				}

				headerComplete = true;
			}
			catch (IOException e) {
				throw new QueryResultHandlerException(e);
			}
		}
	}

	@Override
	public void startQueryResult(List<String> columnHeaders)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}

			if (!headerOpen) {
				startHeader();
			}

			tupleVariablesFound = true;
			jg.writeArrayFieldStart("vars");
			for (String nextColumn : columnHeaders) {
				jg.writeString(nextColumn);
			}
			jg.writeEndArray();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}

			if (!headerOpen) {
				startHeader();
			}

			if (!headerComplete) {
				endHeader();
			}

			if (!tupleVariablesFound) {
				throw new IllegalStateException("Must call startQueryResult before handleSolution");
			}

			firstTupleWritten = true;

			jg.writeStartObject();

			Iterator<Binding> bindingIter = bindingSet.iterator();
			while (bindingIter.hasNext()) {
				Binding binding = bindingIter.next();
				jg.writeFieldName(binding.getName());
				writeValue(binding.getValue());
			}

			jg.writeEndObject();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}

			if (!headerOpen) {
				startHeader();
			}

			if (!headerComplete) {
				endHeader();
			}

			if (!tupleVariablesFound) {
				throw new IllegalStateException(
						"Could not end query result as startQueryResult was not called first.");
			}

			// bindings array
			jg.writeEndArray();
			// results braces
			jg.writeEndObject();
			endDocument();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void startDocument()
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			documentOpen = true;
			headerOpen = false;
			headerComplete = false;
			tupleVariablesFound = false;
			firstTupleWritten = false;
			linksFound = false;

			if (getWriterConfig().get(BasicWriterSettings.PRETTY_PRINT)) {
				// SES-2011: Always use \n for consistency
				Lf2SpacesIndenter indenter = Lf2SpacesIndenter.instance.withLinefeed("\n");
				// By default Jackson does not pretty print, so enable this unless
				// PRETTY_PRINT setting is disabled
				DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withArrayIndenter(indenter).withObjectIndenter(
						indenter);
				jg.setPrettyPrinter(pp);
			}

			try {
				if (getWriterConfig().isSet(BasicQueryWriterSettings.JSONP_CALLBACK)) {
					// SES-1019 : Write the callbackfunction name as a wrapper for
					// the results here
					String callbackName = getWriterConfig().get(BasicQueryWriterSettings.JSONP_CALLBACK);
					jg.writeRaw(callbackName);
					jg.writeRaw("(");
				}
				jg.writeStartObject();
			}
			catch (IOException e) {
				throw new QueryResultHandlerException(e);
			}
		}
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws QueryResultHandlerException
	{
		// Ignore, as JSON does not support stylesheets
	}

	@Override
	public void startHeader()
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		if (!headerOpen) {
			try {
				// Write header
				jg.writeObjectFieldStart("head");

				headerOpen = true;
			}
			catch (IOException e) {
				throw new QueryResultHandlerException(e);
			}
		}
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}

			if (!headerOpen) {
				startHeader();
			}

			jg.writeArrayFieldStart("link");
			for (String nextLink : linkUrls) {
				jg.writeString(nextLink);
			}
			jg.writeEndArray();
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	protected void writeValue(Value value)
		throws IOException, QueryResultHandlerException
	{
		jg.writeStartObject();

		if (value instanceof IRI) {
			jg.writeStringField("type", "uri");
			jg.writeStringField("value", ((IRI)value).toString());
		}
		else if (value instanceof BNode) {
			jg.writeStringField("type", "bnode");
			jg.writeStringField("value", ((BNode)value).getID());
		}
		else if (value instanceof Literal) {
			Literal lit = (Literal)value;

			if (Literals.isLanguageLiteral(lit)) {
				jg.writeObjectField("xml:lang", lit.getLanguage().orElse(null));
			}
			else {
				IRI datatype = lit.getDatatype();
				boolean ignoreDatatype = datatype.equals(XMLSchema.STRING) && xsdStringToPlainLiteral();
				if (!ignoreDatatype) {
					jg.writeObjectField("datatype", lit.getDatatype().stringValue());
				}
			}

			jg.writeObjectField("type", "literal");

			jg.writeObjectField("value", lit.getLabel());
		}
		else {
			throw new TupleQueryResultHandlerException("Unknown Value object type: " + value.getClass());
		}
		jg.writeEndObject();
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		if (!headerOpen) {
			startHeader();
		}

		if (!headerComplete) {
			endHeader();
		}

		if (tupleVariablesFound) {
			throw new QueryResultHandlerException("Cannot call handleBoolean after startQueryResults");
		}

		try {
			if (value) {
				jg.writeBooleanField("boolean", Boolean.TRUE);
			}
			else {
				jg.writeBooleanField("boolean", Boolean.FALSE);
			}

			endDocument();
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public final Collection<RioSetting<?>> getSupportedSettings() {
		Set<RioSetting<?>> result = new HashSet<RioSetting<?>>(super.getSupportedSettings());

		result.add(BasicQueryWriterSettings.JSONP_CALLBACK);
		result.add(BasicWriterSettings.PRETTY_PRINT);
		result.add(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL);

		return result;
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLJSONWriterBase
	}

	protected void endDocument()
		throws IOException
	{
		jg.writeEndObject();
		if (getWriterConfig().isSet(BasicQueryWriterSettings.JSONP_CALLBACK)) {
			jg.writeRaw(");");
		}
		jg.flush();
		documentOpen = false;
		headerOpen = false;
		headerComplete = false;
		tupleVariablesFound = false;
		firstTupleWritten = false;
		linksFound = false;
	}

}
