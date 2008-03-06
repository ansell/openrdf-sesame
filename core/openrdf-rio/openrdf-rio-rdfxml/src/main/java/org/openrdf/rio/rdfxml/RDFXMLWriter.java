/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.util.xml.XMLUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * XML-serialized RDF format.
 */
public class RDFXMLWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected Writer _writer;

	protected Map<String, String> _namespaceTable;

	protected boolean _writingStarted;

	protected boolean _headerWritten;

	protected Resource _lastWrittenSubject;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFXMLWriter. Note that, before this writer can be used, an
	 * OutputStream or Writer needs to be supplied to it using
	 * {@link #setOutputStream(OutputStream)} or {@link #setWriter(Writer)}.
	 */
	public RDFXMLWriter() {
		_namespaceTable = new LinkedHashMap<String, String>();
		_writingStarted = false;
		_headerWritten = false;
		_lastWrittenSubject = null;
	}

	/**
	 * Creates a new RDFXMLWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF/XML document to.
	 */
	public RDFXMLWriter(OutputStream out) {
		this();
		setOutputStream(out);
	}

	/**
	 * Creates a new RDFXMLWriter that will write to the supplied Writer.
	 * 
	 * @param out
	 *        The Writer to write the RDF/XML document to.
	 */
	public RDFXMLWriter(Writer out) {
		this();
		setWriter(out);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFWriter.getRDFFormat()
	public RDFFormat getRDFFormat() {
		return RDFFormat.RDFXML;
	}

	public void setOutputStream(OutputStream out) {
		try {
			setWriter(new OutputStreamWriter(out, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void setWriter(Writer writer) {
		_writer = writer;
	}

	// implements RDFHandler.startRDF()
	public void startRDF() {
		if (_writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}
		_writingStarted = true;
	}

	protected void _writeHeader()
		throws IOException
	{
		try {
			// This export format needs the RDF namespace to be defined, add a
			// prefix for it if there isn't one yet.
			_setNamespace("rdf", RDF.NAMESPACE, false);

			_writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

			_writeStartOfStartTag(RDF.NAMESPACE, "RDF");

			for (Map.Entry<String, String> entry : _namespaceTable.entrySet()) {
				String name = entry.getKey();
				String prefix = entry.getValue();

				_writeNewLine();
				_writeIndent();
				_writer.write("xmlns");
				if (prefix.length() > 0) {
					_writer.write(':');
					_writer.write(prefix);
				}
				_writer.write("=\"");
				_writer.write(XMLUtil.escapeDoubleQuotedAttValue(name));
				_writer.write("\"");
			}

			_writeEndOfStartTag();

			_writeNewLine();
		}
		finally {
			_headerWritten = true;
		}
	}

	// implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}

		try {
			if (!_headerWritten) {
				_writeHeader();
			}

			_flushPendingStatements();

			_writeNewLine();
			_writeEndTag(RDF.NAMESPACE, "RDF");

			_writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			_writingStarted = false;
			_headerWritten = false;
		}
	}

	// implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String name) {
		_setNamespace(prefix, name, true);
	}

	protected void _setNamespace(String prefix, String name, boolean fixedPrefix) {
		if (_headerWritten) {
			// Header containing namespace declarations has already been written
			return;
		}

		if (!_namespaceTable.containsKey(name)) {
			// Try to give it the specified prefix
			if (!_namespaceTable.containsValue(prefix)) {
				_namespaceTable.put(name, prefix);
			}
			else {
				// specified prefix is already used for another namespace
				if (fixedPrefix) {
					throw new IllegalArgumentException("prefix already in use: " + prefix);
				}
				else {
					// specified prefix is already taken, append a number to
					// generate a unique prefix
					int number = 1;

					while (_namespaceTable.containsValue(prefix + number)) {
						number++;
					}

					_namespaceTable.put(RDF.NAMESPACE, prefix + number);
				}
			}
		}
	}

	// implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();

		// Verify that an XML namespace-qualified name can be created for the
		// predicate
		String predString = pred.toString();
		int predSplitIdx = XMLUtil.findURISplitIndex(predString);
		if (predSplitIdx == -1) {
			throw new RDFHandlerException("Unable to create XML namespace-qualified name for predicate: "
					+ predString);
		}

		String predNamespace = predString.substring(0, predSplitIdx);
		String predLocalName = predString.substring(predSplitIdx);

		try {
			if (!_headerWritten) {
				_writeHeader();
			}

			// SUBJECT
			if (!subj.equals(_lastWrittenSubject)) {
				_flushPendingStatements();

				// Write new subject:
				_writeNewLine();
				_writeStartOfStartTag(RDF.NAMESPACE, "Description");
				if (subj instanceof BNode) {
					BNode bNode = (BNode)subj;
					_writeAttribute(RDF.NAMESPACE, "nodeID", bNode.getID());
				}
				else {
					URI uri = (URI)subj;
					_writeAttribute(RDF.NAMESPACE, "about", uri.toString());
				}
				_writeEndOfStartTag();
				_writeNewLine();

				_lastWrittenSubject = subj;
			}

			// PREDICATE
			_writeIndent();
			_writeStartOfStartTag(predNamespace, predLocalName);

			// OBJECT
			if (obj instanceof Resource) {
				Resource objRes = (Resource)obj;

				if (objRes instanceof BNode) {
					BNode bNode = (BNode)objRes;
					_writeAttribute(RDF.NAMESPACE, "nodeID", bNode.getID());
				}
				else {
					URI uri = (URI)objRes;
					_writeAttribute(RDF.NAMESPACE, "resource", uri.toString());
				}

				_writeEndOfEmptyTag();
			}
			else if (obj instanceof Literal) {
				Literal objLit = (Literal)obj;

				// language attribute
				if (objLit.getLanguage() != null) {
					_writeAttribute("xml:lang", objLit.getLanguage());
				}

				// datatype attribute
				boolean isXMLLiteral = false;
				URI datatype = objLit.getDatatype();
				if (datatype != null) {
					// Check if datatype is rdf:XMLLiteral
					isXMLLiteral = datatype.equals(RDF.XMLLITERAL);

					if (isXMLLiteral) {
						_writeAttribute(RDF.NAMESPACE, "parseType", "Literal");
					}
					else {
						_writeAttribute(RDF.NAMESPACE, "datatype", datatype.toString());
					}
				}

				_writeEndOfStartTag();

				// label
				if (isXMLLiteral) {
					// Write XML literal as plain XML
					_writer.write(objLit.getLabel());
				}
				else {
					_writeCharacterData(objLit.getLabel());
				}

				_writeEndTag(predNamespace, predLocalName);
			}

			_writeNewLine();

			// Don't write </rdf:Description> yet, maybe the next statement
			// has the same subject.
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	// implements RDFWriter.writeComment(...)
	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			if (!_headerWritten) {
				_writeHeader();
			}

			_flushPendingStatements();

			_writer.write("<!-- ");
			_writer.write(comment);
			_writer.write(" -->");
			_writeNewLine();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	protected void _flushPendingStatements()
		throws IOException
	{
		if (_lastWrittenSubject != null) {
			// The last statement still has to be closed:
			_writeEndTag(RDF.NAMESPACE, "Description");
			_writeNewLine();

			_lastWrittenSubject = null;
		}
	}

	protected void _writeStartOfStartTag(String namespace, String localName)
		throws IOException
	{
		String prefix = _namespaceTable.get(namespace);

		if (prefix == null) {
			_writer.write("<");
			_writer.write(localName);
			_writer.write(" xmlns=\"");
			_writer.write(XMLUtil.escapeDoubleQuotedAttValue(namespace));
			_writer.write("\"");
		}
		else if (prefix.length() == 0) {
			// default namespace
			_writer.write("<");
			_writer.write(localName);
		}
		else {
			_writer.write("<");
			_writer.write(prefix);
			_writer.write(":");
			_writer.write(localName);
		}
	}

	protected void _writeAttribute(String attName, String value)
		throws IOException
	{
		_writer.write(" ");
		_writer.write(attName);
		_writer.write("=\"");
		_writer.write(XMLUtil.escapeDoubleQuotedAttValue(value));
		_writer.write("\"");
	}

	protected void _writeAttribute(String namespace, String attName, String value)
		throws IOException
	{
		String prefix = _namespaceTable.get(namespace);

		if (prefix == null || prefix.length() == 0) {
			throw new RuntimeException("No prefix has been declared for the namespace used in this attribute: "
					+ namespace);
		}

		_writer.write(" ");
		_writer.write(prefix);
		_writer.write(":");
		_writer.write(attName);
		_writer.write("=\"");
		_writer.write(XMLUtil.escapeDoubleQuotedAttValue(value));
		_writer.write("\"");
	}

	protected void _writeEndOfStartTag()
		throws IOException
	{
		_writer.write(">");
	}

	protected void _writeEndOfEmptyTag()
		throws IOException
	{
		_writer.write("/>");
	}

	protected void _writeEndTag(String namespace, String localName)
		throws IOException
	{
		String prefix = _namespaceTable.get(namespace);

		if (prefix == null || prefix.length() == 0) {
			_writer.write("</");
			_writer.write(localName);
			_writer.write(">");
		}
		else {
			_writer.write("</");
			_writer.write(prefix);
			_writer.write(":");
			_writer.write(localName);
			_writer.write(">");
		}
	}

	protected void _writeCharacterData(String chars)
		throws IOException
	{
		_writer.write(XMLUtil.escapeCharacterData(chars));
	}

	protected void _writeIndent()
		throws IOException
	{
		_writer.write("\t");
	}

	protected void _writeNewLine()
		throws IOException
	{
		_writer.write("\n");
	}
}
