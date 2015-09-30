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
 * A class encapsulating the basic writer settings that most writers may
 * support.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class BasicWriterSettings {

	/**
	 * Boolean setting for writer to determine whether pretty printing is
	 * preferred.
	 * <p>
	 * Defaults to true.
	 */
	public static final RioSetting<Boolean> PRETTY_PRINT = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.prettyprint", "Pretty print", Boolean.TRUE);

	/**
	 * Boolean setting for writer to determine whether it should remove the
	 * xsd:string datatype from literals and represent them as RDF-1.0 Plain
	 * Literals.
	 * <p>
	 * In RDF-1.1, all literals that would have been Plain Literals in RDF-1.0
	 * will be typed as xsd:string internally.
	 * <p>
	 * Defaults to true to allow for backwards compatibility without enforcing
	 * it.
	 */
	public static final RioSetting<Boolean> XSD_STRING_TO_PLAIN_LITERAL = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.rdf10plainliterals", "RDF-1.0 compatible Plain Literals", Boolean.TRUE);

	/**
	 * Boolean setting for writer to determine whether it should omit the
	 * rdf:langString datatype from language literals when serialising them.
	 * <p>
	 * In RDF-1.1, all RDF-1.0 Language Literals are typed using rdf:langString
	 * in the abstract model, but this datatype is not necessary for concrete
	 * syntaxes.
	 * <p>
	 * In most concrete syntaxes it is either syntactically invalid or
	 * semantically ambiguous to have a language tagged literal with an explicit
	 * datatype. In those cases this setting will not be used, and the
	 * rdf:langString datatype will not be attached to language tagged literals.
	 * <p>
	 * In particular, in RDF/XML, if rdf:langString is serialised, the language
	 * tag may not be retained when the document is parsed due to the precedence
	 * rule in RDF/XML for datatype over language.
	 * <p>
	 * Defaults to true as rdf:langString was not previously used, and should not
	 * be commonly required.
	 */
	public static final RioSetting<Boolean> RDF_LANGSTRING_TO_LANG_LITERAL = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.rdf10languageliterals", "RDF-1.0 compatible Language Literals", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private BasicWriterSettings() {
	}

}
