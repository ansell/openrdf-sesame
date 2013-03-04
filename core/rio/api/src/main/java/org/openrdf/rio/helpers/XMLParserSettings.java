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
 * @since 2.7.0
 */
public class XMLParserSettings {

	/**
	 * Parser setting for the secure processing feature of XML parsers to avoid
	 * DOS attacks
	 * <p>
	 * Defaults to true
	 * 
	 * @see http 
	 *      ://docs.oracle.com/javase/1.5.0/docs/api/javax/xml/XMLConstants.html
	 *      #FEATURE_SECURE_PROCESSING
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> SECURE_PROCESSING = new RioSettingImpl<Boolean>(
			XMLConstants.FEATURE_SECURE_PROCESSING, "Secure processing feature of XMLConstants", true);

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
	 * Private constructor
	 */
	private XMLParserSettings() {
	}

}
