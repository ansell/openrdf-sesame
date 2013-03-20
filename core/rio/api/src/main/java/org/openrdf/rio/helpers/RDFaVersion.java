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
 * Enumeration for tracking versions of the RDFa specification to specify
 * processing capabilities of RDFa modules.
 * 
 * @author Peter Ansell
 */
public enum RDFaVersion {

	/**
	 * The initial RDFa 1.0 version (2008)
	 * 
	 * @see http://www.w3.org/TR/2008/REC-rdfa-syntax-20081014/
	 */
	RDFA_1_0("RDFa 1.0", "http://www.w3.org/TR/2008/REC-rdfa-syntax-20081014/"),

	/**
	 * The modified RDFa 1.1 version (2012)
	 * 
	 * @see http://www.w3.org/TR/2012/REC-rdfa-core-20120607/
	 */
	RDFA_1_1("RDFa 1.1", "http://www.w3.org/TR/2012/REC-rdfa-core-20120607/"),

	;

	private final String label;

	private final String reference;

	RDFaVersion(String nextLabel, String nextRef) {
		label = nextLabel;
		reference = nextRef;
	}

	/**
	 * @return Returns the reference URL for the given version.
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
}
