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
package org.openrdf.rio.datatypes;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.DatatypeHandler;

/**
 * Test for {@link DBPediaDatatypeHandler} with
 * http://dbpedia.org/datatype/degreeCelsius .
 * 
 * @author Peter Ansell
 */
public class DBPediaCelsiusDatatypeHandlerTest extends AbstractDatatypeHandlerTest {

	@Ignore("DBPedia datatypes are not currently verified")
	@Test
	@Override
	public void testVerifyDatatypeInvalidValue()
		throws Exception
	{
	}

	@Ignore("DBPedia datatypes are not currently normalised")
	@Test
	@Override
	public void testNormalizeDatatypeInvalidValue()
		throws Exception
	{
	}

	// -------------------------------------
	// RDF LangString specific methods
	// -------------------------------------

	@Override
	protected IRI getRecognisedDatatypeUri() {
		return SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/datatype/", "degreeCelsius");
	}

	@Override
	protected String getValueMatchingRecognisedDatatypeUri() {
		return "1.0";
	}

	@Override
	protected String getValueNotMatchingRecognisedDatatypeUri() {
		return "Not a degrees celsius value.";
	}

	@Override
	protected Literal getNormalisedLiteralForRecognisedDatatypeAndValue() {
		return SimpleValueFactory.getInstance().createLiteral("1.0",
				SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/datatype/", "degreeCelsius"));
	}

	// -------------------------------------
	// Common methods
	// -------------------------------------

	@Override
	protected DatatypeHandler getNewDatatypeHandler() {
		return new DBPediaDatatypeHandler();
	}

	@Override
	protected ValueFactory getValueFactory() {
		return SimpleValueFactory.getInstance();
	}

	@Override
	protected IRI getUnrecognisedDatatypeUri() {
		return XMLSchema.DOUBLE;
	}

	@Override
	protected String getExpectedKey() {
		return DatatypeHandler.DBPEDIA;
	}

}
