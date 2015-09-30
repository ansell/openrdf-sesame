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
package org.openrdf.rio.binary;


class BinaryRDFConstants {

	/**
	 * Magic number for Binary RDF Table Result files.
	 */
	static final byte[] MAGIC_NUMBER = new byte[] { 'B', 'R', 'D', 'F' };

	/**
	 * The version number of the current format.
	 */
	static final int FORMAT_VERSION = 1;

	/* RECORD TYPES */

	static final int NAMESPACE_DECL = 0;

	static final int STATEMENT = 1;

	static final int COMMENT = 2;

	static final int VALUE_DECL = 3;

	// public static final int ERROR = 126;

	static final int END_OF_DATA = 127;

	/* VALUE TYPES */

	static final int NULL_VALUE = 0;

	static final int URI_VALUE = 1;

	static final int BNODE_VALUE = 2;

	static final int PLAIN_LITERAL_VALUE = 3;

	static final int LANG_LITERAL_VALUE = 4;

	static final int DATATYPE_LITERAL_VALUE = 5;

	static final int VALUE_REF = 6;
}
