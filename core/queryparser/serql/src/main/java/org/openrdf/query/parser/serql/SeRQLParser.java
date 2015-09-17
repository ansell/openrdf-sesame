/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
