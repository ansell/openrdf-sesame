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

import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RioSetting;

/**
 * A class encapsulating writer settings that XML writers may support.
 * 
 * @author Peter Ansell
 * @since 2.7.3
 */
public class XMLWriterSettings {

	/**
	 * Boolean setting for XML Writer to determine whether the XML PI (Processing
	 * Instruction) should be printed. If this setting is disabled the user must
	 * have previously printed the XML PI before calling
	 * {@link RDFWriter#startRDF()} for the document to be valid XML.
	 * <p>
	 * Defaults to true.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-complete-document">RDF/XML
	 *      specification</a>
	 */
	public static final RioSetting<Boolean> INCLUDE_XML_PI = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.includexmlpi", "Include XML Processing Instruction", Boolean.TRUE);

	/**
	 * Boolean setting for RDF/XML Writer to determine whether the rdf:RDF root
	 * tag is to be written. The tag is optional in the RDF/XML specification,
	 * but a standalone RDF/XML document typically includes it.
	 * <p>
	 * Defaults to true.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-complete-document">RDF/XML
	 *      specification</a>
	 */
	public static final RioSetting<Boolean> INCLUDE_ROOT_RDF_TAG = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.includerootrdftag", "Include Root RDF Tag", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private XMLWriterSettings() {
	}

}
