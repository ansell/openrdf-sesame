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
package org.openrdf.rio.jsonld;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFParserBase;

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
public class JSONLDParser extends RDFParserBase implements RDFParser {

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
