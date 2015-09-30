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

import javax.xml.XMLConstants;

import org.eclipse.rdf4j.rio.RioConfig;
import org.eclipse.rdf4j.rio.RioSetting;
import org.xml.sax.XMLReader;

/**
 * ParserSettings for the XML parser features.
 * 
 * @author Michael Grove
 * @author Peter Ansell
 * @see XMLConstants
 * @see <a href="http://xerces.apache.org/xerces-j/features.html">Apache XML
 *      Project - Features</a>
 * @since 2.7.0
 */
public final class XMLParserSettings {

	/**
	 * Parser setting for the secure processing feature of XML parsers to avoid
	 * DOS attacks
	 * <p>
	 * Defaults to true
	 * 
	 * @see <a
	 *      href="http://docs.oracle.com/javase/6/docs/api/javax/xml/XMLConstants.html#FEATURE_SECURE_PROCESSING">
	 *      XMLConstants.FEATURE_SECURE_PROCESSING</a>
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> SECURE_PROCESSING = new RioSettingImpl<Boolean>(
			XMLConstants.FEATURE_SECURE_PROCESSING, "Secure processing feature of XMLConstants", true);

	/**
	 * Parser setting specifying whether external DTDs should be loaded.
	 * <p>
	 * Defaults to true.
	 * 
	 * @see <a href="http://xerces.apache.org/xerces-j/features.html">Apache XML
	 *      Project - Features</a>
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> LOAD_EXTERNAL_DTD = new RioSettingImpl<Boolean>(
			"http://apache.org/xml/features/nonvalidating/load-external-dtd", "Load External DTD", true);

	/**
	 * Parser setting to customise the XMLReader that is used by an XML based Rio
	 * parser.
	 * <p>
	 * IMPORTANT: The XMLReader must not be shared across different readers, so
	 * this setting must be reset for each parse operation.
	 * <p>
	 * Defaults to null, This settings is only useful if
	 * {@link RioConfig#isSet(RioSetting)} returns true.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<XMLReader> CUSTOM_XML_READER = new RioSettingImpl<XMLReader>(
			"org.eclipse.rdf4j.rio.xmlreader", "Custom XML Reader", null);

	/**
	 * Parser setting to determine whether to ignore non-fatal errors that come
	 * from SAX parsers.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_SAX_NON_FATAL_ERRORS = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failonsaxnonfatalerrors", "Fail on SAX non-fatal errors", true);

	/**
	 * Parser setting to determine whether to ignore non-standard attributes that
	 * are found in an XML document.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_NON_STANDARD_ATTRIBUTES = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failonnonstandardattributes", "Fail on non-standard attributes", true);

	/**
	 * Parser setting to determine whether to ignore XML documents containing
	 * invalid NCNAMEs.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_INVALID_NCNAME = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failoninvalidncname", "Fail on invalid NCName", true);

	/**
	 * Parser setting to determine whether to throw an error for duplicate uses
	 * of rdf:ID in a single document.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_DUPLICATE_RDF_ID = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failonduplicaterdfid", "Fail on duplicate RDF ID", true);

	/**
	 * Parser setting to determine whether to ignore XML documents containing
	 * invalid QNAMEs.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_INVALID_QNAME = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failoninvalidqname", "Fail on invalid QName", true);

	/**
	 * Parser setting to determine whether to throw an error for XML documents
	 * containing mismatched tags
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.1
	 */
	public static final RioSetting<Boolean> FAIL_ON_MISMATCHED_TAGS = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.failonmismatchedtags", "Fail on mismatched tags", true);

	/**
	 * Flag indicating whether the parser parses stand-alone RDF documents. In
	 * stand-alone documents, the rdf:RDF element is optional if it contains just
	 * one element.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.8
	 */
	public static final RioSetting<Boolean> PARSE_STANDALONE_DOCUMENTS = new RioSettingImpl<Boolean>(
			"org.eclipse.rdf4j.rio.parsestandalonedocuments", "Parse standalone documents", true);

	/**
	 * Private constructor
	 */
	private XMLParserSettings() {
	}

}
