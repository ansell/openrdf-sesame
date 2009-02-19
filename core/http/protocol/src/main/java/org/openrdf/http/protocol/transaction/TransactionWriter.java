/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction;

import java.io.IOException;
import java.io.OutputStream;

import info.aduna.xml.XMLWriter;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.StatementOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Serializes of an RDF transaction.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class TransactionWriter {

	public TransactionWriter() {
	}

	/**
	 * serialize the passed list of operations to the passed writer.
	 * 
	 * @param txn
	 *        the operations
	 * @param out
	 *        the output stream to write to
	 * @throws IllegalArgumentException
	 *         when one of the parameters is null
	 */
	public void serialize(Iterable<? extends TransactionOperation> txn, OutputStream out)
		throws IOException
	{
		assert txn != null : "operation list must not be null";
		assert out != null : "output stream must not be null";

		XMLWriter xmlWriter = new XMLWriter(out);
		xmlWriter.setPrettyPrint(true);

		xmlWriter.startDocument();
		xmlWriter.startTag(TransactionXMLConstants.TRANSACTION_TAG);

		for (TransactionOperation op : txn) {
			serialize(op, xmlWriter);
		}

		xmlWriter.endTag(TransactionXMLConstants.TRANSACTION_TAG);
		xmlWriter.endDocument();
	}

	/**
	 * Serializes the supplied operation.
	 * 
	 * @param op
	 *        The operation to serialize
	 */
	private void serialize(TransactionOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		if (op instanceof AddStatementOperation) {
			serialize((AddStatementOperation)op, xmlWriter);
		}
		else if (op instanceof RemoveStatementsOperation) {
			serialize((RemoveStatementsOperation)op, xmlWriter);
		}
		else if (op instanceof ClearOperation) {
			serialize((ClearOperation)op, xmlWriter);
		}
		else if (op instanceof SetNamespaceOperation) {
			serialize((SetNamespaceOperation)op, xmlWriter);
		}
		else if (op instanceof RemoveNamespaceOperation) {
			serialize((RemoveNamespaceOperation)op, xmlWriter);
		}
		else if (op instanceof ClearNamespacesOperation) {
			serialize((ClearNamespacesOperation)op, xmlWriter);
		}
		else if (op == null) {
			// ignore(?)
		}
		else {
			throw new IllegalArgumentException("Unknown operation type: " + op.getClass());
		}
	}

	private void serialize(AddStatementOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.ADD_STATEMENT_TAG);
		serialize((StatementOperation)op, xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.ADD_STATEMENT_TAG);
	}

	private void serialize(RemoveStatementsOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.REMOVE_STATEMENTS_TAG);
		serialize((StatementOperation)op, xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.REMOVE_STATEMENTS_TAG);
	}

	private void serialize(StatementOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		serialize(op.getSubject(), xmlWriter);
		serialize(op.getPredicate(), xmlWriter);
		serialize(op.getObject(), xmlWriter);
		serialize(op.getContexts(), xmlWriter);
	}

	private void serialize(ClearOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.CLEAR_TAG);
		serialize(op.getContexts(), xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.CLEAR_TAG);
	}

	private void serialize(SetNamespaceOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.setAttribute(TransactionXMLConstants.PREFIX_ATT, op.getPrefix());
		xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, op.getName());
		xmlWriter.emptyElement(TransactionXMLConstants.SET_NAMESPACE_TAG);
	}

	private void serialize(RemoveNamespaceOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.setAttribute(TransactionXMLConstants.PREFIX_ATT, op.getPrefix());
		xmlWriter.emptyElement(TransactionXMLConstants.REMOVE_NAMESPACE_TAG);
	}

	private void serialize(ClearNamespacesOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.emptyElement(TransactionXMLConstants.CLEAR_NAMESPACES_TAG);
	}

	private void serialize(Resource[] contexts, XMLWriter xmlWriter)
		throws IOException
	{
		if (contexts.length > 0) {
			xmlWriter.startTag(TransactionXMLConstants.CONTEXTS_TAG);
			for (Resource context : contexts) {
				serialize(context, xmlWriter);
			}
			xmlWriter.endTag(TransactionXMLConstants.CONTEXTS_TAG);
		}
		else {
			xmlWriter.emptyElement(TransactionXMLConstants.CONTEXTS_TAG);
		}
	}

	private void serialize(Value value, XMLWriter xmlWriter)
		throws IOException
	{
		if (value instanceof Resource) {
			serialize((Resource)value, xmlWriter);
		}
		else if (value instanceof Literal) {
			serialize((Literal)value, xmlWriter);
		}
		else if (value == null) {
			serializeNull(xmlWriter);
		}
		else {
			throw new IllegalArgumentException("Unknown value type: " + value.getClass().toString());
		}
	}

	private void serialize(Resource resource, XMLWriter xmlWriter)
		throws IOException
	{
		if (resource instanceof URI) {
			serialize((URI)resource, xmlWriter);
		}
		else if (resource instanceof BNode) {
			serialize((BNode)resource, xmlWriter);
		}
		else if (resource == null) {
			serializeNull(xmlWriter);
		}
		else {
			throw new IllegalArgumentException("Unknown resource type: " + resource.getClass().toString());
		}
	}

	private void serialize(URI uri, XMLWriter xmlWriter)
		throws IOException
	{
		if (uri != null) {
			xmlWriter.textElement(TransactionXMLConstants.URI_TAG, uri.toString());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	private void serialize(BNode bnode, XMLWriter xmlWriter)
		throws IOException
	{
		if (bnode != null) {
			xmlWriter.textElement(TransactionXMLConstants.BNODE_TAG, bnode.getID());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	private void serialize(Literal literal, XMLWriter xmlWriter)
		throws IOException
	{
		if (literal != null) {
			if (literal.getLanguage() != null) {
				xmlWriter.setAttribute(TransactionXMLConstants.LANG_ATT, literal.getLanguage());
			}
			if (literal.getDatatype() != null) {
				xmlWriter.setAttribute(TransactionXMLConstants.DATATYPE_ATT, literal.getDatatype().toString());
			}
			xmlWriter.textElement(TransactionXMLConstants.LITERAL_TAG, literal.getLabel());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	private void serializeNull(XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.emptyElement(TransactionXMLConstants.NULL_TAG);
	}
}
