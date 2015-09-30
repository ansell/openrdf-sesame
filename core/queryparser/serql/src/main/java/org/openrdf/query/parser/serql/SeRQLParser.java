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
package org.openrdf.query.parser.serql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.serql.ast.ASTGraphQuery;
import org.openrdf.query.parser.serql.ast.ASTQuery;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTTupleQuery;
import org.openrdf.query.parser.serql.ast.ParseException;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.serql.ast.TokenMgrError;
import org.openrdf.query.parser.serql.ast.VisitorException;

public class SeRQLParser implements QueryParser {

	public ParsedQuery parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException
	{
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);

			// Replace deprecated NULL nodes with semantically equivalent
			// alternatives
			NullProcessor.process(qc);

			StringEscapesProcessor.process(qc);
			Map<String, String> namespaces = NamespaceDeclProcessor.process(qc);
			ProjectionProcessor.process(qc);
			qc.jjtAccept(new ProjectionAliasProcessor(), null);
			qc.jjtAccept(new AnonymousVarGenerator(), null);

			// TODO: check use of unbound variables?

			TupleExpr tupleExpr = QueryModelBuilder.buildQueryModel(qc, SimpleValueFactory.getInstance());

			ASTQuery queryNode = qc.getQuery();
			ParsedQuery query;
			if (queryNode instanceof ASTTupleQuery) {
				query = new ParsedTupleQuery(tupleExpr);
			}
			else if (queryNode instanceof ASTGraphQuery) {
				query = new ParsedGraphQuery(tupleExpr, namespaces);
			}
			else {
				throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
			}

			return query;
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (TokenMgrError e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	public static void main(String[] args)
		throws java.io.IOException
	{
		System.out.println("Your SeRQL query:");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		StringBuilder buf = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.length() > 0) {
				buf.append(' ').append(line).append('\n');
			}
			else {
				String queryStr = buf.toString().trim();
				if (queryStr.length() > 0) {
					try {
						SeRQLParser parser = new SeRQLParser();
						parser.parseQuery(queryStr, null);
					}
					catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
				buf.setLength(0);
			}
		}
	}

	public ParsedUpdate parseUpdate(String updateStr, String baseURI)
		throws MalformedQueryException
	{
		throw new UnsupportedOperationException("SeRQL does not support update operations");
	}
}
