/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
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

	private Writer writer;

	/**
	 * Table mapping namespace names (key) to namespace prefixes (value).
	 */
	private Map<String, String> namespaceTable;

	private boolean writingStarted;

	/**
	 * Flag indicating whether the last written statement has been closed.
	 */
	private boolean statementClosed;

	private Resource lastWrittenSubject;

	private URI lastWrittenPredicate;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TurtleWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the Turtle document to.
	 */
	public TurtleWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("UTF-8")));
	}

	/**
	 * Creates a new TurtleWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the Turtle document to.
	 */
	public TurtleWriter(Writer writer) {
		this.writer = writer;
		namespaceTable = new LinkedHashMap<String, String>();
		writingStarted = false;
		statementClosed = true;
		lastWrittenSubject = null;
		lastWrittenPredicate = null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	public void startRDF()
		throws RDFHandlerException
	{
		if (writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}

		writingStarted = true;

		try {
			// Write namespace declarations
			for (Map.Entry<String, String> entry : namespaceTable.entrySet()) {
				String name = entry.getKey();
				String prefix = entry.getValue();

				_writeNamespace(prefix, name);
			}

			if (!namespaceTable.isEmpty()) {
				_writeNewLine();
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void endRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}

		try {
			_closePreviousStatement();
			writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = false;
		}
	}

	public void handleNamespace(String prefix, String name)
		throws RDFHandlerException
	{
		try {
			if (!namespaceTable.containsKey(name)) {
				// Namespace not yet mapped to a prefix, try to give it the
				// specified prefix

				boolean isLegalPrefix = prefix.length() == 0 || TurtleUtil.isLegalPrefix(prefix);

				if (!isLegalPrefix || namespaceTable.containsValue(prefix)) {
					// Specified prefix is not legal or the prefix is already in use,
					// generate a legal unique prefix

					if (prefix.length() == 0 || !isLegalPrefix) {
						prefix = "ns";
					}

					int number = 1;

					while (namespaceTable.containsValue(prefix + number)) {
						number++;
					}

					prefix += number;
				}

				namespaceTable.put(name, prefix);

				if (writingStarted) {
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

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();

		try {
			if (subj.equals(lastWrittenSubject)) {
				if (pred.equals(lastWrittenPredicate)) {
					// Identical subject and predicate
					writer.write(" , ");
				}
				else {
					// Identical subject, new predicate
					writer.write(" ;");
					_writeNewLine();
					_writeIndent();

					// Write new predicate
					_writePredicate(pred);
					writer.write(" ");
					lastWrittenPredicate = pred;
				}
			}
			else {
				// New subject
				_closePreviousStatement();

				// Write new subject:
				_writeResource(subj);
				writer.write(" ");
				lastWrittenSubject = subj;

				// Write new predicate
				_writePredicate(pred);
				writer.write(" ");
				lastWrittenPredicate = pred;

				statementClosed = false;
			}

			_writeValue(obj);

			// Don't close the line just yet. Maybe the next
			// statement has the same subject and/or predicate.
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

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
		writer.write("# ");
		writer.write(line);
		_writeNewLine();
	}

	private void _writeNamespace(String prefix, String name)
		throws IOException
	{
		writer.write("@prefix ");
		writer.write(prefix);
		writer.write(": <");
		writer.write(TurtleUtil.encodeURIString(name));
		writer.write("> .");
		_writeNewLine();
	}

	private void _writePredicate(URI predicate)
		throws IOException
	{
		if (predicate.equals(RDF.TYPE)) {
			// Write short-cut for rdf:type
			writer.write("a");
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
			prefix = namespaceTable.get(namespace);
		}

		if (prefix != null) {
			// Namespace is mapped to a prefix; write abbreviated URI
			writer.write(prefix);
			writer.write(":");
			writer.write(uriString.substring(splitIdx));
		}
		else {
			// Write full URI
			writer.write("<");
			writer.write(TurtleUtil.encodeURIString(uriString));
			writer.write(">");
		}
	}

	private void _writeBNode(BNode bNode)
		throws IOException
	{
		writer.write("_:");
		writer.write(bNode.getID());
	}

	private void _writeLiteral(Literal lit)
		throws IOException
	{
		String label = lit.getLabel();

		if (label.indexOf('\n') > 0 || label.indexOf('\r') > 0 || label.indexOf('\t') > 0) {
			// Write label as long string
			writer.write("\"\"\"");
			writer.write(TurtleUtil.encodeLongString(label));
			writer.write("\"\"\"");
		}
		else {
			// Write label as normal string
			writer.write("\"");
			writer.write(TurtleUtil.encodeString(label));
			writer.write("\"");
		}

		if (lit.getDatatype() != null) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			writer.write("^^");
			_writeURI(lit.getDatatype());
		}
		else if (lit.getLanguage() != null) {
			// Append the literal's language
			writer.write("@");
			writer.write(lit.getLanguage());
		}
	}

	private void _writeIndent()
		throws IOException
	{
		writer.write("\t");
	}

	private void _writeNewLine()
		throws IOException
	{
		writer.write("\n");
	}

	private void _closePreviousStatement()
		throws IOException
	{
		if (!statementClosed) {
			// The previous statement still needs to be closed:
			writer.write(" .");
			_writeNewLine();
			_writeNewLine();

			statementClosed = true;
			lastWrittenSubject = null;
			lastWrittenPredicate = null;
		}
	}
}
