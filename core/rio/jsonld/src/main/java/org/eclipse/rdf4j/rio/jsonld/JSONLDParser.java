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
package org.eclipse.rdf4j.rio.jsonld;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFParser;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

/**
 * An {@link RDFParser} that links to {@link JSONLDInternalTripleCallback}.
 * 
 * @author Peter Ansell
 */
public class JSONLDParser extends AbstractRDFParser implements RDFParser {

	/**
	 * Default constructor
	 */
	public JSONLDParser() {
		super();
	}

	/**
	 * Creates a Sesame JSONLD Parser using the given {@link ValueFactory} to
	 * create new {@link Value}s.
	 * 
	 * @param valueFactory
	 *        The ValueFactory to use
	 */
	public JSONLDParser(final ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.JSONLD;
	}

	@Override
	public void parse(final InputStream in, final String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		final JSONLDInternalTripleCallback callback = new JSONLDInternalTripleCallback(getRDFHandler(),
				valueFactory, getParserConfig(), getParseErrorListener());

		final JsonLdOptions options = new JsonLdOptions(baseURI);
		options.useNamespaces = true;

		try {
			JsonLdProcessor.toRDF(JsonUtils.fromInputStream(in), callback, options);
		}
		catch (final JsonLdError e) {
			throw new RDFParseException("Could not parse JSONLD", e);
		}
		catch (final JsonParseException e) {
			throw new RDFParseException("Could not parse JSONLD", e);
		}
		catch (final RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof RDFParseException) {
				throw (RDFParseException)e.getCause();
			}
			throw e;
		}
	}

	@Override
	public void parse(final Reader reader, final String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		final JSONLDInternalTripleCallback callback = new JSONLDInternalTripleCallback(getRDFHandler(),
				valueFactory, getParserConfig(), getParseErrorListener());

		final JsonLdOptions options = new JsonLdOptions(baseURI);
		options.useNamespaces = true;

		try {
			JsonLdProcessor.toRDF(JsonUtils.fromReader(reader), callback, options);
		}
		catch (final JsonLdError e) {
			throw new RDFParseException("Could not parse JSONLD", e);
		}
		catch (final JsonParseException e) {
			throw new RDFParseException("Could not parse JSONLD", e);
		}
		catch (final RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof RDFParseException) {
				throw (RDFParseException)e.getCause();
			}
			throw e;
		}
	}

}
