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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.sail.config.DelegatingSailImplConfigBase;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfig;

/**
 * Configuration handling for
 * {@link org.openrdf.sail.inferencer.fc.CustomGraphQueryInferencer}.
 * 
 * @author Dale Visser
 */
public final class CustomGraphQueryInferencerConfig extends DelegatingSailImplConfigBase {

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
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal language = GraphUtil.getOptionalObjectLiteral(graph, implNode, QUERY_LANGUAGE);
			if (null == language) {
				setQueryLanguage(QueryLanguage.SPARQL);
			}
			else {
				setQueryLanguage(QueryLanguage.valueOf(language.stringValue()));
				if (null == getQueryLanguage()) {
					throw new SailConfigException("Valid value required for " + QUERY_LANGUAGE
							+ " property, found " + language);
				}
			}
			Iterator<Value> iter = GraphUtil.getObjectIterator(graph, implNode, RULE_QUERY);
			if (iter.hasNext()) {
				setRuleQuery(GraphUtil.getUniqueObjectLiteral(graph, (Resource)iter.next(), SP.TEXT_PROPERTY).stringValue());
			}
			iter = GraphUtil.getObjectIterator(graph, implNode, MATCHER_QUERY);
			if (iter.hasNext()) {
				setMatcherQuery(GraphUtil.getUniqueObjectLiteral(graph, (Resource)iter.next(), SP.TEXT_PROPERTY).stringValue());
			}
		}
		catch (GraphUtilException e) {
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
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);
		if (null != language) {
			graph.add(implNode, QUERY_LANGUAGE, ValueFactoryImpl.getInstance().createLiteral(language.getName()));
		}
		addQueryNode(graph, implNode, RULE_QUERY, ruleQuery);
		addQueryNode(graph, implNode, MATCHER_QUERY, matcherQuery);
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

	private void addQueryNode(Graph graph, Resource implNode, URI predicate, String queryText) {
		if (null != queryText) {
			ValueFactory factory = ValueFactoryImpl.getInstance();
			BNode queryNode = factory.createBNode();
			graph.add(implNode, predicate, queryNode);
			graph.add(queryNode, RDF.TYPE, SP.CONSTRUCT_CLASS);
			graph.add(queryNode, SP.TEXT_PROPERTY, factory.createLiteral(queryText));
		}
	}
}