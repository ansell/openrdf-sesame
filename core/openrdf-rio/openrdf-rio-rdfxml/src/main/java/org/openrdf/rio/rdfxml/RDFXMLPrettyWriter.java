/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Stack;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import org.openrdf.rio.RDFHandlerException;

/**
 * An extension of RDFXMLWriter that outputs a more concise form of RDF/XML. The
 * resulting output is semantically equivalent to the output of an RDFXMLWriter
 * (it produces the same set of statements), but it is usually easier to read
 * for humans.
 * <p>
 * This is a quasi-streaming RDFWriter. Statements are cached as long as the
 * striped syntax is followed (i.e. the subject of the next statement is the
 * object of the previous statement) and written to the output when the stripe
 * is broken.
 * <p>
 * The abbreviations used are <a
 * href="http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-typed-nodes">typed
 * node elements</a>, <a
 * href="http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-empty-property-elements">empty
 * property elements</a> and <a
 * href="http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-node-property-elements">striped
 * syntax</a>. Note that these abbreviations require that statements are
 * written in the appropriate order.
 * <p>
 * Striped syntax means that when the object of a statement is the subject of
 * the next statement we can nest the descriptions in each other.
 * <p>
 * Example:
 * 
 * <pre>
 *  &lt;rdf:Seq&gt;
 *     &lt;rdf:li&gt;
 *        &lt;foaf:Person&gt;
 *           &lt;foaf:knows&gt;
 *              &lt;foaf:Person&gt;
 *                &lt;foaf:mbox rdf:resource="..."/&gt;
 *              &lt;/foaf:Person&gt;
 *           &lt;/foaf:knows&gt;
 *        &lt;/foaf:Person&gt;
 *     &lt;/rdf:li&gt;
 *  &lt;/rdf:Seq&gt;
 * </pre>
 * 
 * Typed node elements means that we write out type information in the short
 * form of
 * 
 * <pre>
 * &lt;foaf:Person rdf:about="..."&gt;
 *     ...
 *  &lt;/foaf:Person&gt;
 * </pre>
 * 
 * instead of
 * 
 * <pre>
 * &lt;rdf:Description rdf:about="..."&gt;
 *    &lt;rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/&gt;
 *     ...
 *  &lt;/rdf:Description&gt;
 * </pre>
 * 
 * Empty property elements are of the form
 * 
 * <pre>
 *  &lt;foaf:Person&gt;
 *    &lt;foaf:homepage rdf:resource="http://www.cs.vu.nl/~marta"/&gt;
 *  &lt;/foaf:Person&gt;
 * </pre>
 * 
 * instead of
 * 
 * <pre>
 *  &lt;foaf:Person&gt;
 *     &lt;foaf:homepage&gt;
 *       &lt;rdf:Description rdf:about="http://www.cs.vu.nl/~marta"/&gt;
 *     &lt;foaf:homepage&gt;
 *  &lt;/foaf:Person&gt;
 * </pre>
 * 
 * @author Peter Mika (pmika@cs.vu.nl)
 */
public class RDFXMLPrettyWriter extends RDFXMLWriter implements Closeable, Flushable {

	/*-----------*
	 * Variables *
	 *-----------*/

	/*
	 * We implement striped syntax by using two stacks, one for predicates and
	 * one for subjects/objects.
	 */

	/**
	 * Stack for remembering the nodes (subjects/objects) of statements at each
	 * level.
	 */
	private Stack<Node> _nodeStack = new Stack<Node>();

	/**
	 * Stack for remembering the predicate of statements at each level.
	 */
	private Stack<URI> _predicateStack = new Stack<URI>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFXMLPrintWriter. Note that, before this writer can be
	 * used, an OutputStream or Writer needs to be supplied to it using
	 * {@link #setOutputStream(OutputStream)} or {@link #setWriter(Writer)}.
	 */
	public RDFXMLPrettyWriter() {
		super();
	}

	/**
	 * Creates a new RDFXMLPrintWriter that will write to the supplied
	 * OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF/XML document to.
	 */
	public RDFXMLPrettyWriter(OutputStream out) {
		super(out);
	}

	/**
	 * Creates a new RDFXMLPrintWriter that will write to the supplied Writer.
	 * 
	 * @param out
	 *        The Writer to write the RDF/XML document to.
	 */
	public RDFXMLPrettyWriter(Writer out) {
		super(out);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void _writeHeader()
		throws IOException
	{
		// This export format needs the RDF Schema namespace to be defined:
		_setNamespace("rdfs", RDFS.NAMESPACE, false);

		super._writeHeader();
	}

	public void flush()
		throws IOException
	{
		if (_writingStarted) {
			if (!_headerWritten) {
				_writeHeader();
			}

			_flushPendingStatements();

			_writer.flush();
		}
	}

	public void close()
		throws IOException
	{
		if (_writingStarted) {
			try {
				endRDF();
			}
			catch (RDFHandlerException e) {
				if (e.getCause() instanceof IOException) {
					throw (IOException)e.getCause();
				}
				else {
					IOException ioe = new IOException(e.getMessage());
					ioe.initCause(e);
					throw ioe;
				}
			}
			finally {
				_writer.close();
			}
		}
	}

	@Override
	protected void _flushPendingStatements()
		throws IOException
	{
		if (!_nodeStack.isEmpty()) {
			_popStacks(null);
		}
	}

	/**
	 * Write out the stacks until we find subject. If subject == null, write out
	 * the entire stack
	 * 
	 * @param newSubject
	 */
	private void _popStacks(Resource newSubject)
		throws IOException
	{
		// Write start tags for the part of the stacks that are not yet
		// written
		for (int i = 0; i < _nodeStack.size() - 1; i++) {
			Node node = _nodeStack.get(i);

			if (!node.isWritten()) {
				if (i > 0) {
					_writeIndents(i * 2 - 1);

					URI predicate = _predicateStack.get(i - 1);

					_writeStartTag(predicate.getNamespace(), predicate.getLocalName());
					_writeNewLine();
				}

				_writeIndents(i * 2);
				_writeNodeStartTag(node);
				node.setIsWritten(true);
			}
		}

		// Write tags for the top subject
		Node topNode = _nodeStack.pop();

		if (_predicateStack.isEmpty()) {
			// write out an empty subject
			_writeIndents(_nodeStack.size() * 2);
			_writeNodeEmptyTag(topNode);
			_writeNewLine();
		}
		else {
			URI topPredicate = _predicateStack.pop();

			if (!topNode.hasType()) {
				// we can use an abbreviated predicate
				_writeIndents(_nodeStack.size() * 2 - 1);
				_writeAbbreviatedPredicate(topPredicate, topNode.getValue());
			}
			else {
				// we cannot use an abbreviated predicate because the type needs to
				// written out as well

				_writeIndents(_nodeStack.size() * 2 - 1);
				_writeStartTag(topPredicate.getNamespace(), topPredicate.getLocalName());
				_writeNewLine();

				// write out an empty subject
				_writeIndents(_nodeStack.size() * 2);
				_writeNodeEmptyTag(topNode);
				_writeNewLine();

				_writeIndents(_nodeStack.size() * 2 - 1);
				_writeEndTag(topPredicate.getNamespace(), topPredicate.getLocalName());
				_writeNewLine();
			}
		}

		// Write out the end tags until we find the subject
		while (!_nodeStack.isEmpty()) {
			Node nextElement = _nodeStack.peek();

			if (nextElement.getValue().equals(newSubject)) {
				break;
			}
			else {
				_nodeStack.pop();

				// We have already written out the subject/object,
				// but we still need to close the tag
				_writeIndents(_predicateStack.size() + _nodeStack.size());

				_writeNodeEndTag(nextElement);

				if (_predicateStack.size() > 0) {
					URI nextPredicate = (URI)_predicateStack.pop();

					_writeIndents(_predicateStack.size() + _nodeStack.size());

					_writeEndTag(nextPredicate.getNamespace(), nextPredicate.getLocalName());

					_writeNewLine();
				}
			}
		}
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();

		try {
			if (!_headerWritten) {
				_writeHeader();
			}

			if (!_nodeStack.isEmpty() && !subj.equals(_nodeStack.peek().getValue())) {
				// Different subject than we had before, empty the stack
				// until we find it
				_popStacks(subj);
			}

			// Stack is either empty or contains the same subject at top

			if (_nodeStack.isEmpty()) {
				// Push subject
				_nodeStack.push(new Node(subj));
			}

			// Stack now contains at least one element
			Node topSubject = _nodeStack.peek();

			// Check if current statement is a type statement and use a typed node
			// element is possible
			// FIXME: verify that an XML namespace-qualified name can be created
			// for the type URI
			if (pred.equals(RDF.TYPE) && obj instanceof URI && !topSubject.hasType() && !topSubject.isWritten()) {
				// Use typed node element
				topSubject.setType((URI)obj);
			}
			else {
				// Push predicate and object
				_predicateStack.push(pred);
				_nodeStack.push(new Node(obj));
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Write out the opening tag of the subject or object of a statement up to
	 * (but not including) the end of the tag. Used both in _writeStartSubject
	 * and _writeEmptySubject.
	 */
	private void _writeNodeStartOfStartTag(Node node)
		throws IOException
	{
		Value value = node.getValue();

		if (node.hasType()) {
			// We can use abbreviated syntax
			_writeStartOfStartTag(node.getType().getNamespace(), node.getType().getLocalName());
		}
		else {
			// We cannot use abbreviated syntax
			_writeStartOfStartTag(RDF.NAMESPACE, "Description");
		}

		if (value instanceof URI) {
			URI uri = (URI)value;
			_writeAttribute(RDF.NAMESPACE, "about", uri.toString());
		}
		// else {
		// BNode bNode = (BNode)subj;
		// _writeAttribute(RDF.NAMESPACE, "nodeID", bNode.getID());
		// }
	}

	/**
	 * Write out the opening tag of the subject or object of a statement.
	 */
	private void _writeNodeStartTag(Node node)
		throws IOException
	{
		_writeNodeStartOfStartTag(node);
		_writeEndOfStartTag();
		_writeNewLine();
	}

	/**
	 * Write out the closing tag for the subject or object of a statement.
	 */
	private void _writeNodeEndTag(Node node)
		throws IOException
	{
		if (node.getType() != null) {
			_writeEndTag(node.getType().getNamespace(), node.getType().getLocalName());
		}
		else {
			_writeEndTag(RDF.NAMESPACE, "Description");
		}
		_writeNewLine();
	}

	/**
	 * Write out an empty tag for the subject or object of a statement.
	 */
	private void _writeNodeEmptyTag(Node node)
		throws IOException
	{
		_writeNodeStartOfStartTag(node);
		_writeEndOfEmptyTag();
	}

	/**
	 * Write out an empty property element.
	 */
	private void _writeAbbreviatedPredicate(URI pred, Value obj)
		throws IOException
	{
		_writeStartOfStartTag(pred.getNamespace(), pred.getLocalName());

		if (obj instanceof Resource) {
			Resource objRes = (Resource)obj;

			if (objRes instanceof URI) {
				URI uri = (URI)objRes;
				_writeAttribute(RDF.NAMESPACE, "resource", uri.toString());
			}
			// else {
			// BNode bNode = (BNode)objRes;
			// _writeAttribute(RDF.NAMESPACE, "nodeID", bNode.getID());
			// }

			_writeEndOfEmptyTag();
		}
		else if (obj instanceof Literal) {
			Literal objLit = (Literal)obj;

			// language attribute
			if (objLit.getLanguage() != null) {
				_writeAttribute("xml:lang", objLit.getLanguage());
			}

			// datatype attribute
			boolean isXmlLiteral = false;
			URI datatype = objLit.getDatatype();
			if (datatype != null) {
				// Check if datatype is rdf:XMLLiteral
				isXmlLiteral = datatype.equals(RDF.XMLLITERAL);

				if (isXmlLiteral) {
					_writeAttribute(RDF.NAMESPACE, "parseType", "Literal");
				}
				else {
					_writeAttribute(RDF.NAMESPACE, "datatype", datatype.toString());
				}
			}

			_writeEndOfStartTag();

			// label
			if (isXmlLiteral) {
				// Write XML literal as plain XML
				_writer.write(objLit.getLabel());
			}
			else {
				_writeCharacterData(objLit.getLabel());
			}

			_writeEndTag(pred.getNamespace(), pred.getLocalName());
		}

		_writeNewLine();
	}

	protected void _writeStartTag(String namespace, String localName)
		throws IOException
	{
		_writeStartOfStartTag(namespace, localName);
		_writeEndOfStartTag();
	}

	/**
	 * Writes <tt>n</tt> indents.
	 */
	protected void _writeIndents(int n)
		throws IOException
	{
		for (int i = 0; i < n; i++) {
			_writeIndent();
		}
	}

	/*------------------*
	 * Inner class Node *
	 *------------------*/

	private static class Node {

		private Value _value;

		// type == null means that we use <rdf:Description>
		private URI _type = null;

		private boolean _isWritten = false;

		/**
		 * Creates a new Node for the supplied Value.
		 */
		public Node(Value value) {
			_value = value;
		}

		public Value getValue() {
			return _value;
		}

		public void setType(URI type) {
			_type = type;
		}

		public URI getType() {
			return _type;
		}

		public boolean hasType() {
			return _type != null;
		}

		public void setIsWritten(boolean isWritten) {
			_isWritten = isWritten;
		}

		public boolean isWritten() {
			return _isWritten;
		}
	}
}
