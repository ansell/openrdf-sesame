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
package org.openrdf.rio.rdfxml;

/**
 * An XML attribute.
 */
class Att {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String namespace;

	private String localName;

	private String qName;

	private String value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Att(String namespace, String localName, String qName, String value) {
		this.namespace = namespace;
		this.localName = localName;
		this.qName = qName;
		this.value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getNamespace() {
		return namespace;
	}

	public String getLocalName() {
		return localName;
	}

	public String getURI() {
		return namespace + localName;
	}

	public String getQName() {
		return qName;
	}

	public String getValue() {
		return value;
	}
}
