/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Parses the transaction using Sesame's SAX helper objects and an internal RDF
 * TRIX reader.
 */
class TransactionSAXParser extends SimpleSAXAdapter {

	private ValueFactory _valueFactory;

	private List<TransactionOperation> _txn;

	private List<Value> _parsedValues = new ArrayList<Value>();

	public TransactionSAXParser() {
		this(new ValueFactoryImpl());
	}

	public TransactionSAXParser(ValueFactory valueFactory) {
		_valueFactory = valueFactory;
	}

	/**
	 * get the parsed transaction
	 * 
	 * @return the parsed transaction
	 */
	public Collection<TransactionOperation> getTxn() {
		return _txn;
	}

	@Override
	public void startDocument()
		throws SAXException
	{
		_txn = new ArrayList<TransactionOperation>();
	}

	@Override
	public void startTag(String tagName, Map<String, String> atts, String text)
		throws SAXException
	{
		if (TransactionXMLConstants.URI_TAG.equals(tagName)) {
			_parsedValues.add(_valueFactory.createURI(text));
		}
		else if (TransactionXMLConstants.BNODE_TAG.equals(tagName)) {
			_parsedValues.add(_valueFactory.createBNode(text));
		}
		else if (TransactionXMLConstants.LITERAL_TAG.equals(tagName)) {
			String lang = atts.get(TransactionXMLConstants.LANG_ATT);
			String datatype = atts.get(TransactionXMLConstants.DATATYPE_ATT);

			Literal lit;
			if (lang != null) {
				lit = _valueFactory.createLiteral(text, lang);
			}
			else if (datatype != null) {
				URI dtURI = _valueFactory.createURI(datatype);
				lit = _valueFactory.createLiteral(text, dtURI);
			}
			else {
				lit = _valueFactory.createLiteral(text);
			}

			_parsedValues.add(lit);
		}
		else if (TransactionXMLConstants.NULL_TAG.equals(tagName)) {
			_parsedValues.add(null);
		}
		else if (TransactionXMLConstants.SET_NAMESPACE_TAG.equals(tagName)) {
			String prefix = atts.get(TransactionXMLConstants.PREFIX_ATT);
			String name = atts.get(TransactionXMLConstants.NAME_ATT);
			_txn.add(new SetNamespaceOperation(prefix, name));
		}
		else if (TransactionXMLConstants.REMOVE_NAMESPACE_TAG.equals(tagName)) {
			String prefix = atts.get(TransactionXMLConstants.PREFIX_ATT);
			_txn.add(new RemoveNamespaceOperation(prefix));
		}
	}

	@Override
	public void endTag(String tagName)
		throws SAXException
	{
		if (TransactionXMLConstants.ADD_STATEMENT_TAG.equals(tagName)) {
			_txn.add(_createAddStatementOperation());
		}
		else if (TransactionXMLConstants.REMOVE_STATEMENTS_TAG.equals(tagName)) {
			_txn.add(_createRemoveStatementsOperation());
		}
		else if (TransactionXMLConstants.CLEAR_TAG.equals(tagName)) {
			_txn.add(_createClearOperation());
		}
	}

	private TransactionOperation _createClearOperation()
		throws SAXException
	{
		ClearOperation result = null;

		Resource[] contexts = createContexts(0);
		_parsedValues.clear();

		result = new ClearOperation(contexts);
		return result;
	}

	private TransactionOperation _createAddStatementOperation()
		throws SAXException
	{
		if (_parsedValues.size() != 4) {
			throw new SAXException("Four values required for AddStatementOperation, found: "
					+ _parsedValues.size());
		}

		try {
			Resource subject = (Resource)_parsedValues.get(0);
			URI predicate = (URI)_parsedValues.get(1);
			Value object = _parsedValues.get(2);
			Resource[] contexts = createContexts(3);

			_parsedValues.clear();

			if (subject == null || predicate == null || object == null) {
				throw new SAXException(
						"Subject, predicate and object cannot be null for an AddStatementOperation");
			}
			return new AddStatementOperation(subject, predicate, object, contexts);
		}
		catch (ClassCastException e) {
			throw new SAXException("Invalid argument(s) for AddStatementOperation", e);
		}
	}

	private TransactionOperation _createRemoveStatementsOperation()
		throws SAXException
	{
		if (_parsedValues.size() != 3) {
			throw new SAXException("Three values required for RemoveStatementsOperation, found: "
					+ _parsedValues.size());
		}

		try {
			Resource subject = (Resource)_parsedValues.get(0);
			URI predicate = (URI)_parsedValues.get(1);
			Value object = _parsedValues.get(2);
			Resource[] contexts = createContexts(3);

			_parsedValues.clear();

			return new RemoveStatementsOperation(subject, predicate, object, contexts);
		}
		catch (ClassCastException e) {
			throw new SAXException("Invalid argument(s) for RemoveNamedContextStatementsOperation", e);
		}
	}

	private Resource[] createContexts(int valuesToSkip)
		throws SAXException
	{
		Resource[] result = null;

		List<Resource> contexts = new ArrayList<Resource>();
		while (_parsedValues.size() > valuesToSkip) {
			Value contextCandidate = _parsedValues.get(valuesToSkip);
			if (contextCandidate == null || contextCandidate instanceof Resource) {
				contexts.add((Resource)contextCandidate);
			}
			else {
				throw new SAXException("ClearOperation only supports Resources as context, found: "
						+ contextCandidate.getClass());
			}
			valuesToSkip++;
		}

		result = contexts.toArray(new Resource[contexts.size()]);

		return result;
	}
}
