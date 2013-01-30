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
package org.openrdf.model;

import java.io.Serializable;

/**
 * The supertype of all RDF model objects (URIs, blank nodes and literals).
 */
public interface Value extends Serializable {

	/**
	 * Returns the String-value of a <tt>Value</tt> object. This returns either
	 * a {@link Literal}'s label, a {@link URI}'s URI or a {@link BNode}'s ID.
	 */
	public String stringValue();
}
