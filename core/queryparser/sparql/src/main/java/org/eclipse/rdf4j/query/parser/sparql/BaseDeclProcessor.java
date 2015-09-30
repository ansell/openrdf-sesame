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

import org.eclipse.rdf4j.common.net.ParsedURI;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTBaseDecl;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDeleteData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTIRI;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTIRIFunc;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTInsertData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTOperationContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUnparsedQuadDataBlock;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

/**
 * Resolves relative URIs in a query model using either an external base URI or
 * using the base URI specified in the query model itself. The former takes
 * precedence over the latter.
 * 
 * @author Arjohn Kampman
 */
public class BaseDeclProcessor {

	/**
	 * Resolves relative URIs in the supplied query model using either the
	 * specified <tt>externalBaseURI</tt> or, if this parameter is <tt>null</tt>,
	 * the base URI specified in the query model itself.
	 * 
	 * @param qc
	 *        The query model to resolve relative URIs in.
	 * @param externalBaseURI
	 *        The external base URI to use for resolving relative URIs, or
	 *        <tt>null</tt> if the base URI that is specified in the query model
	 *        should be used.
	 * @throws IllegalArgumentException
	 *         If an external base URI is specified that is not an absolute URI.
	 * @throws MalformedQueryException
	 *         If the base URI specified in the query model is not an absolute
	 *         URI.
	 */
	public static void process(ASTOperationContainer qc, String externalBaseURI)
		throws MalformedQueryException
	{
		ParsedURI parsedBaseURI = null;

		// Use the query model's own base URI, if available
		ASTBaseDecl baseDecl = qc.getBaseDecl();
		if (baseDecl != null) {
			parsedBaseURI = new ParsedURI(baseDecl.getIRI());

			if (!parsedBaseURI.isAbsolute()) {
				throw new MalformedQueryException("BASE IRI is not an absolute IRI: " + externalBaseURI);
			}
		}
		else if (externalBaseURI != null) {
			// Use external base URI if the query doesn't contain one itself
			parsedBaseURI = new ParsedURI(externalBaseURI);

			if (!parsedBaseURI.isAbsolute()) {
				throw new IllegalArgumentException("Supplied base URI is not an absolute IRI: " + externalBaseURI);
			}
		}
		else {
			// FIXME: use the "Default Base URI"?
		}

		if (parsedBaseURI != null) {
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
				final String baseURIDeclaration = "BASE <" + parsedBaseURI + "> \n";
				dataBlock.setDataBlock(baseURIDeclaration + dataBlock.getDataBlock());
			}
			else {
				RelativeIRIResolver visitor = new RelativeIRIResolver(parsedBaseURI);
				try {
					qc.jjtAccept(visitor, null);
				}
				catch (VisitorException e) {
					throw new MalformedQueryException(e);
				}
			}
		}
	}

	private static class RelativeIRIResolver extends AbstractASTVisitor {

		private ParsedURI parsedBaseURI;

		public RelativeIRIResolver(ParsedURI parsedBaseURI) {
			this.parsedBaseURI = parsedBaseURI;
		}

		@Override
		public Object visit(ASTIRI node, Object data)
			throws VisitorException
		{
			ParsedURI resolvedURI = parsedBaseURI.resolve(node.getValue());
			node.setValue(resolvedURI.toString());

			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTIRIFunc node, Object data)
			throws VisitorException
		{
			node.setBaseURI(parsedBaseURI.toString());
			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTServiceGraphPattern node, Object data)
			throws VisitorException
		{
			node.setBaseURI(parsedBaseURI.toString());
			return super.visit(node, data);
		}
	}
}
