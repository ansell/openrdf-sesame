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
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.DatatypeHandler;
import org.eclipse.rdf4j.rio.datatypes.AbstractDatatypeHandlerTest;
import org.eclipse.rdf4j.rio.datatypes.XMLSchemaDatatypeHandler;

/**
 * Test for {@link XMLSchemaDatatypeHandler} with {@link XMLSchema#DOUBLE}.
 * 
 * @author Peter Ansell
 */
public class XMLSchemaDoubleDatatypeHandlerTest extends AbstractDatatypeHandlerTest {

	// -------------------------------------
	// XMLSchema Double specific methods
	// -------------------------------------

	@Override
	protected IRI getRecognisedDatatypeUri() {
		return XMLSchema.DOUBLE;
	}

	@Override
	protected String getValueMatchingRecognisedDatatypeUri() {
		return "123.0000";
	}

	@Override
	protected String getValueNotMatchingRecognisedDatatypeUri() {
		return "This is not a double";
	}

	@Override
	protected Literal getNormalisedLiteralForRecognisedDatatypeAndValue() {
		return SimpleValueFactory.getInstance().createLiteral("1.23E2", XMLSchema.DOUBLE);
	}

	// -------------------------------------
	// Common methods
	// -------------------------------------

	@Override
	protected DatatypeHandler getNewDatatypeHandler() {
		return new XMLSchemaDatatypeHandler();
	}

	@Override
	protected ValueFactory getValueFactory() {
		return SimpleValueFactory.getInstance();
	}

	@Override
	protected IRI getUnrecognisedDatatypeUri() {
		return RDF.LANGSTRING;
	}

	@Override
	protected String getExpectedKey() {
		return DatatypeHandler.XMLSCHEMA;
	}

}
