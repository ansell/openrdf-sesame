/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.SAXException;

import org.openrdf.protocol.transaction.operations.OperationList;
import org.openrdf.util.xml.SimpleSAXParser;

public class TransactionReader {

	/**
	 * parse the transaction from the serialization
	 * 
	 * @param in
	 * @return
	 * @throws SAXException
	 *             If the SimpleSAXParser was unable to create an XMLReader or
	 *             if the XML is faulty.
	 * @throws IOException
	 *             If IO problems during parsing.
	 */
	public OperationList parseTransactionFromSerialization(InputStream in)
			throws SAXException, IOException {
		// TODO: DTD is to be defined

		// Transaction handler needs a rdf parser to parse the embedded rdf
		// serializations
		SimpleSAXParser saxParser = new SimpleSAXParser();
		TransactionSAXParser handler = new TransactionSAXParser();
		saxParser.setListener(handler);
		saxParser.parse(in);
		return handler.getOperationList();
	}

	/**
	 * parse the transaction from the serialization
	 * 
	 * @param in
	 * @return
	 * @throws SAXException
	 *             If the SimpleSAXParser was unable to create an XMLReader or
	 *             if the XML is faulty.
	 * @throws IOException
	 *             If IO problems during parsing.
	 */
	public OperationList parseTransactionFromSerialization(Reader in)
			throws SAXException, IOException {
		// TODO: DTD is to be defined

		// Transaction handler needs a rdf parser to parse the embedded rdf
		// serializations
		SimpleSAXParser saxParser = new SimpleSAXParser();
		TransactionSAXParser handler = new TransactionSAXParser();
		saxParser.setListener(handler);
		saxParser.parse(in);
		return handler.getOperationList();
	}
}
