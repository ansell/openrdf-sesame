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
package org.eclipse.rdf4j.rio.datatypes;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.util.LiteralUtilException;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.DatatypeHandler;

/**
 * An implementation of a datatype handler that can process {@link XMLSchema}
 * datatypes.
 * <p>
 * Implemented using {@link XMLDatatypeUtil}.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class XMLSchemaDatatypeHandler implements DatatypeHandler {

	/**
	 * Default constructor.
	 */
	public XMLSchemaDatatypeHandler() {
	}

	@Override
	public boolean isRecognizedDatatype(IRI datatypeUri) {
		if (datatypeUri == null) {
			throw new NullPointerException("Datatype URI cannot be null");
		}

		return XMLDatatypeUtil.isBuiltInDatatype(datatypeUri);
	}

	@Override
	public boolean verifyDatatype(String literalValue, IRI datatypeUri)
		throws LiteralUtilException
	{
		if (isRecognizedDatatype(datatypeUri)) {
			if(literalValue == null) {
				throw new NullPointerException("Literal value cannot be null");
			}
			
			return XMLDatatypeUtil.isValidValue(literalValue, datatypeUri);
		}

		throw new LiteralUtilException("Could not verify XMLSchema literal");
	}

	@Override
	public Literal normalizeDatatype(String literalValue, IRI datatypeUri, ValueFactory valueFactory)
		throws LiteralUtilException
	{
		if (isRecognizedDatatype(datatypeUri)) {
			if(literalValue == null) {
				throw new NullPointerException("Literal value cannot be null");
			}
			
			try {
				return valueFactory.createLiteral(XMLDatatypeUtil.normalize(literalValue, datatypeUri),
						datatypeUri);
			}
			catch (IllegalArgumentException e) {
				throw new LiteralUtilException("Could not normalise XMLSchema literal", e);
			}
		}
		
		throw new LiteralUtilException("Could not normalise XMLSchema literal");
	}

	@Override
	public String getKey() {
		return DatatypeHandler.XMLSCHEMA;
	}
}
