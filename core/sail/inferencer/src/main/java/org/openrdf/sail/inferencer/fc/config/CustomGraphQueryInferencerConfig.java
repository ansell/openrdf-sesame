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
package org.openrdf.sail.inferencer.fc.config;

import static org.openrdf.sail.inferencer.fc.config.CustomGraphQueryInferencerSchema.MATCHER_QUERY;
import static org.openrdf.sail.inferencer.fc.config.CustomGraphQueryInferencerSchema.QUERY_LANGUAGE;
import static org.openrdf.sail.inferencer.fc.config.CustomGraphQueryInferencerSchema.RULE_QUERY;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.sail.config.AbstractDelegatingSailImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfig;

/**
 * Configuration handling for
 * {@link org.openrdf.sail.inferencer.fc.CustomGraphQueryInferencer}.
 * 
 * @author Dale Visser
 */
public final class CustomGraphQueryInferencerConfig extends AbstractDelegatingSailImplConfig {

	public static final Pattern SPARQL_PATTERN, SERQL_PATTERN;

	static {
		int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;
		SPARQL_PATTERN = Pattern.compile("^(.*construct\\s+)(\\{.*\\}\\s*)where.*$", flags);
		SERQL_PATTERN = Pattern.compile("^\\s*construct(\\s+.*)from\\s+.*(\\s+using\\s+namespace.*)$", flags);
	}

	private QueryLanguage language;

	private String ruleQuery, matcherQuery;

	public CustomGraphQueryInferencerConfig() {
		super(CustomGraphQueryInferencerFactory.SAIL_TYPE);
	}

	public CustomGraphQueryInferencerConfig(SailImplConfig delegate) {
		super(CustomGraphQueryInferencerFactory.SAIL_TYPE, delegate);
	}

	public void setQueryLanguage(QueryLanguage language) {
		this.language = language;
	}

	public QueryLanguage getQueryLanguage() {
		return language;
	}

	public void setRuleQuery(String ruleQuery) {
		this.ruleQuery = ruleQuery;
	}

	public String getRuleQuery() {
		return ruleQuery;
	}

	/**
	 * Set the optional matcher query.
	 * 
	 * @param matcherQuery
	 *        if null, internal value will be set to the empty string
	 */
	public void setMatcherQuery(String matcherQuery) {
		this.matcherQuery = null == matcherQuery ? "" : matcherQuery;
	}

	public String getMatcherQuery() {
		return matcherQuery;
	}

	@Override
	public void parse(Model m, Resource implNode)
		throws SailConfigException
	{
		super.parse(m, implNode);

		try {

			Optional<Literal> language = Models.objectLiteral(m.filter(implNode, QUERY_LANGUAGE, null));

			if (language.isPresent()) {
				setQueryLanguage(QueryLanguage.valueOf(language.get().stringValue()));
				if (null == getQueryLanguage()) {
					throw new SailConfigException(
							"Valid value required for " + QUERY_LANGUAGE + " property, found " + language.get());
				}
			}
			else {
				setQueryLanguage(QueryLanguage.SPARQL);
			}

			Optional<Resource> object = Models.objectResource(m.filter(implNode, RULE_QUERY, null));
			if (object.isPresent()) {
				Models.objectLiteral(m.filter(object.get(), SP.TEXT_PROPERTY, null)).ifPresent(
						lit -> setRuleQuery(lit.stringValue()));
			}

			object = Models.objectResource(m.filter(implNode, MATCHER_QUERY, null));
			if (object.isPresent()) {
				Models.objectLiteral(m.filter(object.get(), SP.TEXT_PROPERTY, null)).ifPresent(
						lit -> setMatcherQuery(lit.stringValue()));
			}
		}
		catch (ModelException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}

	@Override
	public void validate()
		throws SailConfigException
	{
		super.validate();
		if (null == language) {
			throw new SailConfigException("No query language specified for " + getType() + " Sail.");
		}
		if (null == ruleQuery) {
			throw new SailConfigException("No rule query specified for " + getType() + " Sail.");
		}
		else {
			try {
				QueryParserUtil.parseGraphQuery(language, ruleQuery, null);
			}
			catch (OpenRDFException e) {
				throw new SailConfigException("Problem occured parsing supplied rule query.", e);
			}
		}
		try {
			if (matcherQuery.trim().isEmpty()) {
				matcherQuery = buildMatcherQueryFromRuleQuery(language, ruleQuery);
			}
			QueryParserUtil.parseGraphQuery(language, matcherQuery, null);
		}
		catch (OpenRDFException e) {
			throw new SailConfigException("Problem occured parsing matcher query: " + matcherQuery, e);
		}
	}

	@Override
	public Resource export(Model m) {
		Resource implNode = super.export(m);
		if (null != language) {
			m.add(implNode, QUERY_LANGUAGE, SimpleValueFactory.getInstance().createLiteral(language.getName()));
		}
		addQueryNode(m, implNode, RULE_QUERY, ruleQuery);
		addQueryNode(m, implNode, MATCHER_QUERY, matcherQuery);
		return implNode;
	}

	public static String buildMatcherQueryFromRuleQuery(QueryLanguage language, String ruleQuery)
		throws MalformedQueryException
	{
		String result = "";
		if (QueryLanguage.SPARQL == language) {
			Matcher matcher = SPARQL_PATTERN.matcher(ruleQuery);
			if (matcher.matches()) {
				result = matcher.group(1) + "WHERE" + matcher.group(2);
			}
		}
		else if (QueryLanguage.SERQL == language) {
			Matcher matcher = SERQL_PATTERN.matcher(ruleQuery);
			if (matcher.matches()) {
				result = "CONSTRUCT * FROM" + matcher.group(1) + matcher.group(2);
			}
		}
		else {
			throw new IllegalStateException("language");
		}
		return result;
	}

	private void addQueryNode(Graph graph, Resource implNode, IRI predicate, String queryText) {
		if (null != queryText) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			BNode queryNode = factory.createBNode();
			graph.add(implNode, predicate, queryNode);
			graph.add(queryNode, RDF.TYPE, SP.CONSTRUCT_CLASS);
			graph.add(queryNode, SP.TEXT_PROPERTY, factory.createLiteral(queryText));
		}
	}
}