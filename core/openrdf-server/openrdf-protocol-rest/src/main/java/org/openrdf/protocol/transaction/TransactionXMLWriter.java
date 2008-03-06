/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction;

import java.io.IOException;
import java.io.Writer;

import org.openrdf.util.xml.XMLWriter;

class TransactionXMLWriter extends XMLWriter {

	public TransactionXMLWriter(Writer writer) {
		this(writer, 0);
	}

	public TransactionXMLWriter(Writer writer, int indentLevel) {
		super(writer);
		_indentLevel = indentLevel;
	}

	public void startDocument() throws IOException {
		// do not write the xml declaration
		startDocument(false);
	}

	public void startDocument(boolean writeXMLDeclaration) throws IOException {
		if (writeXMLDeclaration)
			super.startDocument();
	}

	public int getIndentLevel() {
		return _indentLevel;
	}
}
