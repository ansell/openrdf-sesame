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
package org.openrdf.rio.helpers;

import org.openrdf.rio.RioSetting;

/**
 * A selection of parser settings specific to RDF/JSON parsers.
 * 
 * @author Peter Ansell
 * @since 2.7.1
 */
public class RDFJSONParserSettings {

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * fail if it finds multiple values for a single object in a single
	 * statement.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_VALUES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonmultipleobjectvalues", "Fail on multiple object values", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * fail if it finds multiple types for a single object in a single statement.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_TYPES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonmultipleobjecttypes", "Fail on multiple object types", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * fail if it finds multiple languages for a single object in a single
	 * statement.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_LANGUAGES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonmultipleobjectlanguages", "Fail on multiple object languages", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * fail if it finds multiple datatypes for a single object in a single
	 * statement.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_DATATYPES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonmultipleobjectdatatypes", "Fail on multiple object datatypes", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * fail if it finds multiple properties that it does not recognise in the
	 * JSON document.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_UNKNOWN_PROPERTY = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonunknownproperty", "Fail on unknown property", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * support the graphs extension to make it a quads format.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> SUPPORT_GRAPHS_EXTENSION = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.supportgraphsextension", "SUPPORT_GRAPHS_EXTENSION", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private RDFJSONParserSettings() {
	}

}
