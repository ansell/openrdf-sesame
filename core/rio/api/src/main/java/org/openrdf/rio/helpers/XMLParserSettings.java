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

import javax.xml.XMLConstants;

import org.xml.sax.XMLReader;

import org.openrdf.rio.RioConfig;
import org.openrdf.rio.RioSetting;

/**
 * ParserSettings for the XML parser features.
 * 
 * @author Michael Grove
 * @see XMLConstants
 * @see <a href="http://xerces.apache.org/xerces-j/features.html">Apache XML
 *      Project - Features</a>
 * @since 2.7.0
 */
public class XMLParserSettings {

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
	 * Defaults to null, This settings is only useful if
	 * {@link RioConfig#isSet(RioSetting)} returns true.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<XMLReader> CUSTOM_XML_READER = new RioSettingImpl<XMLReader>(
			"org.openrdf.rio.xmlreader", "Custom XML Reader", null);

	/**
	 * Parser setting to determine whether to ignore non-fatal errors that come
	 * from SAX parsers.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_SAX_NON_FATAL_ERRORS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.ignoresaxnonfatalerrors", "", true);

	/**
	 * Parser setting to determine whether to ignore non-standard attributes that
	 * are found in an XML document.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_NON_STANDARD_ATTRIBUTES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.ignorenonstandardattributes", "", true);

	/**
	 * Parser setting to determine whether to ignore XML documents containing
	 * invalid NCNAMEs.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_INVALID_NCNAME = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.ignoreinvalidncname", "", true);

	/**
	 * Parser setting to determine whether to throw an error for duplicate uses
	 * of rdf:ID in a single document.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_DUPLICATE_RDF_ID = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonduplicaterdfid", "", true);

	/**
	 * Parser setting to determine whether to ignore XML documents containing
	 * invalid QNAMEs.
	 * <p>
	 * Defaults to true
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_INVALID_QNAME = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.ignoreinvalidqname", "", true);

	/**
	 * Private constructor
	 */
	private XMLParserSettings() {
	}

}
