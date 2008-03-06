/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction;

import java.util.Map;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.protocol.transaction.operations.ClearContextOperation;
import org.openrdf.protocol.transaction.operations.ClearRepositoryOperation;
import org.openrdf.protocol.transaction.operations.OperationList;
import org.openrdf.protocol.transaction.operations.RemoveStatementOperation;
import org.openrdf.protocol.transaction.operations.StatementOperation;
import org.openrdf.protocol.transaction.operations.TransactionOperation;
import org.openrdf.rio.trix.TriXConstants;
import org.openrdf.rio.trix.TriXParser;
import org.openrdf.util.xml.SimpleSAXAdapter;

/**
 * Parses the transaction using Sesame's SAX helper objects and an internal RDF
 * TRIX reader.
 */
public class TransactionSAXParser extends SimpleSAXAdapter {

	public static final Logger log = Logger
			.getLogger(TransactionSAXParser.class.getName());

	/**
	 * when parsing an operation, this will be set on
	 * {@link #startElement(String, String, String, Attributes)} and added to
	 * the _operationslist on endElement().
	 */
	TransactionOperation _currentOp = null;

	/**
	 * when parsing trix, this thing creates the operations
	 */
	TransactionRDFHandler _currentrdfhandler = null;

	OperationList _operationlist = new OperationList();

	/**
	 * when this is true, all calls are forwarded to the TrixParser inside. the
	 * parser then creates events on the TransactionEDFHandler
	 */
	boolean _parsingTrix = false;

	/**
	 * when parsing trix, this is the parser that I use to generate the
	 * trixadapter
	 */
	TriXParser _trixparser = new TriXParser();

	/**
	 * when parsing trix, this is the adapter that I forward my SAX calls to
	 */
	SimpleSAXAdapter _trixadapter = _trixparser.createSAXHandler();

	/**
	 * 
	 */
	public TransactionSAXParser() {
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endTag(String tagName) throws SAXException {
		// WHEN TRIX ENDS, END THE FORWARDING ISSUES!
		if (_parsingTrix) {
			_trixadapter.endTag(tagName);
			if (TriXConstants.ROOT_TAG.equals(tagName)) {
				// TRIX ENDS
				stopParsingTrix();
			}
		} else {
			// end an op?
			if ((_currentOp != null)
					&& (_currentOp.getOperationXMLElementName().equals(tagName))) {
				// do not add statementoperations, they are only a placeholder,
				// they are added by the _trixAdapter
				if (!(_currentOp instanceof StatementOperation))
					_operationlist.add(_currentOp);
			}

		}
	}

	/**
	 * get the parsed operationslist
	 * 
	 * @return the parsed operationslist
	 */
	public OperationList getOperationList() {
		return _operationlist;
	}

	/**
	 * I am starting a new operation. When there is still an operation open,
	 * this is an error.
	 * 
	 * @param operation
	 *            the operation to start
	 */
	private void setCurrentOp(TransactionOperation operation)
			throws SAXException {
		if (_currentOp != null)
			throw new SAXException("Encountered operation " + operation
					+ " while this was still open: " + _currentOp);
		_currentOp = operation;

	}

	@Override
	public void startDocument() throws SAXException {
		if (_parsingTrix)
			_trixadapter.startDocument();
	}

	private void startParsingTrix(
			Class<? extends StatementOperation> operationclass) {
		_parsingTrix = true;
		_currentrdfhandler = new TransactionRDFHandler(_operationlist,
				operationclass);
		try {
			_currentOp = operationclass.newInstance();
		} catch (Exception e) {
			log.warning("programming error: " + e);
			throw new RuntimeException(e);
		}
		_trixparser.setRDFHandler(_currentrdfhandler);
	}

	@Override
	public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException {
		if (_parsingTrix)
			_trixadapter.startTag(tagName, atts, text);
		else {
			if (AddStatementOperation.XMLELEMENT.equals(tagName))
				startParsingTrix(AddStatementOperation.class);
			else if (RemoveStatementOperation.XMLELEMENT.equals(tagName))
				startParsingTrix(RemoveStatementOperation.class);
			else if (ClearContextOperation.XMLELEMENT.equals(tagName)) {
				setCurrentOp(new ClearContextOperation());
			} else if (ClearRepositoryOperation.XMLELEMENT.equals(tagName)) {
				setCurrentOp(new ClearRepositoryOperation());
			} else if ("uri".equals(tagName)) {
				if ((_currentOp != null)
						&& (_currentOp instanceof ClearContextOperation)) {
					ClearContextOperation ccop = (ClearContextOperation) _currentOp;
					if (ccop.getContext() != null)
						throw new SAXException(
								"clearcontext has two 'uri' elements.");
					else
						ccop.setContext(new URIImpl(text));
				} else {
					throw new SAXException("tag 'uri' not expected here.");
				}
			}
		}
	}

	private void stopParsingTrix() {
		_parsingTrix = false;
		_currentOp = null;
		_currentrdfhandler = null;
		_trixparser.setRDFHandler(null);
	}

}
