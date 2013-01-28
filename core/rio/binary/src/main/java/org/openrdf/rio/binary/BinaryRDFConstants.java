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
