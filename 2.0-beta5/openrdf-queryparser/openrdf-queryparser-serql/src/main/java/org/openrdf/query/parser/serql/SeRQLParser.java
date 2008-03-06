/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.serql.ast.ASTGraphQuery;
import org.openrdf.query.parser.serql.ast.ASTQuery;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTTupleQuery;
import org.openrdf.query.parser.serql.ast.ParseException;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.serql.ast.Token;
import org.openrdf.query.parser.serql.ast.TokenMgrError;
import org.openrdf.query.parser.serql.ast.VisitorException;

public class SeRQLParser implements QueryParser {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

			StringEscapesProcessor.process(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed string escape sequences");
				logger.debug("{}", qc.dump(""));
			}

			Map<String, String> namespaces = NamespaceDeclProcessor.process(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed namespace declarations");
				logger.debug("{}", qc.dump(""));
			}

			WildcardProjectionProcessor.process(qc);
			if (logger.isDebugEnabled()) {
				logger.debug("Processed wildcard projections");
				logger.debug("{}", qc.dump(""));
			}

			qc.jjtAccept(new ProjectionAliasProcessor(), null);
			if (logger.isDebugEnabled()) {
				logger.debug("Generated implicit projection aliases");
				logger.debug("{}", qc.dump(""));
			}

			qc.jjtAccept(new AnonymousVarGenerator(), null);
			if (logger.isDebugEnabled()) {
				logger.debug("Inserted anonymous variables");
				logger.debug("{}", qc.dump(""));
			}

			// TODO: check use of unbound variables?

			TupleExpr tupleExpr = QueryModelBuilder.buildQueryModel(qc, new ValueFactoryImpl());
			if (logger.isDebugEnabled()) {
				logger.debug("Query model built");
				logger.debug("{}", QueryModelTreePrinter.printTree(tupleExpr));
			}

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
			Token errorToken = e.currentToken.next;
			if (errorToken == null) {
				errorToken = e.currentToken;
			}
			MalformedQueryException mfqe = new MalformedQueryException(e.getMessage(), e);
			mfqe.setEncounteredToken(errorToken.image);
			mfqe.setLineNumber(errorToken.beginLine);
			mfqe.setColumnNumber(errorToken.beginColumn);

			Set<String> expectedTokens = new HashSet<String>();
			int[][] expectedTokenSequences = e.expectedTokenSequences;
			for (int i = 0; i < expectedTokenSequences.length; i++) {
				StringBuilder expectedToken = new StringBuilder();
				for (int j = 0; j < expectedTokenSequences[i].length; j++) {
					expectedToken.append(e.tokenImage[expectedTokenSequences[i][j]]).append(" ");
				}
				expectedTokens.add(expectedToken.toString());
			}
			mfqe.setExpectedTokens(expectedTokens);

			throw mfqe;
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
}
