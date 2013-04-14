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
package org.openrdf.rio.datatypes;

import org.openrdf.model.Literal;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.DatatypeHandler;

/**
 * An implementation of a datatype handler that can process {@link RDF}
 * datatypes.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class RDFDatatypeHandler implements DatatypeHandler {

	/**
	 * Default constructor.
	 */
	public RDFDatatypeHandler() {
	}

	@Override
	public boolean isRecognisedDatatype(URI datatypeUri) {
		return org.openrdf.model.vocabulary.RDF.LANGSTRING.equals(datatypeUri)
				|| org.openrdf.model.vocabulary.RDF.XMLLITERAL.equals(datatypeUri);
	}

	@Override
	public boolean verifyDatatype(String literalValue, URI datatypeUri)
		throws LiteralUtilException
	{
		return true;
	}

	@Override
	public Literal normalizeDatatype(String literalValue, URI datatypeUri, ValueFactory valueFactory)
		throws LiteralUtilException
	{
		// TODO: Implement normalisation
		try {
			return valueFactory.createLiteral(literalValue, datatypeUri);
		}
		catch (IllegalArgumentException e) {
			throw new LiteralUtilException("Could not normalise RDF vocabulary defined literal", e);
		}
	}

	@Override
	public String getKey() {
		return DatatypeHandler.RDF;
	}
}
