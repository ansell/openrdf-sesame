/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXParser;

import org.openrdf.http.protocol.transaction.operations.TransactionOperation;

public class TransactionReader {

	/**
	 * parse the transaction from the serialization
	 * 
	 * @throws SAXException
	 *         If the SimpleSAXParser was unable to create an XMLReader or if the
	 *         XML is faulty.
	 * @throws IOException
	 *         If IO problems during parsing.
	 */
	public Collection<TransactionOperation> parse(InputStream in)
		throws SAXException, IOException
	{
		SimpleSAXParser saxParser = new SimpleSAXParser();
		TransactionSAXParser handler = new TransactionSAXParser();
		saxParser.setListener(handler);
		saxParser.parse(in);
		return handler.getTxn();
	}

	/**
	 * parse the transaction from the serialization
	 * 
	 * @throws SAXException
	 *         If the SimpleSAXParser was unable to create an XMLReader or if the
	 *         XML is faulty.
	 * @throws IOException
	 *         If IO problems during parsing.
	 */
	public Collection<TransactionOperation> parse(Reader in)
		throws SAXException, IOException
	{
		SimpleSAXParser saxParser = new SimpleSAXParser();
		TransactionSAXParser handler = new TransactionSAXParser();
		saxParser.setListener(handler);
		saxParser.parse(in);
		return handler.getTxn();
	}
}
