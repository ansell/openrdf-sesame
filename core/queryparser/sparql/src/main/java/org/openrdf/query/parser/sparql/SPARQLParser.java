/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.BooleanQueryModel;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.query.parser.sparql.ast.ASTConstructQuery;
import org.openrdf.query.parser.sparql.ast.ASTDescribeQuery;
import org.openrdf.query.parser.sparql.ast.ASTQuery;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.query.parser.sparql.ast.ParseException;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.query.parser.sparql.ast.VisitorException;

public class SPARQLParser implements QueryParser {

	public QueryModel parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException
	{
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
			StringEscapesProcessor.process(qc);
			BaseDeclProcessor.process(qc, baseURI);
			Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
			WildcardProjectionProcessor.process(qc);
			BlankNodeVarProcessor.process(qc);
			TupleExpr tupleExpr = buildQueryModel(qc);

			QueryModel query;

			ASTQuery queryNode = qc.getQuery();
			if (queryNode instanceof ASTSelectQuery) {
				query = new TupleQueryModel(tupleExpr);
			}
			else if (queryNode instanceof ASTConstructQuery) {
				query = new GraphQueryModel(tupleExpr, prefixes);
			}
			else if (queryNode instanceof ASTAskQuery) {
				query = new BooleanQueryModel(tupleExpr);
			}
			else if (queryNode instanceof ASTDescribeQuery) {
				query = new GraphQueryModel(tupleExpr, prefixes);
			}
			else {
				throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
			}

			// Handle dataset declaration
			Dataset dataset = DatasetDeclProcessor.process(qc);
			if (dataset != null) {
				query.setDefaultGraphs(dataset.getDefaultGraphs());
				query.setNamedGraphs(dataset.getNamedGraphs());
			}

			return query;
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (TokenMgrError e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	private TupleExpr buildQueryModel(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(new ValueFactoryImpl());
		try {
			return (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	public static void main(String[] args)
		throws java.io.IOException
	{
		System.out.println("Your SPARQL query:");

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
						SPARQLParser parser = new SPARQLParser();
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
}
