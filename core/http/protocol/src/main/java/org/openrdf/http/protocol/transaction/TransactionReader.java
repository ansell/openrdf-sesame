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
		saxParser.setPreserveWhitespace(true);
		
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
		saxParser.setPreserveWhitespace(true);
		saxParser.setListener(handler);
		saxParser.parse(in);
		return handler.getTxn();
	}
}
