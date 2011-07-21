/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.binary;

import java.nio.charset.Charset;

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

	static final Charset CHARSET = Charset.forName("UTF-8");
}
