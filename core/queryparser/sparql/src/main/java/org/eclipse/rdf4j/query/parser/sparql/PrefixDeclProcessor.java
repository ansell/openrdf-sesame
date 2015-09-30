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
package org.eclipse.rdf4j.query.parser.sparql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SESAME;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDeleteData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTIRI;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTInsertData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTOperationContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPrefixDecl;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQName;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUnparsedQuadDataBlock;
import org.eclipse.rdf4j.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

/**
 * Processes the prefix declarations in a SPARQL query model.
 * 
 * @author Arjohn Kampman
 */
public class PrefixDeclProcessor {

	/**
	 * Processes prefix declarations in queries. This method collects all
	 * prefixes that are declared in the supplied query, verifies that prefixes
	 * are not redefined and replaces any {@link ASTQName} nodes in the query
	 * with equivalent {@link ASTIRI} nodes.
	 * 
	 * @param qc
	 *        The query that needs to be processed.
	 * @return A map containing the prefixes that are declared in the query (key)
	 *         and the namespace they map to (value).
	 * @throws MalformedQueryException
	 *         If the query contains redefined prefixes or qnames that use
	 *         undefined prefixes.
	 */
	public static Map<String, String> process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		List<ASTPrefixDecl> prefixDeclList = qc.getPrefixDeclList();

		// Build a prefix --> IRI map
		Map<String, String> prefixMap = new LinkedHashMap<String, String>();

		for (ASTPrefixDecl prefixDecl : prefixDeclList) {
			String prefix = prefixDecl.getPrefix();
			String iri = prefixDecl.getIRI().getValue();

			if (prefixMap.containsKey(prefix)) {
				throw new MalformedQueryException("Multiple prefix declarations for prefix '" + prefix + "'");
			}

			prefixMap.put(prefix, iri);
		}

		// insert some default prefixes (if not explicitly defined in the query)
		insertDefaultPrefix(prefixMap, "rdf", RDF.NAMESPACE);
		insertDefaultPrefix(prefixMap, "rdfs", RDFS.NAMESPACE);
		insertDefaultPrefix(prefixMap, "sesame", SESAME.NAMESPACE);
		insertDefaultPrefix(prefixMap, "owl", OWL.NAMESPACE);
		insertDefaultPrefix(prefixMap, "xsd", XMLSchema.NAMESPACE);
		insertDefaultPrefix(prefixMap, "fn", FN.NAMESPACE);

		ASTUnparsedQuadDataBlock dataBlock = null;
		if (qc.getOperation() instanceof ASTInsertData) {
			ASTInsertData insertData = (ASTInsertData)qc.getOperation();
			dataBlock = insertData.jjtGetChild(ASTUnparsedQuadDataBlock.class);

		}
		else if (qc.getOperation() instanceof ASTDeleteData) {
			ASTDeleteData deleteData = (ASTDeleteData)qc.getOperation();
			dataBlock = deleteData.jjtGetChild(ASTUnparsedQuadDataBlock.class);
		}

		if (dataBlock != null) {
			String prefixes = createPrefixesInSPARQLFormat(prefixMap);
			// TODO optimize string concat?
			dataBlock.setDataBlock(prefixes + dataBlock.getDataBlock());
		}
		else {
			QNameProcessor visitor = new QNameProcessor(prefixMap);
			try {
				qc.jjtAccept(visitor, null);
			}
			catch (VisitorException e) {
				throw new MalformedQueryException(e);
			}
		}

		return prefixMap;
	}

	private static void insertDefaultPrefix(Map<String, String> prefixMap, String prefix, String namespace) {
		if (!prefixMap.containsKey(prefix) && !prefixMap.containsValue(namespace)) {
			prefixMap.put(prefix, namespace);
		}
	}

	private static String createPrefixesInSPARQLFormat(Map<String, String> prefixMap) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : prefixMap.entrySet()) {
			sb.append("PREFIX");
			final String prefix = entry.getKey();
			if (prefix != null) {
				sb.append(" " + prefix);
			}
			sb.append(":");
			sb.append(" <" + entry.getValue() + "> \n");
		}
		return sb.toString();
	}

	private static class QNameProcessor extends AbstractASTVisitor {

		private Map<String, String> prefixMap;

		public QNameProcessor(Map<String, String> prefixMap) {
			this.prefixMap = prefixMap;
		}

		@Override
		public Object visit(ASTQName qnameNode, Object data)
			throws VisitorException
		{
			String qname = qnameNode.getValue();

			int colonIdx = qname.indexOf(':');
			assert colonIdx >= 0 : "colonIdx should be >= 0: " + colonIdx;

			String prefix = qname.substring(0, colonIdx);
			String localName = qname.substring(colonIdx + 1);

			String namespace = prefixMap.get(prefix);
			if (namespace == null) {
				throw new VisitorException("QName '" + qname + "' uses an undefined prefix");
			}

			localName = processEscapes(localName);

			// Replace the qname node with a new IRI node in the parent node
			ASTIRI iriNode = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
			iriNode.setValue(namespace + localName);
			qnameNode.jjtReplaceWith(iriNode);

			return null;
		}

		private String processEscapes(String localName) {

			// process escaped special chars.
			StringBuffer unescaped = new StringBuffer();
			Pattern escapedCharPattern = Pattern.compile("\\\\[_~\\.\\-!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=\\:\\/\\?#\\@\\%]");
			Matcher m = escapedCharPattern.matcher(localName);
			boolean result = m.find();
			while (result) {
				String escaped = m.group();
				m.appendReplacement(unescaped, escaped.substring(1));
				result = m.find();
			}
			m.appendTail(unescaped);

			return unescaped.toString();
		}

		@Override
		public Object visit(ASTServiceGraphPattern node, Object data)
			throws VisitorException
		{
			node.setPrefixDeclarations(prefixMap);
			return super.visit(node, data);
		}

	}
}
