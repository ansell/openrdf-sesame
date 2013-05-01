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
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.DatatypeHandler;

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
	protected URI getRecognisedDatatypeUri() {
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
		return ValueFactoryImpl.getInstance().createLiteral("1.23E2", XMLSchema.DOUBLE);
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
		return ValueFactoryImpl.getInstance();
	}

	@Override
	protected URI getUnrecognisedDatatypeUri() {
		return RDF.LANGSTRING;
	}

	@Override
	protected String getExpectedKey() {
		return DatatypeHandler.XMLSCHEMA;
	}

}
