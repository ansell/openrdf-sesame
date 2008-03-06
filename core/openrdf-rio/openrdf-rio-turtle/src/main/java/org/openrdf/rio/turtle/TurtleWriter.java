/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
 * Turtle format. The Turtle format is defined in <a
 * href="http://www.dajobe.org/2004/01/turtle/">in this document</a>.
 */
public class TurtleWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Writer _writer;

	private Map<String, String> _namespaceTable;

	private boolean _writingStarted;

	/**
	 * Flag indicating whether the last written statement has been closed.
	 */
	private boolean _statementClosed;

	private Resource _lastWrittenSubject;

	private URI _lastWrittenPredicate;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TurtleWriter. Note that, before this writer can be used, an
	 * OutputStream or Writer needs to be supplied to it using
	 * {@link #setOutputStream(OutputStream)} or {@link #setWriter(Writer)}.
	 */
	public TurtleWriter() {
		_namespaceTable = new LinkedHashMap<String, String>();
		_writingStarted = false;
		_statementClosed = true;
		_lastWrittenSubject = null;
		_lastWrittenPredicate = null;
	}

	/**
	 * Creates a new TurtleWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the Turtle document to.
	 */
	public TurtleWriter(OutputStream out) {
		this();
		setOutputStream(out);
	}

	/**
	 * Creates a new TurtleWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the Turtle document to.
	 */
	public TurtleWriter(Writer writer) {
		this();
		setWriter(writer);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFWriter.getRDFFormat()
	public RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	public void setOutputStream(OutputStream out) {
		try {
			setWriter(new OutputStreamWriter(out, "UTF-8"));
		}
		catch (java.io.UnsupportedEncodingException e) {
			// UTF-8 is required to be supported on all JVMs...
			throw new RuntimeException(e);
		}
	}

	public void setWriter(Writer writer) {
		_writer = writer;
	}

	// implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
		if (_writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}

		_writingStarted = true;

		try {
			// Write namespace declarations
			for (Map.Entry<String, String> entry : _namespaceTable.entrySet()) {
				String name = entry.getKey();
				String prefix = entry.getValue();

				_writeNamespace(prefix, name);
			}

			if (!_namespaceTable.isEmpty()) {
				_writeNewLine();
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
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
			_closePreviousStatement();
			_writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			_writingStarted = false;
		}
	}

	// implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String name)
		throws RDFHandlerException
	{
		try {
			if (!_namespaceTable.containsKey(name)) {
				_namespaceTable.put(name, prefix);

				if (_writingStarted) {
					_closePreviousStatement();

					_writeNamespace(prefix, name);
					_writeNewLine();
				}
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
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

		try {
			if (subj.equals(_lastWrittenSubject)) {
				if (pred.equals(_lastWrittenPredicate)) {
					// Identical subject and predicate
					_writer.write(" , ");
				}
				else {
					// Identical subject, new predicate
					_writer.write(" ;");
					_writeNewLine();
					_writeIndent();

					// Write new predicate
					_writePredicate(pred);
					_writer.write(" ");
					_lastWrittenPredicate = pred;
				}
			}
			else {
				// New subject
				_closePreviousStatement();

				// Write new subject:
				_writeResource(subj);
				_writer.write(" ");
				_lastWrittenSubject = subj;

				// Write new predicate
				_writePredicate(pred);
				_writer.write(" ");
				_lastWrittenPredicate = pred;

				_statementClosed = false;
			}

			_writeValue(obj);

			// Don't close the line just yet. Maybe the next
			// statement has the same subject and/or predicate.
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	// implements RDFWriter.writeComment(String)
	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			_closePreviousStatement();

			if (comment.indexOf('\r') != -1 || comment.indexOf('\n') != -1) {
				// Comment is not allowed to contain newlines or line feeds.
				// Split comment in individual lines and write comment lines
				// for each of them.
				StringTokenizer st = new StringTokenizer(comment, "\r\n");
				while (st.hasMoreTokens()) {
					_writeCommentLine(st.nextToken());
				}
			}
			else {
				_writeCommentLine(comment);
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	private void _writeCommentLine(String line)
		throws IOException
	{
		_writer.write("# ");
		_writer.write(line);
		_writeNewLine();
	}

	private void _writeNamespace(String prefix, String name)
		throws IOException
	{
		_writer.write("@prefix ");
		_writer.write(prefix);
		_writer.write(": <");
		_writer.write(TurtleUtil.encodeURIString(name));
		_writer.write("> .");
		_writeNewLine();
	}

	private void _writePredicate(URI predicate)
		throws IOException
	{
		if (predicate.equals(RDF.TYPE)) {
			// Write short-cut for rdf:type
			_writer.write("a");
		}
		else {
			_writeURI(predicate);
		}
	}

	private void _writeValue(Value val)
		throws IOException
	{
		if (val instanceof Resource) {
			_writeResource((Resource)val);
		}
		else {
			_writeLiteral((Literal)val);
		}
	}

	private void _writeResource(Resource res)
		throws IOException
	{
		if (res instanceof URI) {
			_writeURI((URI)res);
		}
		else {
			_writeBNode((BNode)res);
		}
	}

	private void _writeURI(URI uri)
		throws IOException
	{
		String uriString = uri.toString();

		// Try to find a prefix for the URI's namespace
		String prefix = null;

		int splitIdx = TurtleUtil.findURISplitIndex(uriString);
		if (splitIdx > 0) {
			String namespace = uriString.substring(0, splitIdx);
			prefix = _namespaceTable.get(namespace);
		}

		if (prefix != null) {
			// Namespace is mapped to a prefix; write abbreviated URI
			_writer.write(prefix);
			_writer.write(":");
			_writer.write(uriString.substring(splitIdx));
		}
		else {
			// Write full URI
			_writer.write("<");
			_writer.write(TurtleUtil.encodeURIString(uriString));
			_writer.write(">");
		}
	}

	private void _writeBNode(BNode bNode)
		throws IOException
	{
		_writer.write("_:");
		_writer.write(bNode.getID());
	}

	private void _writeLiteral(Literal lit)
		throws IOException
	{
		String label = lit.getLabel();

		if (label.indexOf('\n') > 0 || label.indexOf('\r') > 0 || label.indexOf('\t') > 0) {
			// Write label as long string
			_writer.write("\"\"\"");
			_writer.write(TurtleUtil.encodeLongString(label));
			_writer.write("\"\"\"");
		}
		else {
			// Write label as normal string
			_writer.write("\"");
			_writer.write(TurtleUtil.encodeString(label));
			_writer.write("\"");
		}

		if (lit.getDatatype() != null) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			_writer.write("^^");
			_writeURI(lit.getDatatype());
		}
		else if (lit.getLanguage() != null) {
			// Append the literal's language
			_writer.write("@");
			_writer.write(lit.getLanguage());
		}
	}

	private void _writeIndent()
		throws IOException
	{
		_writer.write("\t");
	}

	private void _writeNewLine()
		throws IOException
	{
		_writer.write("\n");
	}

	private void _closePreviousStatement()
		throws IOException
	{
		if (!_statementClosed) {
			// The previous statement still needs to be closed:
			_writer.write(" .");
			_writeNewLine();
			_writeNewLine();

			_statementClosed = true;
			_lastWrittenSubject = null;
			_lastWrittenPredicate = null;
		}
	}
}
