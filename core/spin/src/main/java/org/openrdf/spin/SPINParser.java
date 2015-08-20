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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.spin.util.Statements;

import com.google.common.collect.Sets;

public class SPINParser {

	private static final Set<URI> QUERY_TYPES = Sets.newHashSet(SP.SELECT_CLASS, SP.CONSTRUCT_CLASS,
			SP.ASK_CLASS, SP.DESCRIBE_CLASS);

	private static final Set<URI> UPDATE_TYPES = Sets.newHashSet(SP.MODIFY_CLASS, SP.INSERT_DATA_CLASS,
			SP.DELETE_DATA_CLASS, SP.LOAD_CLASS, SP.CLEAR_CLASS, SP.CREATE_CLASS, SP.DROP_CLASS);

	private static final Set<URI> COMMAND_TYPES = Sets.union(QUERY_TYPES, UPDATE_TYPES);

	private static final Set<URI> NON_TEMPLATES = Sets.newHashSet(RDFS.RESOURCE, SP.SYSTEM_CLASS,
			SP.COMMAND_CLASS, SP.QUERY_CLASS, SP.UPDATE_CLASS, SPIN.MODULES_CLASS, SPIN.TEMPLATES_CLASS,
			SPIN.ASK_TEMPLATES_CLASS, SPIN.SELECT_TEMPLATES_CLASS, SPIN.CONSTRUCT_TEMPLATES_CLASS,
			SPIN.UPDATE_TEMPLATES_CLASS, SPIN.RULE_CLASS);

	private static final Set<URI> TEMPLATE_TYPES = Sets.newHashSet(SPIN.ASK_TEMPLATE_CLASS,
			SPIN.SELECT_TEMPLATE_CLASS, SPIN.CONSTRUCT_TEMPLATE_CLASS, SPIN.UPDATE_TEMPLATE_CLASS);

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

	public Map<URI, RuleProperty> parseRuleProperties(TripleSource store)
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

	private List<URI> getNextRules(Resource ruleProp, TripleSource store)
		throws OpenRDFException
	{
		List<URI> nextRules = new ArrayList<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> iter = Statements.getObjectURIs(ruleProp,
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

	private int getMaxIterationCount(Resource ruleProp, TripleSource store)
		throws OpenRDFException
	{
		Value v = Statements.singleValue(ruleProp, SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY, store);
		if (v == null) {
			return -1;
		}
		else if (v instanceof Literal) {
			try {
				return ((Literal)v).intValue();
			}
			catch (NumberFormatException e) {
				throw new MalformedSPINException("Value for " + SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY
						+ " must be of datatype " + XMLSchema.INTEGER + ": " + ruleProp);
			}
		}
		else {
			throw new MalformedSPINException("Non-literal value for "
					+ SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY + ": " + ruleProp);
		}
	}

	public boolean isThisUnbound(Resource subj, TripleSource store)
		throws OpenRDFException
	{
		Value v = Statements.singleValue(subj, SPIN.THIS_UNBOUND_PROPERTY, store);
		if (v == null) {
			return false;
		}
		else if (v instanceof Literal) {
			try {
				return ((Literal)v).booleanValue();
			}
			catch (IllegalArgumentException e) {
				throw new MalformedSPINException("Value for " + SPIN.THIS_UNBOUND_PROPERTY
						+ " must be of datatype " + XMLSchema.BOOLEAN + ": " + subj);
			}
		}
		else {
			throw new MalformedSPINException("Non-literal value for " + SPIN.THIS_UNBOUND_PROPERTY + ": " + subj);
		}
	}

	public ConstraintViolation parseConstraintViolation(Resource subj, TripleSource store)
		throws OpenRDFException
	{
		Value labelValue = Statements.singleValue(subj, RDFS.LABEL, store);
		Value rootValue = Statements.singleValue(subj, SPIN.VIOLATION_ROOT_PROPERTY, store);
		Value pathValue = Statements.singleValue(subj, SPIN.VIOLATION_PATH_PROPERTY, store);
		Value valueValue = Statements.singleValue(subj, SPIN.VIOLATION_VALUE_PROPERTY, store);
		Value levelValue = Statements.singleValue(subj, SPIN.VIOLATION_LEVEL_PROPERTY, store);
		String label = (labelValue instanceof Literal) ? labelValue.stringValue() : null;
		String root = (rootValue instanceof Resource) ? rootValue.stringValue() : null;
		String path = (pathValue != null) ? pathValue.stringValue() : null;
		String value = (valueValue != null) ? valueValue.stringValue() : null;
		ConstraintViolationLevel level;
		if (levelValue == null) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if (SPIN.INFO_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.INFO;
		}
		else if (SPIN.WARNING_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.WARNING;
		}
		else if (SPIN.ERROR_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if (SPIN.FATAL_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.FATAL;
		}
		else {
			throw new MalformedSPINException("Invalid value " + levelValue + " for "
					+ SPIN.VIOLATION_LEVEL_PROPERTY + ": " + subj);
		}
		return new ConstraintViolation(label, root, path, value, level);
	}

	public ParsedOperation parse(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return parse(queryResource, SP.COMMAND_CLASS, store);
	}

	public ParsedQuery parseQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedQuery)parse(queryResource, SP.QUERY_CLASS, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedGraphQuery)parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedTupleQuery)parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedBooleanQuery)parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedDescribeQuery)parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedOperation parse(Resource queryResource, URI queryClass, TripleSource store)
		throws OpenRDFException
	{
		Boolean isQueryElseTemplate = null;
		Set<URI> possibleQueryTypes = new HashSet<URI>();
		Set<URI> possibleTemplates = new HashSet<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				queryResource, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if (isQueryElseTemplate == null && SPIN.TEMPLATES_CLASS.equals(type)) {
					isQueryElseTemplate = Boolean.FALSE;
				}
				else if ((isQueryElseTemplate == null || isQueryElseTemplate == Boolean.TRUE)
						&& COMMAND_TYPES.contains(type))
				{
					isQueryElseTemplate = Boolean.TRUE;
					possibleQueryTypes.add(type);
				}
				else if ((isQueryElseTemplate == null || isQueryElseTemplate == Boolean.FALSE)
						&& !NON_TEMPLATES.contains(type))
				{
					possibleTemplates.add(type);
				}
			}
		}
		finally {
			typeIter.close();
		}

		ParsedOperation parsedOp;
		if (isQueryElseTemplate == null) {
			throw new MalformedSPINException("Missing RDF type: " + queryResource);
		}
		else if (isQueryElseTemplate == Boolean.TRUE) {
			// command (query or update)
			if (possibleQueryTypes.size() > 1) {
				throw new MalformedSPINException("Incompatible RDF types for command: " + queryResource
						+ " has types " + possibleQueryTypes);
			}

			URI queryType = possibleQueryTypes.iterator().next();

			if (input.textFirst) {
				parsedOp = parseText(queryResource, queryType, store);
				if (parsedOp == null && input.canFallback) {
					parsedOp = parseRDF(queryResource, queryType, store);
				}
			}
			else {
				parsedOp = parseRDF(queryResource, queryType, store);
				if (parsedOp == null && input.canFallback) {
					parsedOp = parseText(queryResource, queryType, store);
				}
			}

			if (parsedOp == null) {
				throw new MalformedSPINException("Command is not parsable: " + queryResource);
			}
		}
		else {
			// template
			if (possibleTemplates.size() > 1) {
				for (Iterator<URI> templateIter = possibleTemplates.iterator(); templateIter.hasNext();) {
					URI template = templateIter.next();
					boolean isTemplateType = false;
					CloseableIteration<? extends URI, ? extends OpenRDFException> tmplTypeIter = Statements.getObjectURIs(
							template, RDF.TYPE, store);
					try {
						while (tmplTypeIter.hasNext()) {
							URI tmplType = tmplTypeIter.next();
							if (TEMPLATE_TYPES.contains(tmplType)) {
								isTemplateType = true;
								break;
							}
						}
					}
					finally {
						tmplTypeIter.close();
					}
					if (!isTemplateType) {
						templateIter.remove();
					}
				}
			}

			if (possibleTemplates.isEmpty()) {
				throw new MalformedSPINException("Template missing RDF type: " + queryResource);
			}
			else if (possibleTemplates.size() > 1) {
				throw new MalformedSPINException("Incompatible RDF types for template: " + queryResource
						+ " has types " + possibleTemplates);
			}

			URI templateResource = possibleTemplates.iterator().next();

			Template tmpl = parseTemplate(templateResource, queryClass, store);
			// TODO
			BindingSet args = null;
			parsedOp = new ParsedTemplateQuery(templateResource, tmpl.getParsedOperation(), args);
		}

		return parsedOp;
	}

	private Template parseTemplate(URI tmplUri, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		Set<URI> possibleTmplTypes = new HashSet<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				tmplUri, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if (TEMPLATE_TYPES.contains(type)) {
					possibleTmplTypes.add(type);
				}
			}
		}
		finally {
			typeIter.close();
		}

		if (possibleTmplTypes.isEmpty()) {
			throw new MalformedSPINException("Template missing RDF type: " + tmplUri);
		}
		else if (possibleTmplTypes.size() > 1) {
			throw new MalformedSPINException("Incompatible RDF types for template: " + tmplUri + " has types "
					+ possibleTmplTypes);
		}

		URI tmplType = possibleTmplTypes.iterator().next();

		Set<URI> compatibleTmplTypes;
		if (SP.QUERY_CLASS.equals(queryType)) {
			compatibleTmplTypes = Sets.newHashSet(SPIN.ASK_TEMPLATE_CLASS, SPIN.SELECT_TEMPLATE_CLASS,
					SPIN.CONSTRUCT_TEMPLATE_CLASS);
		}
		else if (SP.UPDATE_CLASS.equals(queryType) || UPDATE_TYPES.contains(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.UPDATE_TEMPLATE_CLASS);
		}
		else if (SP.ASK_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.ASK_TEMPLATE_CLASS);
		}
		else if (SP.SELECT_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.SELECT_TEMPLATE_CLASS);
		}
		else if (SP.CONSTRUCT_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.CONSTRUCT_TEMPLATE_CLASS);
		}
		else {
			compatibleTmplTypes = TEMPLATE_TYPES;
		}
		if (!compatibleTmplTypes.contains(tmplType)) {
			throw new MalformedSPINException("Template type " + tmplType + " is incompatible with command type "
					+ queryType);
		}

		Template tmpl = new Template(tmplUri);

		Value body = Statements.singleValue(tmplUri, SPIN.BODY_PROPERTY, store);
		if (!(body instanceof Resource)) {
			throw new MalformedSPINException("Template body is not a resource: " + body);
		}
		ParsedOperation op = parse((Resource)body, queryType, store);
		tmpl.setParsedOperation(op);

		// TODO args

		return tmpl;
	}

	private ParsedOperation parseText(Resource queryResource, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		Value text = Statements.singleValue(queryResource, SP.TEXT_PROPERTY, store);
		if (text != null) {
			if (QUERY_TYPES.contains(queryType)) {
				return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, text.stringValue(), null);
			}
			else if (UPDATE_TYPES.contains(queryType)) {
				return QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, text.stringValue(), null);
			}
			else {
				throw new MalformedSPINException("Unrecognised command type: " + queryType);
			}
		}
		else {
			return null;
		}
	}

	private ParsedOperation parseRDF(Resource queryResource, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		throw new UnsupportedOperationException("TO DO");
	}
}
