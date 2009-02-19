/*
 * Copyright James Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.rio.rdfa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import info.aduna.xml.XMLUtil;

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
 * @author James Leigh
 */
public class PrettyRDFWriter implements RDFWriter {

	private static class Node {

		private boolean isWritten = false;

		private Value value;

		private Set<URI> types = new HashSet<URI>();

		/**
		 * Creates a new Node for the supplied Value.
		 */
		public Node(Value value) {
			this.value = value;
		}

		public Value getValue() {
			return value;
		}

		public void addType(URI type) {
			types.add(type);
		}

		public Set<URI> getTypes() {
			return types;
		}

		public boolean isWritten() {
			return isWritten;
		}

		public void setIsWritten(boolean isWritten) {
			this.isWritten = isWritten;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private String baseURI;

	private java.net.URI relativeURI;

	private boolean headerWritten;

	private boolean inHeader;

	private Map<String, String> namespaceTable;

	/**
	 * Stack for remembering the nodes (subjects/objects) of statements at each
	 * level.
	 */
	private Stack<Node> nodeStack = new Stack<Node>();

	/*
	 * We implement striped syntax by using two stacks, one for predicates and
	 * one for subjects/objects.
	 */

	/**
	 * Stack for remembering the predicate of statements at each level.
	 */
	private Stack<URI> predicateStack = new Stack<URI>();

	private RDFaMetaWriter writer;

	/*--------------*
	 * Constructors *
	 *--------------*/

	private boolean writingStarted;

	public PrettyRDFWriter(RDFaMetaWriter writer) {
		this.writer = writer;
		namespaceTable = new LinkedHashMap<String, String>();
		writingStarted = false;
		headerWritten = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RDFFormat getRDFFormat() {
		return RDFFormat.RDFA;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
		try {
			if (baseURI == null) {
				relativeURI = null;
			}
			else if (baseURI.charAt(baseURI.length() - 1) == '/') {
				relativeURI = new java.net.URI(baseURI);
			}
			else if (baseURI.lastIndexOf('/') > 0) {
				String parent = baseURI.substring(0, baseURI.lastIndexOf('/'));
				relativeURI = new java.net.URI(parent);
			}
		}
		catch (URISyntaxException e) {
			// don't use relative URIs
			relativeURI = null;
		}
	}

	public void close()
		throws IOException
	{
		try {
			if (writingStarted) {
				endRDF();
			}
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
			writer.close();
		}
	}

	public void startRDF() {
		if (writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}
		writingStarted = true;
	}

	public void endRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}

		try {
			if (!headerWritten) {
				header();
			}
			if (inHeader) {
				flush();
				body();
			}

			flush();

			writer.endRDF();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = false;
			headerWritten = false;
		}
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			if (!headerWritten) {
				header();
			}

			flush();

			writer.handleComment(comment);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleNamespace(String prefix, String name) {
		if (headerWritten) {
			// Header containing namespace declarations has already been written
			return;
		}

		if (!namespaceTable.containsKey(name)) {
			// Namespace not yet mapped to a prefix, try to give it the specified
			// prefix

			boolean isLegalPrefix = prefix.length() == 0 || XMLUtil.isNCName(prefix);

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
			if (!headerWritten) {
				header();
			}
			// can only be about baseURI subject
			// can't have nested BNodes in header
			if (inHeader && isAbout(subj)) {
				if (obj instanceof URI) {
					String relativize = relativize(obj.stringValue());
					writer.handleMetaLink(pred, relativize);
					return;
				}
				else if (obj instanceof Literal) {
					writer.handleMetaAttribute(pred, (Literal)obj);
					return;
				}
			}
			else if (inHeader) {
				flush();
				body();
			}

			if (!nodeStack.isEmpty() && !subj.equals(nodeStack.peek().getValue())) {
				// New subject, empty the stack
				popStacks(subj);
			}
			else if (nodeStack.size() > 1 && subj instanceof URI) {
				// New subject, empty the stack
				popStacks(subj);
			}

			// Stack is either empty or contains the same subject at top

			if (nodeStack.isEmpty()) {
				// Push subject
				nodeStack.push(new Node(subj));
			}

			// Stack now contains at least one element

			// Check if current statement is a type statement and use a typed node
			// element is possible
			if ((obj instanceof URI && RDF.TYPE.equals(pred) && !nodeStack.peek().isWritten())) {
				nodeStack.peek().addType((URI)obj);
			}
			else {
				// Push predicate and object
				predicateStack.push(pred);
				nodeStack.push(new Node(obj));
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	private void header()
		throws IOException
	{
		try {
			writer.startRDF(baseURI, namespaceTable);
			inHeader = true;
			writer.startMeta();
		}
		finally {
			headerWritten = true;
		}
	}

	private void body()
		throws IOException
	{
		try {
			writer.endMeta();
		}
		finally {
			inHeader = false;
		}
	}

	private boolean isAbout(Resource subj) {
		if (subj instanceof URI) {
			return subj.stringValue().equals(baseURI);
		}
		return false;
	}

	private void flush()
		throws IOException
	{
		if (!nodeStack.isEmpty()) {
			popStacks(null);
		}
	}

	/**
	 * Write out the stacks until we find subject. If subject == null, write out
	 * the entire stack
	 * 
	 * @param newSubject
	 */
	private void popStacks(Resource newSubject)
		throws IOException
	{
		// Write start tags for the part of the stacks that are not yet
		// written
		for (int i = 0; i < nodeStack.size() - 1; i++) {
			Node node = nodeStack.get(i);

			if (!node.isWritten()) {
				if (i > 0) {
					assert node.getValue() instanceof BNode;
					writer.openProperty((i * 2 - 1), predicateStack.get(i - 1));
				}
				openStartTag((i * 2), node.getValue(), node.getTypes());
				node.setIsWritten(true);
			}
		}

		// Write tags for the top subject
		Node topNode = nodeStack.pop();

		if (predicateStack.isEmpty()) {
			openStartTag(0, topNode.getValue(), topNode.getTypes());
			endTag(0, topNode.getValue(), topNode.getTypes());
		}
		else {
			URI topPredicate = predicateStack.pop();
			Value obj = topNode.getValue();
			int indent = nodeStack.size() * 2 - 1;
			if (obj instanceof URI) {
				String relativize = relativize(obj.stringValue());
				assert topNode.getTypes().isEmpty();
				writer.handleURI(indent, topPredicate, relativize);
			}
			else if (obj instanceof BNode && topNode.getTypes().isEmpty()) {
				writer.handleBlankNode(indent, topPredicate, (BNode)obj);
			}
			else if (obj instanceof BNode) {
				writer.openProperty(indent, topPredicate);
				writer.startBlankNode(indent, (BNode)obj, topNode.getTypes());
				writer.endBlankNode(indent, (BNode)obj, topNode.getTypes());
				writer.closeProperty(indent, topPredicate);
			}
			else if (obj instanceof Literal) {
				writer.handleLiteral(indent, topPredicate, (Literal)obj);
			}

			// Write out the end tags until we find the subject
			while (!nodeStack.isEmpty()) {
				Node nextElement = nodeStack.peek();
				if (nextElement.getValue().equals(newSubject)) {
					break;
				}
				else {
					Node node = nodeStack.pop();
					// We have already written out the subject/object,
					// but we still need to close the tag
					indent = predicateStack.size() + nodeStack.size();
					endTag(indent, nextElement.getValue(), nextElement.getTypes());
					if (predicateStack.size() > 0) {
						URI predicate = predicateStack.pop();
						indent = predicateStack.size() + nodeStack.size();
						assert node.getValue() instanceof BNode;
						writer.closeProperty(indent, predicate);
					}
				}
			}
		}
	}

	/**
	 * Write out the opening tag of the subject or object of a statement up to
	 * (but not including) the end of the tag. Used both in writeStartSubject and
	 * writeEmptySubject.
	 */
	private void openStartTag(int indent, Value subj, Set<URI> types)
		throws IOException
	{
		if (subj instanceof URI) {
			assert indent == 0;
			String uri = subj.stringValue();
			String relativize = relativize(uri);
			writer.startNode(relativize, types);
		}
		else {
			writer.startBlankNode(indent, (BNode)subj, types);
		}
	}

	/**
	 * Write out the closing tag for the subject or object of a statement.
	 */
	private void endTag(int indent, Value subj, Set<URI> types)
		throws IOException
	{
		if (subj instanceof URI) {
			assert indent == 0;
			String uri = subj.stringValue();
			String relativize = relativize(uri);
			writer.endNode(relativize, types);
		}
		else {
			writer.endBlankNode(indent, (BNode)subj, types);
		}
	}

	private String relativize(String stringValue) {
		if (baseURI == null)
			return stringValue;
		if (stringValue.equals(baseURI))
			return "";
		if (!stringValue.startsWith(baseURI))
			return stringValue;
		if ('#' == stringValue.charAt(baseURI.length()))
			return stringValue.substring(baseURI.length());
		if (relativeURI == null)
			return stringValue;
		try {
			java.net.URI uri = new java.net.URI(stringValue);
			return relativeURI.relativize(uri).toString();
		}
		catch (URISyntaxException e) {
			return stringValue;
		}
	}

}
