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
			"org.openrdf.rio.prettyprint", "Pretty print", Boolean.TRUE);

	/**
	 * Boolean setting for writer to determine whether it should attempt to
	 * remove the xsd:string datatype from literals and represent them as RDF-1.0
	 * Plain Literals.
	 * <p>
	 * In RDF-1.1, all literals that would have been Plain Literals in RDF-1.0
	 * will be typed as xsd:string internally.
	 * <p>
	 * Defaults to false to allow for backwards compatibility without enforcing
	 * it.
	 */
	public static final RioSetting<Boolean> XSD_STRING_TO_PLAIN_LITERAL = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.rdf10plainliterals", "RDF-1.0 compatible Plain Literals", Boolean.FALSE);

	/**
	 * Boolean setting for writer to determine whether it should attempt to
	 * remove the rdf:langString datatype from literals and represent them as
	 * RDF-1.0 Language Literals.
	 * <p>
	 * In RDF-1.1, all literals that would have been Language Literals in RDF-1.0
	 * will be typed as rdf:langString internally.
	 * <p>
	 * Defaults to false to allow for backwards compatibility without enforcing
	 * it.
	 */
	public static final RioSetting<Boolean> RDF_LANGSTRING_TO_LANG_LITERAL = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.rdf10languageliterals", "RDF-1.0 compatible Language Literals", Boolean.FALSE);

	/**
	 * Private default constructor.
	 */
	private BasicWriterSettings() {
	}

}
