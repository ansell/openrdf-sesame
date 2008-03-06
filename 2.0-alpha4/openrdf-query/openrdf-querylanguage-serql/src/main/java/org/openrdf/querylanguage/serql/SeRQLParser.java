/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.QueryParser;
import org.openrdf.querylanguage.serql.ast.ASTGraphQuery;
import org.openrdf.querylanguage.serql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.serql.ast.ASTQuery;
import org.openrdf.querylanguage.serql.ast.ASTTupleQuery;
import org.openrdf.querylanguage.serql.ast.ParseException;
import org.openrdf.querylanguage.serql.ast.SyntaxTreeBuilder;
import org.openrdf.querylanguage.serql.ast.VisitorException;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.Query;
import org.openrdf.querymodel.QueryLanguage;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.querymodel.helpers.QueryModelTreePrinter;
import org.openrdf.util.log.ThreadLog;

public class SeRQLParser implements QueryParser {

	public Query parseQuery(String queryStr)
		throws MalformedQueryException
	{
		ThreadLog.trace("Parsing query", queryStr);

		try {
			ASTQueryContainer qc = _buildAST(queryStr);

			_processWildcardProjections(qc);
			_processProjectionAliases(qc);
			_generateAnonymousVars(qc);

			// - check use of unbound variables?
			// - align projections in set queries

			TupleExpr tupleExpr = _buildQueryModel(qc);

			ASTQuery queryNode = qc.getQuery();
			Query query;
			if (queryNode instanceof ASTTupleQuery) {
				query = new TupleQuery(tupleExpr);
			}
			else if (queryNode instanceof ASTGraphQuery) {
				// FIXME: supply query namespace definitions to GraphQuery
				query = new GraphQuery(tupleExpr);
			}
			else {
				throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
			}

			query.setQueryLanguage(QueryLanguage.SERQL);
			query.setQueryString(queryStr);

			return query;
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e);
		}
		catch (VisitorException e) {
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

	private void _processWildcardProjections(ASTQueryContainer qc)
		throws VisitorException
	{
		qc.jjtAccept(new WildcardProjectionProcessor(), null);
		ThreadLog.trace("Processed wildcard projections", qc.dump(""));
	}

	private void _processProjectionAliases(ASTQueryContainer qc)
		throws VisitorException
	{
		qc.jjtAccept(new ProjectionAliasProcessor(), null);
		ThreadLog.trace("Generated implicit projection aliases", qc.dump(""));
	}

	private void _generateAnonymousVars(ASTQueryContainer qc)
		throws VisitorException
	{
		qc.jjtAccept(new AnonymousVarGenerator(), null);
		ThreadLog.trace("Inserted anonymous variables", qc.dump(""));
	}

	private TupleExpr _buildQueryModel(ASTQueryContainer qc)
		throws MalformedQueryException, VisitorException
	{
		TupleExpr tupleExpr = QueryModelBuilder.buildQueryModel(qc, new ValueFactoryImpl());
		ThreadLog.trace("Query model built", QueryModelTreePrinter.printTree(tupleExpr));
		return tupleExpr;
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
