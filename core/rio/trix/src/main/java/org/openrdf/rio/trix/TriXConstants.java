/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
