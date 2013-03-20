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
package org.openrdf.rio;

import java.io.Serializable;

import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.BasicParserSettings;

/**
 * A container object for easy setting and passing of {@link RDFParser}
 * configuration options.
 * 
 * @author Jeen Broekstra
 * @author Peter Ansell
 */
public class ParserConfig extends RioConfig implements Serializable {

	/**
	 * @since 2.7.0
	 */
	private static final long serialVersionUID = 270L;

	/**
	 * Creates a ParserConfig object starting with default settings.
	 */
	public ParserConfig() {
		super();
	}

	/**
	 * Creates a ParserConfig object with the supplied config settings.
	 */
	public ParserConfig(boolean verifyData, boolean stopAtFirstError, boolean preserveBNodeIDs,
			DatatypeHandling datatypeHandling)
	{
		this();
		this.set(BasicParserSettings.VERIFY_DATA, verifyData);
		this.set(BasicParserSettings.STOP_AT_FIRST_ERROR, stopAtFirstError);
		this.set(BasicParserSettings.PRESERVE_BNODE_IDS, preserveBNodeIDs);
		this.set(BasicParserSettings.DATATYPE_HANDLING, datatypeHandling);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#VERIFY_DATA} setting.
	 */
	public boolean verifyData() {
		return this.get(BasicParserSettings.VERIFY_DATA);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#STOP_AT_FIRST_ERROR}
	 *         setting.
	 */
	public boolean stopAtFirstError() {
		return this.get(BasicParserSettings.STOP_AT_FIRST_ERROR);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#PRESERVE_BNODE_IDS}
	 *         setting.
	 */
	public boolean isPreserveBNodeIDs() {
		return this.get(BasicParserSettings.PRESERVE_BNODE_IDS);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#DATATYPE_HANDLING} setting.
	 */
	public DatatypeHandling datatypeHandling() {
		return this.get(BasicParserSettings.DATATYPE_HANDLING);
	}
}
