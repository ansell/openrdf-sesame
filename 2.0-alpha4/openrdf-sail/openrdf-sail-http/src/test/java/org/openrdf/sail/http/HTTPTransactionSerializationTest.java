/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.http;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.protocol.transaction.TransactionReader;
import org.openrdf.protocol.transaction.TransactionSerializationException;
import org.openrdf.protocol.transaction.TransactionWriter;
import org.openrdf.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.protocol.transaction.operations.ClearContextOperation;
import org.openrdf.protocol.transaction.operations.ClearRepositoryOperation;
import org.openrdf.protocol.transaction.operations.OperationList;
import org.openrdf.protocol.transaction.operations.RemoveStatementOperation;
import org.openrdf.protocol.transaction.operations.TransactionOperation;


public class HTTPTransactionSerializationTest extends TestCase {
	
	// statements will be added and removed directly to the repository
//	private Repository _repositoryDirect;
//	// statements will be added and removed via a serialized and parsed transaction to the repository
//	private Repository _repositoryTA;
//	
//	// sail to create the transaction. will not be used to store the actual data
//	private HTTPSail _httpSail;
	

/*	
	protected void setUp() throws Exception {
		super.setUp();
		// Sail implementation MemoryStore
		MemoryStore memorySail = new MemoryStore();
		_repositoryDirect = new RepositoryImpl(memorySail);
		_repositoryDirect.initialize();
		// Sail implementation HTTPSail...use it for transaction serialization
		String server_url = "http://NOTUSED";
		_httpSail = new HTTPSail(server_url, "NOTUSED", "NOTUSED", "NOTUSED");
		// use another repository
//		_repositoryTA = new RepositoryImpl(new MemoryStore());
//		_repositoryTA.initialize();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		_repositoryDirect.getConnection().clear();
		_repositoryTA.getConnection().clear();
	}
	
	*/
	TransactionWriter taWriter;
	TransactionReader taReader;
	public void setUp() {
		taWriter = new TransactionWriter();
		taReader = new TransactionReader();
	}
	
	
	/**
	 * test if the writer adds the statistics correctly
	 */
	public void testTransactionWriter() throws Exception {
		OperationList operations = createOperationsA();

		StringWriter writer = new StringWriter();
		taWriter.serialize(operations, writer);
		System.out.println("serialized ops:");
		System.out.println(writer);

		assertTrue(taWriter.getAddCount() == 4);
		assertTrue(taWriter.getRemoveCount() == 1);
		assertTrue(taWriter.getClearCount() == 4);
		assertTrue(taWriter.getNumberTriXSerializations() == 3);
		assertTrue(taWriter.getAddGroupCount() == 2);
		assertTrue(taWriter.getRemoveGroupCount() == 1);
		//assertEquals(4, taWriter.getClearGroupCount());
	}
	
	public void testTransactionWriter2() throws Exception {
		OperationList operations = createOperationsA();
		StringWriter writer = new StringWriter();
		// 2. test
		taWriter.setReductionFlag(true);
		taWriter.serialize(operations, writer);
		System.out.println("optimized serialized ops:");
		System.out.println(writer);
		System.out.println("number counts: " + taWriter.getClearCount());
		assertTrue(taWriter.getClearCount() == 2);
		//assertTrue(taWriter.getClearGroupCount() == 2);
	}
	
	public void testNullParam() throws Exception 
	{
		// 3. test
		try {
			taWriter.serialize(null, null);
			fail("shoud have thrown an TransactionSerializationException");
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	
	public void testEmptyList() 
	{
		// 4. test
		try {
			taWriter.serialize(new ArrayList<TransactionOperation>(), null);
			fail("fail because....?? TODO");
		}
		catch (TransactionSerializationException e) {
			
		}
	}
	
	public void testClear() throws Exception
	{
		// 5. test only one operation
		OperationList operations = new OperationList();
		StringWriter writer = new StringWriter();
		operations.add(new ClearRepositoryOperation()); 
		taWriter.serialize(operations, writer);
		System.out.println("only one operation: clear repo");
		System.out.println(writer);
		assertTrue(taWriter.getClearCount() == 1);
		//assertTrue(taWriter.getClearGroupCount() == 1);
	}
	
	public void testNotOptimizeTwoClears() throws Exception
	{
		//	 6. test
		OperationList operations = new OperationList();
		operations.add(new ClearRepositoryOperation());
		operations.add(new ClearRepositoryOperation());
		StringWriter writer = new StringWriter();
		taWriter.setReductionFlag(false);
		taWriter.serialize(operations, writer);
		System.out.println("two, non optimized clear repos");
		System.out.println(writer);
		assertTrue(taWriter.getClearCount() == 2);
		//assertTrue(taWriter.getClearGroupCount() == 2);
	}
	
	public void testOptimizeTwoClears() throws Exception
	{
		// 7. test
		OperationList operations = new OperationList();
		operations.add(new ClearRepositoryOperation());
		operations.add(new ClearRepositoryOperation());

		StringWriter writer = new StringWriter();
		taWriter.setReductionFlag(true);
		taWriter.serialize(operations, writer);
		System.out.println("one optimized clear repo");
		System.out.println(writer);		
		assertTrue(taWriter.getClearCount() == 1);
		//assertTrue(taWriter.getClearGroupCount() == 1);
	}
	
	public void testClearContext() throws Exception
	{
		// 8. test	
		OperationList operations = new OperationList();
		URI context = new URIImpl("urn:ctx4");
		operations.add(new ClearContextOperation(context));
		StringWriter writer = new StringWriter();
		taWriter.serialize(operations, writer);
		System.out.println("clear urn:ctx4");
		System.out.println(writer);
		assert(taWriter.getClearContextCount() == 1);
		assert(taWriter.getClearContextGroupCount() == 1);
	}
	
	public void testListEquals()
	{
		OperationList list = createOperationsA();
		assertTrue(list.equals(list));
	}
	
	public void testSerializeAndParseA() throws Exception {
		OperationList a = createOperationsA();
		CharArrayWriter writer = new CharArrayWriter();
		taWriter.setReductionFlag(true);
		taWriter.serialize(a, writer);
		CharArrayReader in = new CharArrayReader(writer.toCharArray());
		OperationList after = taReader.parseTransactionFromSerialization(in);
		// check on system.out
		System.out.println("serialized version of A");
		System.out.println(writer.toString());
		assertTrue(a.equalsOptimizedVersion(after));
	}
	
	private OperationList createOperationsA() {		
		Statement stm1 = new ContextStatementImpl(new URIImpl("urn:subj1"), new URIImpl("urn:pred1"), new URIImpl("urn:obj1"), new URIImpl("urn:ctx1"));
		Statement stm2 = new ContextStatementImpl(new URIImpl("urn:subj2"), new URIImpl("urn:pred2"), new URIImpl("urn:obj2"), new URIImpl("urn:ctx2"));
		Statement stm3 = new ContextStatementImpl(new URIImpl("urn:subj3"), new URIImpl("urn:pred3"), new URIImpl("urn:obj3"), new URIImpl("urn:ctx3"));
		Statement stm5 = new ContextStatementImpl(new URIImpl("urn:subj4"), new URIImpl("urn:pred4"), new URIImpl("urn:obj4"), null);
		
		OperationList operations = 
			new OperationList();

		operations.add(new ClearRepositoryOperation());
		operations.add(new AddStatementOperation(stm1));
		operations.add(new AddStatementOperation(stm2));
		operations.add(new AddStatementOperation(stm3));
		operations.add(new RemoveStatementOperation(stm1));
		operations.add(new AddStatementOperation(stm5));
		operations.add(new ClearRepositoryOperation());
		operations.add(new ClearRepositoryOperation());
		operations.add(new ClearRepositoryOperation());
		return operations;
	}
	
	/**
	 * Serialize a transaction with an operation list. Use this serialization for 
	 * the TransactionReader and compare the resulting operation list with the 
	 * initial operation list for equality.
	 */
	/*public void testReaderAndWriter() throws Exception {
		ArrayList<TransactionOperation> operations = _getOperations();
		TransactionWriter taWriter = new TransactionWriter(operations);
		String serialization = taWriter.write().toString();
		TransactionRDFHandler handler = new TransactionRDFHandler();
		TransactionReader reader = new TransactionReader(handler);
		// TODO: kl�ren was mit baseURI gemeint ist...
		reader.parse(new StringReader(serialization), null);		
		ArrayList<TransactionOperation> operations2 = reader.getOperations();
		assertTrue(operations.equals(operations2));
		// TODO: nur Parser testen indem fehlerhafte Transactionen als Strings �bergeben werden (Testen der jeweiligen Exceptions!)
		// TODO: mehrere TestCases schreiben. 1. nur Parser (Exceptions checken) 2. Writer und Parser 3. HTTPSail testen mit Hinzuf�gen von Opeationen und hier auch Reader / Writer testen 4. Repositories vergleichen
	}
	
	public void testTransactions() throws Exception {
		// MemoryStore
		Transaction ta1 = _repositoryDirect.getSail().startTransaction();
		_executeOperations(ta1);
		ta1.commit();
		// create serialized transaction
		Transaction ta2 = _httpSail.startTransaction();
		_executeOperations(ta2);
		// do NOT ta2.commit(); but de-serialize
		String serialzed = ((HTTPTransaction) ta2).getSerializedTransaction();
		// read with SAX
		SimpleSAXParser saxParser = new SimpleSAXParser();
		TransactionReader reader = new TransactionReader();
		saxParser.setListener(reader);
		saxParser.parse(new StringReader(serialzed));
				
//		Transaction ta2 = _repositoryTA.getSail().startTransaction();
		reader.executeOperations(ta2);
		ta2.commit();
		
		// check if both repositories have the same content
		// TODO: erstmal nur die Anzahl testen
		//_repositoryTA.getSail().
		// TODO: wie machen? equals ist nicht implementiert... alle von memory 
		//		assertTrue(transacted.equals(direct));
		
	}
	*/
	
}
