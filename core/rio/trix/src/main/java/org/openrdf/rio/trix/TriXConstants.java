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
package org.openrdf.rio.trix;

/**
 * Interface defining a number of constants for the TriX document format.
 */
public interface TriXConstants {

	/** The TriX namespace. */
	public static final String NAMESPACE = "http://www.w3.org/2004/03/trix/trix-1/";

	/** The root tag. */
	public static final String ROOT_TAG = "TriX";

	/** The tag that starts a new context/graph. */
	public static final String CONTEXT_TAG = "graph";

	/** The tag that starts a new triple. */
	public static final String TRIPLE_TAG = "triple";

	/** The tag for URI values. */
	public static final String URI_TAG = "uri";

	/** The tag for BNode values. */
	public static final String BNODE_TAG = "id";

	/** The tag for plain literal values. */
	public static final String PLAIN_LITERAL_TAG = "plainLiteral";

	/** The tag for typed literal values. */
	public static final String TYPED_LITERAL_TAG = "typedLiteral";

	/** The attribute for language tags of plain literal. */
	public static final String LANGUAGE_ATT = "xml:lang";

	/** The attribute for datatypes of typed literal. */
	public static final String DATATYPE_ATT = "datatype";
}
