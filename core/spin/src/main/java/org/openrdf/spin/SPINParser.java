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
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
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

	private static final Set<URI> TEMPLATE_SUPER_TYPES = Sets.newHashSet(RDFS.RESOURCE, SP.SYSTEM_CLASS,
			SP.COMMAND_CLASS, SP.QUERY_CLASS, SP.UPDATE_CLASS, SPIN.TEMPLATES_CLASS,
			SPIN.ASK_TEMPLATES_CLASS, SPIN.SELECT_TEMPLATES_CLASS, SPIN.CONSTRUCT_TEMPLATES_CLASS, SPIN.UPDATE_TEMPLATES_CLASS);

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
		throws X, MalformedSPINException
	{
		Value v = singleValue(ruleProp,
				SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY, store);
		if(v == null) {
			return -1;
		}
		else if(v instanceof Literal) {
			try {
				return ((Literal)v).intValue();
			}
			catch (NumberFormatException e) {
				throw new MalformedSPINException("Value for "+SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY+" must be of datatype "+XMLSchema.INTEGER+": "+ruleProp);
			}
		}
		else {
			throw new MalformedSPINException("Non-literal value for "+SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY+": "+ruleProp);
		}
	}

	public boolean isThisUnbound(Resource subj, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Value v = singleValue(subj,
				SPIN.THIS_UNBOUND_PROPERTY, store);
		if(v == null) {
			return false;
		}
		else if(v instanceof Literal) {
			try {
				return ((Literal)v).booleanValue();
			}
			catch (IllegalArgumentException e) {
				throw new MalformedSPINException("Value for "+SPIN.THIS_UNBOUND_PROPERTY+" must be of datatype "+XMLSchema.BOOLEAN+": "+subj);
			}
		}
		else {
			throw new MalformedSPINException("Non-literal value for "+SPIN.THIS_UNBOUND_PROPERTY+": "+subj);
		}
	}

	public ConstraintViolation parseConstraintViolation(Resource subj, StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		Value labelValue = singleValue(subj, RDFS.LABEL, store);
		Value rootValue = singleValue(subj, SPIN.VIOLATION_ROOT_PROPERTY, store);
		Value pathValue = singleValue(subj, SPIN.VIOLATION_PATH_PROPERTY, store);
		Value valueValue = singleValue(subj, SPIN.VIOLATION_VALUE_PROPERTY, store);
		Value levelValue = singleValue(subj, SPIN.VIOLATION_LEVEL_PROPERTY, store);
		String label = (labelValue instanceof Literal) ? labelValue.stringValue() : null;
		String root = (rootValue instanceof Resource) ? rootValue.stringValue() : null;
		String path = (pathValue != null) ? pathValue.stringValue() : null;
		String value = (valueValue != null) ? valueValue.stringValue() : null;
		ConstraintViolationLevel level;
		if(levelValue == null) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if(SPIN.INFO_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.INFO;
		}
		else if(SPIN.WARNING_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.WARNING;
		}
		else if(SPIN.ERROR_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if(SPIN.FATAL_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.FATAL;
		}
		else {
			throw new MalformedSPINException("Invalid value "+levelValue+" for "+SPIN.VIOLATION_LEVEL_PROPERTY+": "+subj);
		}
		return new ConstraintViolation(label, root, path, value, level);
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
		boolean isTemplate = false;
		URI template = null;
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				queryResource, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if(queryType == null || queryType.equals(type)) {
					queryTypes.add(type);
				}
				if(SPIN.TEMPLATES_CLASS.equals(type)) {
					isTemplate = true;
				}
				else if(!TEMPLATE_SUPER_TYPES.contains(type)) {
					template = type;
				}
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
		if (isTemplate) {
			if(template == null) {
				throw new MalformedSPINException("Resource is not a template: " + queryResource);
			}
			Template tmpl = parseTemplate(template, queryTypes, store);
			// TODO
			BindingSet args = null;
			pq = new ParsedTemplateQuery(template, tmpl.getParsedOperation(), args);
		}
		else {
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
		}
		if (pq == null) {
			throw new MalformedSPINException("Resource is not a query: " + queryResource);
		}
		return pq;
	}

	private Template parseTemplate(URI tmplUri, List<URI> queryTypes,
			StatementSource<? extends OpenRDFException> store)
		throws OpenRDFException
	{
		// TODO
		return new Template(tmplUri);
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
			// else try the next queryType
		}
		return false;
	}

	private static <X extends Exception> Value singleValue(Resource subj, URI pred,
			StatementSource<X> store)
		throws X, MalformedSPINException
	{
		Statement stmt = single(subj, pred, null, store);
		return (stmt != null) ? stmt.getObject() : null;
	}

	private static <X extends Exception> Statement single(Resource subj, URI pred, Value obj,
			StatementSource<X> store)
		throws X, MalformedSPINException
	{
		Statement stmt;
		CloseableIteration<? extends Statement, X> stmts = store.getStatements(subj,
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
