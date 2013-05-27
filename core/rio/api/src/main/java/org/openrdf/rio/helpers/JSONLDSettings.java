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
 * Settings that can be passed to JSONLD Parsers and Writers.
 * 
 * @author Peter Ansell
 * @see <a
 *      href="http://json-ld.org/spec/latest/json-ld-api/#data-structures">JSONLD
 *      Data Structures</a>
 */
public class JSONLDSettings {

	/**
	 * If set to true, the JSON-LD processor replaces arrays with just one
	 * element with that element during compaction. If set to false, all arrays
	 * will remain arrays even if they have just one element.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.0
	 * @see <a
	 *      href="http://json-ld.org/spec/latest/json-ld-api/#data-structures">JSONLD
	 *      Data Structures</a>
	 */
	public static final RioSetting<Boolean> COMPACT_ARRAYS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.jsonld.compactarrays", "Compact arrays", Boolean.TRUE);

	/**
	 * If set to true, the JSON-LD processor is allowed to optimize the output of
	 * the <a href=
	 * "http://json-ld.org/spec/latest/json-ld-api/#compaction-algorithm"
	 * >Compaction algorithm</a> to produce even compacter representations.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 * @see <a
	 *      href="http://json-ld.org/spec/latest/json-ld-api/#data-structures">JSONLD
	 *      Data Structures</a>
	 */
	public static final RioSetting<Boolean> OPTIMIZE = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.jsonld.optimize", "Optimize output", Boolean.FALSE);

	/**
	 * If set to true, the JSON-LD processor will try to convert typed values to
	 * JSON native types instead of using the expanded object form when
	 * converting from RDF. xsd:boolean values will be converted to true or
	 * false. xsd:integer and xsd:double values will be converted to JSON
	 * numbers.
	 * <p>
	 * Defaults to false for RDF compatibility.
	 * 
	 * @since 2.7.0
	 * @see <a
	 *      href="http://json-ld.org/spec/latest/json-ld-api/#data-structures">JSONLD
	 *      Data Structures</a>
	 */
	public static final RioSetting<Boolean> USE_NATIVE_TYPES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.jsonld.usenativetypes", "Use Native JSON Types", Boolean.FALSE);

	/**
	 * If set to true, the JSON-LD processor will use the expanded rdf:type IRI
	 * as the property instead of @type when converting from RDF.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 * @see <a
	 *      href="http://json-ld.org/spec/latest/json-ld-api/#data-structures">JSONLD
	 *      Data Structures</a>
	 */
	public static final RioSetting<Boolean> USE_RDF_TYPE = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.jsonld.userdftype", "Use RDF Type", Boolean.FALSE);

	/**
	 * The {@link JSONLDMode} that the writer will use to reorganise the JSONLD
	 * document after it is created.
	 * <p>
	 * Defaults to {@link JSONLDMode#EXPAND} to provide maximum RDF
	 * compatibility.
	 * 
	 * @since 2.7.0
	 * @see <a href="http://json-ld.org/spec/latest/json-ld-api/#features">JSONLD
	 *      Features</a>
	 */
	public static final RioSetting<JSONLDMode> JSONLD_MODE = new RioSettingImpl<JSONLDMode>(
			"org.openrdf.rio.jsonld.mode", "JSONLD Mode", JSONLDMode.EXPAND);

	/**
	 * Private default constructor.
	 */
	private JSONLDSettings() {
	}

}
