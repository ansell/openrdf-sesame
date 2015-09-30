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
package org.eclipse.rdf4j.rio.helpers;

import org.eclipse.rdf4j.rio.RioSetting;

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
			"org.eclipse.rdf4j.rio.failonmultipleobjectvalues", "Fail on multiple object values", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * fail if it finds multiple types for a single object in a single statement.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_TYPES = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failonmultipleobjecttypes", "Fail on multiple object types", Boolean.TRUE);

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
			"org.eclipse.rdf4j.rio.failonmultipleobjectlanguages", "Fail on multiple object languages", Boolean.TRUE);

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
			"org.eclipse.rdf4j.rio.failonmultipleobjectdatatypes", "Fail on multiple object datatypes", Boolean.TRUE);

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
			"org.eclipse.rdf4j.rio.failonunknownproperty", "Fail on unknown property", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should
	 * support the graphs extension to make it a quads format.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> SUPPORT_GRAPHS_EXTENSION = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.supportgraphsextension", "SUPPORT_GRAPHS_EXTENSION", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private RDFJSONParserSettings() {
	}

}
