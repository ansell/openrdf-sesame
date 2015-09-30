/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.http.protocol.transaction;

import java.io.IOException;
import java.io.OutputStream;

import info.aduna.xml.XMLUtil;
import info.aduna.xml.XMLWriter;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SPARQLUpdateOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.StatementOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.util.Literals;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;

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
	protected void serialize(TransactionOperation op, XMLWriter xmlWriter)
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
		else if (op instanceof SPARQLUpdateOperation) {
			serialize((SPARQLUpdateOperation)op, xmlWriter);
		}
		else if (op == null) {
			// ignore(?)
		}
		else {
			throw new IllegalArgumentException("Unknown operation type: " + op.getClass());
		}
	}

	protected void serialize(AddStatementOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.ADD_STATEMENT_TAG);
		serialize((StatementOperation)op, xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.ADD_STATEMENT_TAG);
	}

	protected void serialize(SPARQLUpdateOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		String baseURI = op.getBaseURI();
		if (baseURI != null) {
			xmlWriter.setAttribute(TransactionXMLConstants.BASE_URI_ATT, baseURI);
		}
		xmlWriter.setAttribute(TransactionXMLConstants.INCLUDE_INFERRED_ATT, op.isIncludeInferred());
		xmlWriter.startTag(TransactionXMLConstants.SPARQL_UPDATE_TAG);

		// serialize update string
		String updateString = op.getUpdateString();
		xmlWriter.textElement(TransactionXMLConstants.UPDATE_STRING_TAG, updateString);

		// serialize dataset definition (if any)
		Dataset dataset = op.getDataset();
		if (dataset != null) {
			xmlWriter.startTag(TransactionXMLConstants.DATASET_TAG);

			xmlWriter.startTag(TransactionXMLConstants.DEFAULT_GRAPHS_TAG);
			for (IRI defaultGraph : dataset.getDefaultGraphs()) {
				xmlWriter.textElement(TransactionXMLConstants.GRAPH_TAG, defaultGraph.stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.DEFAULT_GRAPHS_TAG);

			xmlWriter.startTag(TransactionXMLConstants.NAMED_GRAPHS_TAG);
			for (IRI namedGraph : dataset.getNamedGraphs()) {
				xmlWriter.textElement(TransactionXMLConstants.GRAPH_TAG, namedGraph.stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.NAMED_GRAPHS_TAG);

			xmlWriter.startTag(TransactionXMLConstants.DEFAULT_REMOVE_GRAPHS_TAG);
			for (IRI defaultRemoveGraph : dataset.getDefaultRemoveGraphs()) {
				xmlWriter.textElement(TransactionXMLConstants.GRAPH_TAG, defaultRemoveGraph.stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.DEFAULT_REMOVE_GRAPHS_TAG);

			if (dataset.getDefaultInsertGraph() != null) {
				xmlWriter.textElement(TransactionXMLConstants.DEFAULT_INSERT_GRAPH,
						dataset.getDefaultInsertGraph().stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.DATASET_TAG);
		}

		if (op.getBindings() != null && op.getBindings().length > 0) {
			xmlWriter.startTag(TransactionXMLConstants.BINDINGS);

			for (Binding binding : op.getBindings()) {
				if (binding.getName() != null && binding.getValue() != null
						&& binding.getValue().stringValue() != null)
				{
					if (binding.getValue() instanceof IRI) {
						xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, binding.getName());
						xmlWriter.textElement(TransactionXMLConstants.BINDING_URI, binding.getValue().stringValue());
					}

					if (binding.getValue() instanceof BNode) {
						xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, binding.getName());
						xmlWriter.textElement(TransactionXMLConstants.BINDING_BNODE,
								binding.getValue().stringValue());
					}

					if (binding.getValue() instanceof Literal) {
						xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, binding.getName());

						Literal literal = (Literal)binding.getValue();
						if (Literals.isLanguageLiteral(literal)) {
							xmlWriter.setAttribute(TransactionXMLConstants.LANGUAGE_ATT, literal.getLanguage().get());
						}
						else {
							xmlWriter.setAttribute(TransactionXMLConstants.DATA_TYPE_ATT,
									literal.getDatatype().stringValue());
						}

						xmlWriter.textElement(TransactionXMLConstants.BINDING_LITERAL,
								binding.getValue().stringValue());
					}
				}
			}

			xmlWriter.endTag(TransactionXMLConstants.BINDINGS);
		}

		xmlWriter.endTag(TransactionXMLConstants.SPARQL_UPDATE_TAG);

	}

	protected void serialize(RemoveStatementsOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.REMOVE_STATEMENTS_TAG);
		serialize((StatementOperation)op, xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.REMOVE_STATEMENTS_TAG);
	}

	protected void serialize(StatementOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		serialize(op.getSubject(), xmlWriter);
		serialize(op.getPredicate(), xmlWriter);
		serialize(op.getObject(), xmlWriter);
		serialize(op.getContexts(), xmlWriter);
	}

	protected void serialize(ClearOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.CLEAR_TAG);
		serialize(op.getContexts(), xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.CLEAR_TAG);
	}

	protected void serialize(SetNamespaceOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.setAttribute(TransactionXMLConstants.PREFIX_ATT, op.getPrefix());
		xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, op.getName());
		xmlWriter.emptyElement(TransactionXMLConstants.SET_NAMESPACE_TAG);
	}

	protected void serialize(RemoveNamespaceOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.setAttribute(TransactionXMLConstants.PREFIX_ATT, op.getPrefix());
		xmlWriter.emptyElement(TransactionXMLConstants.REMOVE_NAMESPACE_TAG);
	}

	protected void serialize(ClearNamespacesOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.emptyElement(TransactionXMLConstants.CLEAR_NAMESPACES_TAG);
	}

	protected void serialize(Resource[] contexts, XMLWriter xmlWriter)
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

	protected void serialize(Value value, XMLWriter xmlWriter)
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

	protected void serialize(Resource resource, XMLWriter xmlWriter)
		throws IOException
	{
		if (resource instanceof IRI) {
			serialize((IRI)resource, xmlWriter);
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

	protected void serialize(IRI uri, XMLWriter xmlWriter)
		throws IOException
	{
		if (uri != null) {
			xmlWriter.textElement(TransactionXMLConstants.URI_TAG, uri.toString());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	protected void serialize(BNode bnode, XMLWriter xmlWriter)
		throws IOException
	{
		if (bnode != null) {
			xmlWriter.textElement(TransactionXMLConstants.BNODE_TAG, bnode.getID());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	protected void serialize(Literal literal, XMLWriter xmlWriter)
		throws IOException
	{
		if (literal != null) {
			if (Literals.isLanguageLiteral(literal)) {
				xmlWriter.setAttribute(TransactionXMLConstants.LANG_ATT, literal.getLanguage().get());
			}
			else {
				xmlWriter.setAttribute(TransactionXMLConstants.DATATYPE_ATT, literal.getDatatype().toString());
			}

			String label = literal.getLabel();

			boolean valid = true;
			int i = 0;
			while (valid && i < label.length()) {
				char c = label.charAt(i++);
				valid = XMLUtil.isValidCharacterDataChar(c);
			}

			if (!valid) {
				xmlWriter.setAttribute(TransactionXMLConstants.ENCODING_ATT, "base64");
				label = javax.xml.bind.DatatypeConverter.printBase64Binary(label.getBytes("UTF-8"));
			}

			xmlWriter.textElement(TransactionXMLConstants.LITERAL_TAG, label);
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	protected void serializeNull(XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.emptyElement(TransactionXMLConstants.NULL_TAG);
	}
}
