/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.inferencer.fc.config;

import static org.eclipse.rdf4j.sail.inferencer.fc.config.CustomGraphQueryInferencerSchema.MATCHER_QUERY;
import static org.eclipse.rdf4j.sail.inferencer.fc.config.CustomGraphQueryInferencerSchema.QUERY_LANGUAGE;
import static org.eclipse.rdf4j.sail.inferencer.fc.config.CustomGraphQueryInferencerSchema.RULE_QUERY;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SP;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;

/**
 * Configuration handling for
 * {@link org.eclipse.rdf4j.sail.inferencer.fc.CustomGraphQueryInferencer}.
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

	private void addQueryNode(Model m, Resource implNode, IRI predicate, String queryText) {
		if (null != queryText) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			BNode queryNode = factory.createBNode();
			m.add(implNode, predicate, queryNode);
			m.add(queryNode, RDF.TYPE, SP.CONSTRUCT_CLASS);
			m.add(queryNode, SP.TEXT_PROPERTY, factory.createLiteral(queryText));
		}
	}
}