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
