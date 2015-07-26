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
import java.util.Set;

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
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;

import com.google.common.collect.Sets;

public class SPINParser {

	private static final Set<URI> QUERY_TYPES = Sets.newHashSet(SP.QUERY_CLASS, SP.SELECT_CLASS,
			SP.CONSTRUCT_CLASS, SP.ASK_CLASS, SP.DESCRIBE_CLASS);

	private static final Set<URI> UPDATE_TYPES = Sets.newHashSet(SP.UPDATE_CLASS, SP.MODIFY_CLASS,
			SP.INSERT_DATA_CLASS, SP.DELETE_DATA_CLASS, SP.LOAD_CLASS, SP.CLEAR_CLASS, SP.CREATE_CLASS,
			SP.DROP_CLASS);

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

	public Map<URI, RuleProperty> parseRuleProperties(StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Map<URI, RuleProperty> rules = new HashMap<URI, RuleProperty>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> rulePropIter = Statements.getSubjectURIs(
				RDFS.SUBPROPERTYOF, SPIN.RULE_PROPERTY, store);
		try {
			while (rulePropIter.hasNext()) {
				URI ruleProp = rulePropIter.next();
				RuleProperty ruleProperty = new RuleProperty(ruleProp);

				List<URI> nextRules = getNextRules(ruleProp, store);
				ruleProperty.setNextRules(nextRules);

				int maxIterCount = getMaxIterationCount(ruleProp, store);
				ruleProperty.setMaxIterationCount(maxIterCount);

				boolean thisUnbound = isThisUnbound(ruleProp, store);
				ruleProperty.setThisUnbound(thisUnbound);

				rules.put(ruleProp, ruleProperty);
			}
		}
		finally {
			rulePropIter.close();
		}
		return rules;
	}

	private <X extends Exception> List<URI> getNextRules(Resource ruleProp, StatementSource<X> store)
		throws X
	{
		List<URI> nextRules = new ArrayList<URI>();
		CloseableIteration<? extends URI, X> iter = Statements.getObjectURIs(ruleProp,
				SPIN.NEXT_RULE_PROPERTY_PROPERTY, store);
		try {
			while (iter.hasNext()) {
				nextRules.add(iter.next());
			}
		}
		finally {
			iter.close();
		}
		return nextRules;
	}

	private <X extends Exception> int getMaxIterationCount(Resource ruleProp, StatementSource<X> store)
		throws X
	{
		int maxIterCount = -1;
		CloseableIteration<? extends Literal, X> iter = Statements.getObjectLiterals(ruleProp,
				SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY, store);
		try {
			while (iter.hasNext()) {
				Literal value = iter.next();
				try {
					maxIterCount = value.intValue();
					break;
				}
				catch (NumberFormatException e) {
				}
			}
		}
		finally {
			iter.close();
		}
		return maxIterCount;
	}

	private <X extends Exception> boolean isThisUnbound(Resource ruleProp, StatementSource<X> store)
		throws X
	{
		boolean thisUnbound = false;
		CloseableIteration<? extends Literal, X> iter = Statements.getObjectLiterals(ruleProp,
				SPIN.THIS_UNBOUND_PROPERTY, store);
		try {
			while (iter.hasNext()) {
				Literal value = iter.next();
				try {
					thisUnbound = value.booleanValue();
					break;
				}
				catch (IllegalArgumentException e) {
				}
			}
		}
		finally {
			iter.close();
		}
		return thisUnbound;
	}

	public ParsedOperation parse(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return parse(queryResource, null, store);
	}

	public ParsedQuery parseQuery(Resource queryResource, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedQuery)parse(queryResource, SP.QUERY_CLASS, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedGraphQuery)parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedTupleQuery)parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedBooleanQuery)parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		return (ParsedDescribeQuery)parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedOperation parse(Resource queryResource, URI queryType,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		List<URI> queryTypes = new ArrayList<URI>(4);
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				queryResource, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				queryTypes.add(typeIter.next());
			}
		}
		finally {
			typeIter.close();
		}
		if (queryTypes.isEmpty()) {
			if (queryType != null) {
				throw new MalformedSPINException("No query of RDF type: " + queryType);
			}
			else {
				throw new MalformedSPINException("Query missing RDF type: " + queryResource);
			}
		}

		ParsedOperation pq;
		if (input.textFirst) {
			pq = parseText(queryResource, queryTypes, store);
			if (pq == null && input.canFallback) {
				pq = parseRDF(queryResource, queryTypes, store);
			}
		}
		else {
			pq = parseRDF(queryResource, queryTypes, store);
			if (pq == null && input.canFallback) {
				pq = parseText(queryResource, queryTypes, store);
			}
		}
		if (pq == null) {
			throw new MalformedSPINException("Resource is not a query: " + queryResource);
		}
		return pq;
	}

	private ParsedOperation parseText(Resource queryResource, List<URI> queryTypes,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Statement textStmt = single(queryResource, SP.TEXT_PROPERTY, null, store);
		if (textStmt != null) {
			if (isQuery(queryTypes)) {
				return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, textStmt.getObject().stringValue(), null);
			}
			else {
				return QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, textStmt.getObject().stringValue(), null);
			}
		}
		else {
			return null;
		}
	}

	private ParsedOperation parseRDF(Resource queryResource, List<URI> queryTypes,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		throw new UnsupportedOperationException("TO DO");
	}

	private static boolean isQuery(List<URI> queryTypes) {
		for (URI queryType : queryTypes) {
			if (QUERY_TYPES.contains(queryType)) {
				return true;
			}
			else if (UPDATE_TYPES.contains(queryType)) {
				return false;
			}
		}
		return false;
	}

	private static Statement single(Resource subj, URI pred, Value obj,
			StatementSource<? extends OpenRDFException> store)
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
