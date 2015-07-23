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
package org.openrdf.spin;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.StatementSource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.Statements;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;

public class SPINParser {

	public enum Input {
		TEXT_FIRST(true, true),
		TEXT_ONLY(true, false),
		RDF_FIRST(false, true),
		RDF_ONLY(false, false);

		final boolean textFirst;

		final boolean canFallback;

		Input(boolean textFirst, boolean canFallback) {
			this.textFirst = textFirst;
			this.canFallback = canFallback;
		}
	}

	private final Input input;

	public SPINParser() {
		this(Input.TEXT_FIRST);
	}

	public SPINParser(Input input) {
		this.input = input;
	}

	public Map<URI, RuleProperty> parseRules(StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Map<URI, RuleProperty> rules = new HashMap<URI, RuleProperty>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> rulePropIter = Statements.getSubjectURIs(RDFS.SUBPROPERTYOF, SPIN.RULE_PROPERTY, store);
		try {
			while(rulePropIter.hasNext()) {
				URI ruleProp = rulePropIter.next();
				List<URI> nextRules = new ArrayList<URI>();
				CloseableIteration<? extends URI, ? extends OpenRDFException> nextRuleIter = Statements.getObjectURIs(ruleProp, SPIN.NEXT_RULE_PROPERTY_PROPERTY, store);
				try {
					while(nextRuleIter.hasNext()) {
						nextRules.add(nextRuleIter.next());
					}
				}
				finally {
					nextRuleIter.close();
				}
				int maxCount = -1;
				CloseableIteration<? extends Literal, ? extends OpenRDFException> maxCountIter = Statements.getObjectLiterals(ruleProp, SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY, store);
				try {
					while(maxCountIter.hasNext()) {
						Literal maxCountValue = maxCountIter.next();
						try {
							maxCount = maxCountValue.intValue();
							break;
						}
						catch(NumberFormatException e) {
						}
					}
				}
				finally {
					nextRuleIter.close();
				}
				RuleProperty ruleProperty = new RuleProperty(ruleProp, nextRules, maxCount);
				rules.put(ruleProp, ruleProperty);
			}
		}
		finally {
			rulePropIter.close();
		}
		return rules;
	}

	public ParsedQuery parseQuery(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return parse(queryResource, null, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedGraphQuery)parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedTupleQuery)parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedBooleanQuery)parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedDescribeQuery)parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedQuery parse(Resource queryResource, URI queryType, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Statement actualTypeStmt = single(queryResource, RDF.TYPE, queryType, store);
		if (actualTypeStmt == null) {
			if (queryType != null) {
				throw new MalformedSPINException("No query of RDF type: " + queryType);
			}
			else {
				throw new MalformedSPINException("Query missing RDF type: " + queryResource);
			}
		}
		URI actualType = (URI)actualTypeStmt.getObject();

		ParsedQuery pq;
		if (input.textFirst) {
			pq = parseText(queryResource, actualType, store);
			if (pq == null && input.canFallback) {
				pq = parseRDF(queryResource, actualType, store);
			}
		}
		else {
			pq = parseRDF(queryResource, actualType, store);
			if (pq == null && input.canFallback) {
				pq = parseText(queryResource, actualType, store);
			}
		}
		if (pq == null) {
			throw new MalformedSPINException("Resource is not a query: " + queryResource);
		}
		return pq;
	}

	private ParsedQuery parseText(Resource queryResource, URI queryType, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Statement textStmt = single(queryResource, SP.TEXT_PROPERTY, null, store);
		if (textStmt != null) {
			return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, textStmt.getObject().stringValue(), null);
		}
		else {
			return null;
		}
	}

	private ParsedQuery parseRDF(Resource queryResource, URI queryType, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		throw new UnsupportedOperationException("TO DO");
	}

	private static Statement single(Resource subj, URI pred, Value obj, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Statement stmt;
		CloseableIteration<? extends Statement, ? extends OpenRDFException> stmts = store.getStatements(subj,
				pred, obj);
		try {
			if (stmts.hasNext()) {
				stmt = stmts.next();
				if (stmts.hasNext()) {
					throw new MalformedSPINException("Multiple statements for pattern " + subj + " " + pred + " "
							+ obj);
				}
			}
			else {
				stmt = null;
			}
		}
		finally {
			stmts.close();
		}
		return stmt;
	}
}
