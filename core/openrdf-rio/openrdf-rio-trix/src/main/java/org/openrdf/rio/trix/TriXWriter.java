/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import info.aduna.xml.XMLWriter;


import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import static org.openrdf.rio.trix.TriXConstants.*;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in <a
 * href="http://www.w3.org/2004/03/trix/">TriX format</a>.
 */
public class TriXWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private XMLWriter _xmlWriter;

	private boolean _writingStarted;

	private boolean _inActiveContext;

	private Resource _currentContext;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriXWtier. Note that, before this writer can be used, an
	 * OutputStream or Writer needs to be supplied to it using
	 * {@link #setOutputStream(OutputStream)} or {@link #setWriter(Writer)}.
	 */
	public TriXWriter() {
		_writingStarted = false;
		_inActiveContext = false;
		_currentContext = null;
	}

	/**
	 * Creates a new TriXWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF/XML document to.
	 */
	public TriXWriter(OutputStream out) {
		this();
		setOutputStream(out);
	}

	/**
	 * Creates a new TriXWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the RDF/XML document to.
	 */
	public TriXWriter(Writer writer) {
		this();
		setWriter(writer);
	}
	
	public TriXWriter(XMLWriter xmlWriter) {
		this();
		_xmlWriter = xmlWriter;
		_xmlWriter.setPrettyPrint(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFWriter.getRDFFormat()
	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	public void setOutputStream(OutputStream out) {
		_xmlWriter = new XMLWriter(out);
		_xmlWriter.setPrettyPrint(true);
	}

	public void setWriter(Writer writer) {
		_xmlWriter = new XMLWriter(writer);
		_xmlWriter.setPrettyPrint(true);
	}

	// implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
		if (_writingStarted) {
			throw new RDFHandlerException("Document writing has already started");
		}

		try {
			_xmlWriter.startDocument();

			_xmlWriter.setAttribute("xmlns", NAMESPACE);
			_xmlWriter.startTag(ROOT_TAG);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			_writingStarted = true;
		}
	}

	// implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RDFHandlerException("Document writing has not yet started");
		}

		try {
			if (_inActiveContext) {
				_xmlWriter.endTag(CONTEXT_TAG);
				_inActiveContext = false;
				_currentContext = null;
			}
			_xmlWriter.endTag(ROOT_TAG);
			_xmlWriter.endDocument();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			_writingStarted = false;
		}
	}

	// implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String name) {
		// ignore
	}

	// implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RDFHandlerException("Document writing has not yet been started");
		}

		try {
			Resource context = st.getContext();

			if (_inActiveContext && !_contextsEquals(context, _currentContext)) {
				// Close currently active context
				_xmlWriter.endTag(CONTEXT_TAG);
				_inActiveContext = false;
			}

			if (!_inActiveContext) {
				// Open new context
				_xmlWriter.startTag(CONTEXT_TAG);

				if (context != null) {
					_writeValue(context);
				}

				_currentContext = context;
				_inActiveContext = true;
			}

			_xmlWriter.startTag(TRIPLE_TAG);

			_writeValue(st.getSubject());
			_writeValue(st.getPredicate());
			_writeValue(st.getObject());

			_xmlWriter.endTag(TRIPLE_TAG);
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
			_xmlWriter.comment(comment);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Writes out the XML-representation for the supplied value.
	 */
	private void _writeValue(Value value)
		throws IOException, RDFHandlerException
	{
		if (value instanceof URI) {
			URI uri = (URI)value;
			_xmlWriter.textElement(URI_TAG, uri.toString());
		}
		else if (value instanceof BNode) {
			BNode bNode = (BNode)value;
			_xmlWriter.textElement(BNODE_TAG, bNode.getID());
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;
			URI datatype = literal.getDatatype();

			if (datatype != null) {
				_xmlWriter.setAttribute(DATATYPE_ATT, datatype.toString());
				_xmlWriter.textElement(TYPED_LITERAL_TAG, literal.getLabel());
			}
			else {
				String language = literal.getLanguage();
				if (language != null) {
					_xmlWriter.setAttribute(LANGUAGE_ATT, language);
				}
				_xmlWriter.textElement(PLAIN_LITERAL_TAG, literal.getLabel());
			}
		}
		else {
			throw new RDFHandlerException("Unknown value type: " + value.getClass());
		}
	}

	private static final boolean _contextsEquals(Resource context1, Resource context2) {
		if (context1 == null) {
			return context2 == null;
		}
		else {
			return context1.equals(context2);
		}
	}
}
