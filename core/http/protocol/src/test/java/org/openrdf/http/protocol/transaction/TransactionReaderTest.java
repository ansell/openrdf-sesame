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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * @author jeen
 */
public class TransactionReaderTest {

	private static final ValueFactory vf = SimpleValueFactory.getInstance();

	private static final IRI bob = vf.createIRI("http://example.org/bob");

	private static final IRI alice = vf.createIRI("http://example.org/alice");

	private static final IRI knows = vf.createIRI("http://example.org/knows");

	private static final char ux0005 = 0x0005;

	private static final Literal controlCharText = vf.createLiteral("foobar." + ux0005 + " foo.");

	private static final IRI context1 = vf.createIRI("http://example.org/context1");

	private static final IRI context2 = vf.createIRI("http://example.org/context2");

	@Test
	public void testRoundtrip()
		throws Exception
	{

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
	public void testControlCharHandling()
		throws Exception
	{
		AddStatementOperation operation = new AddStatementOperation(bob, knows, controlCharText);

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
			assertTrue(addOp.getObject().equals(controlCharText));
		}

	}

}
