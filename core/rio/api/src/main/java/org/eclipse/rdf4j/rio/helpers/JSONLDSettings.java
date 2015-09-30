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
			"org.eclipse.rdf4j.rio.jsonld.compactarrays", "Compact arrays", Boolean.TRUE);

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
			"org.eclipse.rdf4j.rio.jsonld.optimize", "Optimize output", Boolean.FALSE);

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
			"org.eclipse.rdf4j.rio.jsonld.usenativetypes", "Use Native JSON Types", Boolean.FALSE);

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
			"org.eclipse.rdf4j.rio.jsonld.userdftype", "Use RDF Type", Boolean.FALSE);

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
			"org.eclipse.rdf4j.rio.jsonld.mode", "JSONLD Mode", JSONLDMode.EXPAND);

	/**
	 * Private default constructor.
	 */
	private JSONLDSettings() {
	}

}
