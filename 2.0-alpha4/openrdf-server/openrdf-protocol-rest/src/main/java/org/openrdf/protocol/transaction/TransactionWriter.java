/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.protocol.transaction.operations.ClearContextOperation;
import org.openrdf.protocol.transaction.operations.StatementOperation;
import org.openrdf.protocol.transaction.operations.TransactionOperation;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.trix.TriXWriter;


/**
 * Serialization of a transaction
 */
public class TransactionWriter {

	/**
	 * counters for testing purposes
	 */
	private int _triXCount;

	private int _addCount;

	private int _removeCount;

	private int _clearCount;

	private int _clearContextCount;

	private int _addGroupCount;

	private int _removeGroupCount;

	private int _clearGroupCount;

	private int _clearContextGroupCount;

	/**
	 * reduce multiple susccessive operations that are equal (have the same
	 * meaning, for example multiple clear())
	 */
	private boolean _reduceOperations;

	private TransactionXMLWriter _xmlWriter;

	/**
	 * the writer we are writing to. Used by _xmlWriter and embedded XMLWriters
	 * for TRIX
	 */
	private Writer _writer;

	public TransactionWriter() {
		// by default do not reduce mutiple successive operations which are not
		// associated with a statement or a context
		_reduceOperations = false;
	}

	/**
	 * serialize the passed list of operations to the passed writer.
	 * 
	 * @param operations
	 *        the operations
	 * @param writer
	 *        the writer to write to
	 * @throws TransactionSerializationException
	 *         if the serialization fails (should not happen)
	 * @throws IllegalArgumentException
	 *         when one of the parameters is null
	 */
public void serialize(List<TransactionOperation> operations, Writer writer) 
		throws TransactionSerializationException, IllegalArgumentException {
		if (operations == null) {
			throw new IllegalArgumentException("Operation list is null");
		}
		if (operations.size() == 0) {
			throw new TransactionSerializationException("Operation list can't be "+
					"empty");
		}
		if (writer == null)
			throw new IllegalArgumentException("writer is null");
		_writer = writer;
		
		// construct xml writer for serialization
		_xmlWriter = new TransactionXMLWriter(writer);
		_xmlWriter.setPrettyPrint(true);	
		// counters are only for testing purposes (to test assertions)
		_initCounter();
		try {			
			boolean writeXMLDeclaration = true;
			_xmlWriter.startDocument(writeXMLDeclaration);
			_xmlWriter.startTag("transaction");		
			/**
			 * helper field, stores previous operation
			 */ 
			// TransactionOperation _previousOp = null;
			
			// iterate all operations and group same successive statement-based
			// operations in one embedded TriX document
			// multipleStatements stores triples for a group of operations
			// the operation to run on them is the _previousOp
			// - Operations are not run inside the current loop, they are run in
			// the next loop.
			// ArrayList<Statement> multipleStatements = null;
			int i = 0;
			while (i< operations.size()) {
				TransactionOperation op = operations.get(i);
				_increaseCounter(op.getOperationXMLElementName());
				
				// group muliple successive statement operations of same type
				if (op instanceof StatementOperation)
				{
					StatementOperation sop = (StatementOperation)op;
					ArrayList<Statement> multipleStatements = new ArrayList<Statement>(1);
					multipleStatements.add(sop.getStatement());
					// peek for more statementoperations of the same class
					i++;
					while ((i<operations.size()) && operations.get(i).getClass().equals(op.getClass()))
					{
						_increaseCounter(op.getOperationXMLElementName());
						sop = (StatementOperation)operations.get(i);
						multipleStatements.add(sop.getStatement());
						i++;
					}
					_processOperation(op, multipleStatements);
				} else 
				{
					// for the other classes: step further if the next operation is equal()
					i++; 
					while (_reduceOperations && (i<operations.size()) && (operations.get(i).equals(op)))
					{
						i++;
					}
					_processOperation(op, null);					
				}
			}
			_xmlWriter.endTag("transaction");
			_xmlWriter.endDocument();
		}
		catch (IOException e) {
			throw new TransactionSerializationException("Problems occured while " +
					"construction the serialization");
		}
		catch (RDFHandlerException e) {
			throw new TransactionSerializationException("Problems occured while " +
					"construction the TriX serialization to embed");
		}
	}
	/**
	 * process operation(s). serialize the operations. If this operation runs on
	 * multiple statements, do so.
	 * 
	 * @param op
	 *        the operation to serialize
	 * @param statements
	 *        multiple statements if this was a statementop
	 */
	private void _processOperation(TransactionOperation op, ArrayList<Statement> statements)
		throws IOException, RDFHandlerException
	{
		int indentLevel = _xmlWriter.getIndentLevel();
		// check with first operation in group which operation it is about
		if (op instanceof StatementOperation) {
			// write operation tag
			_xmlWriter.startTag(op.getOperationXMLElementName());
			// embed TriX serialization
			// work directly on the String-Writer to embed serialization into
			// XML document
			_triXSerialization(statements, indentLevel);
			_triXCount++;
			_xmlWriter.endTag(op.getOperationXMLElementName());
			_increaseGroupCounter(op.getOperationXMLElementName());
		}
		else if (op instanceof ClearContextOperation) {
			_xmlWriter.startTag(op.getOperationXMLElementName());
			_xmlWriter.startTag("uri");
			_xmlWriter.text(((ClearContextOperation)op).getContext().toString());
			_xmlWriter.endTag("uri");
			_xmlWriter.endTag(op.getOperationXMLElementName());
			_increaseGroupCounter(op.getOperationXMLElementName());
		}
		// simple operation (serialize as empty tags)
		else {
			_xmlWriter.emptyElement(op.getOperationXMLElementName());
		}
	}

	// TODO: verschiedene Serialisierungen erm�glichen... diese Methode abstract
	// machen

	/**
	 * Uses the TriXWriter to serialize the provided statements
	 * 
	 * @param statements
	 *        list of triples
	 * @param indentLevel
	 *        this is for correct indention that this TriX serialization fits
	 *        into the complete XML document
	 * @return TriX document / serialization of the provided statements
	 * @throws RDFHandlerException
	 */
	private void _triXSerialization(ArrayList<Statement> statements, int indentLevel)
		throws RDFHandlerException
	{
		TransactionXMLWriter xmlWriter = new TransactionXMLWriter(_writer, indentLevel + 1);
		TriXWriter triXWriter = new TriXWriter(xmlWriter);
		triXWriter.startRDF();
		for (Statement stm : statements) {
			if (stm != null) {
				triXWriter.handleStatement(stm);
			}
		}
		triXWriter.endRDF();
	}

	private void _initCounter() {
		_triXCount = 0;
		_addCount = 0;
		_removeCount = 0;
		_clearCount = 0;
		_clearContextCount = 0;
		_addGroupCount = 0;
		_removeGroupCount = 0;
		_clearGroupCount = 0;
		_clearContextGroupCount = 0;
	}

	private void _increaseCounter(String name) {
		if (name.equals("add"))
			_addCount++;
		else if (name.equals("remove"))
			_removeCount++;
		else if (name.equals("clear"))
			_clearCount++;
		else if (name.equals("clearcontext"))
			_clearContextCount++;
	}

	private void _increaseGroupCounter(String name) {
		if (name.equals("add"))
			_addGroupCount++;
		else if (name.equals("remove"))
			_removeGroupCount++;
		else if (name.equals("clear"))
			_clearGroupCount++;
		else if (name.equals("clearcontext"))
			_clearContextGroupCount++;
	}

	public void setReductionFlag(boolean flag) {
		_reduceOperations = flag;
	}

	public boolean isReductionFlagSet() {
		return _reduceOperations;
	}

	public int getNumberTriXSerializations() {
		return _triXCount;
	}

	public int getAddCount() {
		return _addCount;
	}

	public int getClearCount() {
		return _clearCount;
	}

	public int getClearContextCount() {
		return _clearContextCount;
	}

	public int getRemoveCount() {
		return _removeCount;
	}

	public int getAddGroupCount() {
		return _addGroupCount;
	}

	public int getClearGroupCount() {
		return _clearGroupCount;
	}

	public int getClearContextGroupCount() {
		return _clearContextGroupCount;
	}

	public int getRemoveGroupCount() {
		return _removeGroupCount;
	}

	public int getTriXCount() {
		return _triXCount;
	}
}
