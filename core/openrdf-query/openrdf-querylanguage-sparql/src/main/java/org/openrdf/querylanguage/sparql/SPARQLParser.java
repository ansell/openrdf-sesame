/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.sparql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.QueryParser;
import org.openrdf.querylanguage.sparql.ast.ASTAskQuery;
import org.openrdf.querylanguage.sparql.ast.ASTConstructQuery;
import org.openrdf.querylanguage.sparql.ast.ASTDescribeQuery;
import org.openrdf.querylanguage.sparql.ast.ASTQuery;
import org.openrdf.querylanguage.sparql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.sparql.ast.ASTSelectQuery;
import org.openrdf.querylanguage.sparql.ast.ParseException;
import org.openrdf.querylanguage.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.querylanguage.sparql.ast.VisitorException;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.Query;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.querymodel.helpers.QueryModelTreePrinter;
import org.openrdf.util.log.ThreadLog;

public class SPARQLParser implements QueryParser {

	public Query parseQuery(String queryStr)
		throws MalformedQueryException
	{
		ThreadLog.trace("Parsing query", queryStr);

		try {
			ASTQueryContainer qc = _buildAST(queryStr);

			BaseDeclProcessor.process(qc);
			ThreadLog.trace("Processed base declaration", qc.dump(""));

			Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
			ThreadLog.trace("Processed prefix declarations", qc.dump(""));

			WildcardProjectionProcessor.process(qc);
			ThreadLog.trace("Processed wildcard projections", qc.dump(""));

			BlankNodeVarProcessor.process(qc);
			ThreadLog.trace("Processed blank nodes variables", qc.dump(""));

			TupleExpr tupleExpr = _buildQueryModel(qc);
			ThreadLog.trace("Query model built", QueryModelTreePrinter.printTree(tupleExpr));

			ASTQuery queryNode = qc.getQuery();
			Query query;
			if (queryNode instanceof ASTSelectQuery) {
				query = new TupleQuery(tupleExpr);
			}
			else if (queryNode instanceof ASTConstructQuery) {
				query = new GraphQuery(tupleExpr, prefixes);
			}
			else if (queryNode instanceof ASTAskQuery) {
				throw new RuntimeException("SPARQL ASK queries not yet supported");
			}
			else if (queryNode instanceof ASTDescribeQuery) {
				throw new RuntimeException("SPARQL DESCRIBE queries not yet supported");
			}
			else {
				throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
			}

			query.setQueryLanguage(QueryLanguage.SPARQL);
			query.setQueryString(queryStr);

			return query;
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e);
		}
	}

	private ASTQueryContainer _buildAST(String queryStr)
		throws ParseException
	{
		ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
		ThreadLog.trace("Syntax tree built", qc.dump(""));
		return qc;
	}

	private TupleExpr _buildQueryModel(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(new ValueFactoryImpl());
		try {
			return (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	public static void main(String[] args)
		throws java.io.IOException
	{
		ThreadLog.setDefaultLog(null, ThreadLog.ALL);
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
						parser.parseQuery(queryStr);
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
