/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

import static org.openrdf.rio.trix.TriXConstants.BNODE_TAG;
import static org.openrdf.rio.trix.TriXConstants.CONTEXT_TAG;
import static org.openrdf.rio.trix.TriXConstants.DATATYPE_ATT;
import static org.openrdf.rio.trix.TriXConstants.LANGUAGE_ATT;
import static org.openrdf.rio.trix.TriXConstants.NAMESPACE;
import static org.openrdf.rio.trix.TriXConstants.PLAIN_LITERAL_TAG;
import static org.openrdf.rio.trix.TriXConstants.ROOT_TAG;
import static org.openrdf.rio.trix.TriXConstants.TRIPLE_TAG;
import static org.openrdf.rio.trix.TriXConstants.TYPED_LITERAL_TAG;
import static org.openrdf.rio.trix.TriXConstants.URI_TAG;

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

/**
 * An implementation of the RDFWriter interface that writes RDF documents in <a
 * href="http://www.w3.org/2004/03/trix/">TriX format</a>.
 * 
 * @author Arjohn Kampman
 */
public class TriXWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String baseURI;

	private XMLWriter xmlWriter;

	private boolean writingStarted;

	private boolean inActiveContext;

	private Resource currentContext;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriXWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF/XML document to.
	 */
	public TriXWriter(OutputStream out) {
		this(new XMLWriter(out));
	}

	/**
	 * Creates a new TriXWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the RDF/XML document to.
	 */
	public TriXWriter(Writer writer) {
		this(new XMLWriter(writer));
	}

	protected TriXWriter(XMLWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
		this.xmlWriter.setPrettyPrint(true);

		writingStarted = false;
		inActiveContext = false;
		currentContext = null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public void startRDF()
		throws RDFHandlerException
	{
		if (writingStarted) {
			throw new RDFHandlerException("Document writing has already started");
		}

		try {
			xmlWriter.startDocument();

			xmlWriter.setAttribute("xmlns", NAMESPACE);
			xmlWriter.startTag(ROOT_TAG);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = true;
		}
	}

	public void endRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RDFHandlerException("Document writing has not yet started");
		}

		try {
			if (inActiveContext) {
				xmlWriter.endTag(CONTEXT_TAG);
				inActiveContext = false;
				currentContext = null;
			}
			xmlWriter.endTag(ROOT_TAG);
			xmlWriter.endDocument();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = false;
		}
	}

	public void handleNamespace(String prefix, String name) {
		// ignore
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RDFHandlerException("Document writing has not yet been started");
		}

		try {
			Resource context = st.getContext();

			if (inActiveContext && !contextsEquals(context, currentContext)) {
				// Close currently active context
				xmlWriter.endTag(CONTEXT_TAG);
				inActiveContext = false;
			}

			if (!inActiveContext) {
				// Open new context
				xmlWriter.startTag(CONTEXT_TAG);

				if (context != null) {
					writeValue(context);
				}

				currentContext = context;
				inActiveContext = true;
			}

			xmlWriter.startTag(TRIPLE_TAG);

			writeValue(st.getSubject());
			writeValue(st.getPredicate());
			writeValue(st.getObject());

			xmlWriter.endTag(TRIPLE_TAG);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			xmlWriter.comment(comment);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Writes out the XML-representation for the supplied value.
	 */
	private void writeValue(Value value)
		throws IOException, RDFHandlerException
	{
		if (value instanceof URI) {
			URI uri = (URI)value;
			xmlWriter.textElement(URI_TAG, uri.toString());
		}
		else if (value instanceof BNode) {
			BNode bNode = (BNode)value;
			xmlWriter.textElement(BNODE_TAG, bNode.getID());
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;
			URI datatype = literal.getDatatype();

			if (datatype != null) {
				xmlWriter.setAttribute(DATATYPE_ATT, datatype.toString());
				xmlWriter.textElement(TYPED_LITERAL_TAG, literal.getLabel());
			}
			else {
				String language = literal.getLanguage();
				if (language != null) {
					xmlWriter.setAttribute(LANGUAGE_ATT, language);
				}
				xmlWriter.textElement(PLAIN_LITERAL_TAG, literal.getLabel());
			}
		}
		else {
			throw new RDFHandlerException("Unknown value type: " + value.getClass());
		}
	}

	private static final boolean contextsEquals(Resource context1, Resource context2) {
		if (context1 == null) {
			return context2 == null;
		}
		else {
			return context1.equals(context2);
		}
	}
}
