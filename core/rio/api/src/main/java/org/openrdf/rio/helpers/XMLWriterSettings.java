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

import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;

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
			"org.openrdf.rio.includexmlpi", "Include XML Processing Instruction", Boolean.TRUE);

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
			"org.openrdf.rio.includerootrdftag", "Include Root RDF Tag", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private XMLWriterSettings() {
	}

}
