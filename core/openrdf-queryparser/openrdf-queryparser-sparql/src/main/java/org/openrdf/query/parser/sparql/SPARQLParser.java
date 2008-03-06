/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
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

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ParsedQuery parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException
	{
		logger.debug("Parsing query: {}", queryStr);

		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
			if (logger.isDebugEnabled()) {
				logger.debug("Syntax tree built");
				logger.debug("{}", qc.dump(""));
			}

			BaseDeclProcessor.process(qc, baseURI);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed base declaration");
				logger.debug("{}", qc.dump(""));
			}

			Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed prefix declarations");
				logger.debug("{}", qc.dump(""));
			}

			WildcardProjectionProcessor.process(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed wildcard projections");
				logger.debug("{}", qc.dump(""));
			}

			BlankNodeVarProcessor.process(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed blank nodes variables");
				logger.debug("{}", qc.dump(""));
			}

			TupleExpr tupleExpr = _buildQueryModel(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Query model built");
				logger.debug("{}", QueryModelTreePrinter.printTree(tupleExpr));
			}

			ASTQuery queryNode = qc.getQuery();
			ParsedQuery query;
			if (queryNode instanceof ASTSelectQuery) {
				query = new ParsedTupleQuery(tupleExpr);
			}
			else if (queryNode instanceof ASTConstructQuery) {
				query = new ParsedGraphQuery(tupleExpr, prefixes);
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

			return query;
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (TokenMgrError e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	private TupleExpr _buildQueryModel(ASTQueryContainer qc)
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
