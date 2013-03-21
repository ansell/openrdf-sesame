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

/**
 * Specifies constants to identify various modes that are relevant to JSONLD
 * documents.
 * 
 * @author Peter Ansell
 * @see <a href="http://json-ld.org/spec/latest/json-ld-api/#features">JSONLD
 *      Features</a>
 */
public enum JSONLDMode {

	EXPAND("Expansion", "http://json-ld.org/spec/latest/json-ld-api/index.html#expansion"),

	COMPACT("Compaction", "http://json-ld.org/spec/latest/json-ld-api/index.html#compaction"),

	FLATTEN("Flattening", "http://json-ld.org/spec/latest/json-ld-api/index.html#flattening"),

	;

	private final String label;

	private final String reference;

	JSONLDMode(String label, String reference) {
		this.label = label;
		this.reference = reference;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Returns the reference URL for the given mode.
	 */
	public String getReference() {
		return reference;
	}
}
