/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.protocol.transaction;

import org.xml.sax.helpers.DefaultHandler;

class TransactionSAXHandler extends DefaultHandler {

	//	
	//	
	// /**
	// * ordered set of operations building a transaction
	// */
	// private OperationList _operationlist;
	//	
	// /**
	// * depth in the xml tree
	// */
	// private int _depth;
	// /**
	// * start or end element flag
	// */
	// private boolean _startElement;
	// /**
	// * current element
	// */
	// private String _current;
	// /**
	// * previous element
	// */
	// private String _previous;
	// /**
	// * current attributes
	// */
	// private Attributes _attributes;
	// /**
	// * current content
	// */
	// private String _content;
	// /**
	// * if true control is passed to provided rdf parser
	// */
	// private boolean _rdfParserFlag;
	// private TransactionRDFParser _rdfParser;
	// private StringWriter _rdfSerialization;
	// private enum ContentType {StartTag, EndTag, Content};
	//	
	// private boolean _isContextOperation;
	//	
	// public TransactionSAXHandler() {
	// _depth = 0;
	// _rdfParser = rdfParser;
	// _operationlist = new ArrayList<TransactionOperation>();
	// _current = null;
	// _previous = null;
	// _isContextOperation = false;
	// _rdfSerialization = null;
	// _attributes = null;
	// }
	//	
	// /**
	// * @return the parsed transaction from the provided serialization
	// */
	// public OperationList getOperationList() {
	// return _operationlist;
	// }
	//	
	// public void characters(char[] ch, int start, int length)
	// throws SAXException {
	// _attributes = null;
	// // handle characters and construct content
	// StringBuffer content = new StringBuffer();
	// content.append(ch, start, length);
	// _content = content.toString();
	// if (_rdfParserFlag) {
	// _collect(ContentType.Content);
	// }
	// else {
	// if (_isContextOperation) {
	// ContextOperation operation = new ContextOperation(_previous,
	// new URIImpl(content.toString()));
	// _operationlist.add(operation);
	// _isContextOperation = false;
	// }
	// }
	// }
	//
	// public void endElement(String uri, String localName, String qName)
	// throws SAXException {
	// _depth--;
	// _startElement = false;
	// _attributes = null;
	// _content = null;
	// _current = localName;
	// if (_depth <= 2) {
	// _rdfParserFlag = false;
	// }
	// if (_rdfParserFlag) {
	// _collect(ContentType.EndTag);
	// }
	// // elements forming rdf serialization completely collected, hand on to
	// rdf
	// // parser
	// else {
	// _parseWithRDFParser();
	// _rdfSerialization = null;
	// }
	// }
	//
	// // TODO: herausfinden wie empty tags behandelt werden...
	// public void startElement(String uri, String localName, String qName,
	// Attributes atts) throws SAXException {
	// _current = localName;
	// _attributes = atts;
	// // detect empty tags
	// boolean isEmptyTag = false;
	// if (!_startElement) _depth++;
	// else isEmptyTag = true;
	// _startElement = true;
	// // collect the start element
	// if (_rdfParserFlag) {
	// _collect(TransactionSAXHandler.ContentType.StartTag);
	// }
	// // continue handling of elements
	// else {
	// // on this depth in tree are the transaction operations defined
	// if (_depth == 2) {
	// _handleDepth2(isEmptyTag);
	// }
	// else if (_depth == 3) {
	// // store name of last element
	// _previous = _current;
	// _handleDepth3();
	// }
	// }
	// }
	//	
	// private void _handleDepth2(boolean isEmptyTag) {
	// // simple operation identified (not associated with a statement or
	// // context)
	// if (isEmptyTag) {
	// SimpleOperation operation = new SimpleOperation(_current);
	// _operationlist.add(operation);
	// }
	// else if (_current.equals("add") |
	// _current.equals("remove")) {
	// _rdfParserFlag = true;
	// }
	// }
	//	
	// private void _handleDepth3() {
	// // handle context operation
	// if (_current.equals("uri")) {
	// _isContextOperation = true;
	// }
	// }
	//	
	// private void _collect(TransactionSAXHandler.ContentType type) {
	// if (_rdfSerialization == null) {
	// _rdfSerialization = new StringWriter();
	// }
	// if (type == TransactionSAXHandler.ContentType.StartTag) {
	// String element = _current;
	// // handle attributes
	// String attributes = "";
	// if (_attributes != null & _attributes.getLength() > 0) {
	// attributes = " ";
	// int size = _attributes.getLength();
	// for (int i = 0; i < size; i++) {
	// attributes += _attributes.getLocalName(i) + "=\""
	// + _attributes.getValue(i) + "\" ";
	// }
	// }
	// _rdfSerialization.append("<" + element + attributes + ">");
	// }
	// else if (type == TransactionSAXHandler.ContentType.EndTag) {
	// _rdfSerialization.append("</" + _current + ">");
	// }
	// // content
	// else {
	// _rdfSerialization.append(_content);
	// }
	// }
	//	
	// // TODO: BaseURI ????
	// private void _parseWithRDFParser() {
	// // get InputStream
	// String s = _rdfSerialization.toString();
	// InputStream in = new InputSource(new StringReader(s)).getByteStream();
	// try {
	// // parse rdf serialization and add operations to entire operation list
	// _rdfParser.parse(in, null);
	// TransactionRDFHandler handler = _rdfParser.getTransactionRDFHandler();
	// ArrayList<TransactionOperation> operations = handler.getOperations();
	// for (TransactionOperation op : operations) {
	// _operationlist.add(op);
	// }
	// }
	// catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// catch (RDFParseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// catch (RDFHandlerException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//			
	// }

}
