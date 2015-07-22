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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A utility class to help converting Sesame Models to and from RDF/JSON using
 * Jackson.
 * 
 * @author Peter Ansell
 */
class RDFJSONUtility {

	public static final String NULL = "null";

	public static final String GRAPHS = "graphs";

	public static final String URI = "uri";

	public static final String BNODE = "bnode";

	public static final String DATATYPE = "datatype";

	public static final String LITERAL = "literal";

	public static final String LANG = "lang";

	public static final String TYPE = "type";

	public static final String VALUE = "value";

	public static final JsonFactory JSON_FACTORY = new JsonFactory();

	static {
		// Disable features that may work for most JSON where the field names are
		// in limited supply,
		// but does not work for RDF/JSON where a wide range of URIs are used for
		// subjects and predicates
		JSON_FACTORY.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
		JSON_FACTORY.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
		JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

}