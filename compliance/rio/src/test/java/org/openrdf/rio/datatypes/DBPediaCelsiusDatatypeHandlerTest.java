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

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
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
		return ValueFactoryImpl.getInstance().createIRI("http://dbpedia.org/datatype/", "degreeCelsius");
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
		return ValueFactoryImpl.getInstance().createLiteral("1.0",
				ValueFactoryImpl.getInstance().createIRI("http://dbpedia.org/datatype/", "degreeCelsius"));
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
		return ValueFactoryImpl.getInstance();
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
