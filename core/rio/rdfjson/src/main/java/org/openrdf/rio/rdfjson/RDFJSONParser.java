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
import java.io.Reader;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFParserBase;

/**
 * {@link RDFParser} implementation for the RDF/JSON format
 * 
 * @author Peter Ansell
 */
public class RDFJSONParser extends RDFParserBase implements RDFParser {

	private final RDFFormat actualFormat;

	/**
	 * Creates a parser using {@link RDFFormat#RDFJSON} to identify the parser.
	 */
	public RDFJSONParser() {
		this.actualFormat = RDFFormat.RDFJSON;
	}

	/**
	 * Creates a parser using the given RDFFormat to self identify.
	 * 
	 * @param actualFormat
	 */
	public RDFJSONParser(final RDFFormat actualFormat) {
		this.actualFormat = actualFormat;
	}

	@Override
	public RDFFormat getRDFFormat() {
		return this.actualFormat;
	}

	@Override
	public void parse(final InputStream inputStream, final String baseUri)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (this.rdfHandler == null) {
			throw new IllegalStateException("RDF handler has not been set");
		}

		this.rdfHandler.startRDF();
		RDFJSONUtility.rdfJsonToHandler(inputStream, this.rdfHandler, this.valueFactory);
		this.rdfHandler.endRDF();
	}

	@Override
	public void parse(final Reader reader, final String baseUri)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (this.rdfHandler == null) {
			throw new IllegalStateException("RDF handler has not been set");
		}

		this.rdfHandler.startRDF();
		RDFJSONUtility.rdfJsonToHandler(reader, this.rdfHandler, this.valueFactory);
		this.rdfHandler.endRDF();
	}

}
