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
	 * @see <a
	 *      href="http://www.w3.org/TR/2012/REC-rdfa-core-20120607/#s_vocab_expansion">RDFa
	 *      Vocabulary Expansion</a>
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> VOCAB_EXPANSION_ENABLED = new RioSettingImpl<Boolean>(
			"http://www.w3.org/TR/2012/REC-rdfa-core-20120607/#s_vocab_expansion", "Vocabulary Expansion",
			Boolean.FALSE);
	/**
	 * Boolean setting for parser to determine whether the published RDFa
	 * prefixes are used to substitute for undefined prefixes.
	 * <p>
	 * Defaults to false.
	 *
	 * @since 2.7.0
	 * @deprecated Use {@link BasicParserSettings#NAMESPACES}
	 */
	@Deprecated
	public static final RioSetting<Boolean> FAIL_ON_RDFA_UNDEFINED_PREFIXES = new RioSettingImpl<Boolean>(
		    "org.openrdf.rio.allowrdfaundefinedprefixes", "Allow RDFa Undefined Prefixes", Boolean.FALSE);

	/**
	 * Private default constructor.
	 */
	private RDFaParserSettings() {
	}

}
