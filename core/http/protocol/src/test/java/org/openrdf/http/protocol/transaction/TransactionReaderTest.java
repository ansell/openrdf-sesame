/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;


/**
 *
 * @author jeen
 */
public class TransactionReaderTest {

	private static final URI bob = new URIImpl("http://example.org/bob");
	private static final URI alice = new URIImpl("http://example.org/alice");
	private static final URI knows = new URIImpl("http://example.org/knows");
	
	private static final char ux0005 = 0x0005;
	
	private static final Literal controlCharText = new LiteralImpl("foobar." + ux0005 + " foo.");
	
	private static final URI context1 = new URIImpl("http://example.org/context1");
	private static final URI context2 = new URIImpl("http://example.org/context2");
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	@Test
	public void testRoundtrip() throws Exception {
		
		AddStatementOperation operation = new AddStatementOperation(bob, knows, alice, context1, context2);
		
		List<TransactionOperation> txn = new ArrayList<TransactionOperation>();
		txn.add(operation);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TransactionWriter w = new TransactionWriter();
		w.serialize(txn, out);
		
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TransactionReader r = new TransactionReader();
		Collection<TransactionOperation> parsedTxn = r.parse(in);
		
		assertNotNull(parsedTxn);
		
		for (TransactionOperation op : parsedTxn) {
			assertTrue(op instanceof AddStatementOperation);
			AddStatementOperation addOp = (AddStatementOperation)op;
			
			Resource[] contexts = addOp.getContexts();
			
			assertEquals(2, contexts.length);
			assertTrue(contexts[0].equals(context1) || contexts[1].equals(context1));
			assertTrue(contexts[0].equals(context2) || contexts[1].equals(context2));
		}
		
	}
	
	@Test
	public void testControlCharHandling() throws Exception {
		AddStatementOperation operation = new AddStatementOperation(bob, knows, controlCharText);
		
		System.out.println(controlCharText.stringValue());
		
		List<TransactionOperation> txn = new ArrayList<TransactionOperation>();
		txn.add(operation);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TransactionWriter w = new TransactionWriter();
		w.serialize(txn, out);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TransactionReader r = new TransactionReader();
		Collection<TransactionOperation> parsedTxn = r.parse(in);
		
		assertNotNull(parsedTxn);
		
		for (TransactionOperation op : parsedTxn) {
			assertTrue(op instanceof AddStatementOperation);
			AddStatementOperation addOp = (AddStatementOperation)op;

			System.out.println(addOp.getObject().stringValue());
			assertTrue(addOp.getObject().equals(controlCharText));
		}

	}

}
