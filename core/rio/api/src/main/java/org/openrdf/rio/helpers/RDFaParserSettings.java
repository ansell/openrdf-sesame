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
 * A selection of parser settings specific to RDFa parsers.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class RDFaParserSettings {

	/**
	 * Boolean setting for parser to determine the RDFa version to use when
	 * processing the document.
	 * <p>
	 * Defaults to {@link RDFaVersion#RDFA_1_0}.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<RDFaVersion> RDFA_COMPATIBILITY = new RioSettingImpl<RDFaVersion>(
			"org.openrdf.rio.rdfa.version", "RDFa Version Compatibility", RDFaVersion.RDFA_1_0);

	/**
	 * Enables or disables <a href=
	 * "http://www.w3.org/TR/2012/REC-rdfa-core-20120607/#s_vocab_expansion"
	 * >vocabulary expansion</a> feature.
	 * <p>
	 * Defaults to false
	 * 
	 * @see http://www.w3.org/TR/2012/REC-rdfa-core-20120607/#s_vocab_expansion
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> VOCAB_EXPANSION_ENABLED = new RioSettingImpl<Boolean>(
			"http://www.w3.org/TR/2012/REC-rdfa-core-20120607/#s_vocab_expansion", "Vocabulary Expansion",
			Boolean.FALSE);

	/**
	 * Private default constructor.
	 */
	private RDFaParserSettings() {
	}

}
