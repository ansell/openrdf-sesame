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
package org.openrdf.sail.lucene;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

import com.google.common.base.Supplier;

/**
 * A QuerySpec holds information extracted from a TupleExpr corresponding with a
 * single Lucene query. Access the patterns or use the get-methods to get the
 * names of the variables to bind.
 */
public class QuerySpec implements SearchQueryEvaluator {

	private final StatementPattern matchesPattern;

	private final StatementPattern queryPattern;

	private final StatementPattern propertyPattern;

	private final StatementPattern scorePattern;

	private final StatementPattern snippetPattern;

	private final StatementPattern typePattern;

	private final Resource subject;

	private final String queryString;

	private final IRI propertyURI;

	public QuerySpec(StatementPattern matchesPattern, StatementPattern queryPattern,
			StatementPattern propertyPattern, StatementPattern scorePattern, StatementPattern snippetPattern,
			StatementPattern typePattern, Resource subject, String queryString, IRI propertyURI)
	{
		this.matchesPattern = matchesPattern;
		this.queryPattern = queryPattern;
		this.propertyPattern = propertyPattern;
		this.scorePattern = scorePattern;
		this.snippetPattern = snippetPattern;
		this.typePattern = typePattern;
		this.subject = subject;
		this.queryString = queryString;
		this.propertyURI = propertyURI;
	}

	@Override
	public QueryModelNode getParentQueryModelNode() {
		return getMatchesPattern();
	}

	@Override
	public void updateQueryModelNodes(boolean hasResult) {
		Supplier<QueryModelNode> nodeFactory = hasResult ? new Supplier<QueryModelNode>() {

			@Override
			public QueryModelNode get() {
				return new SingletonSet();
			}
		} : new Supplier<QueryModelNode>() {

			@Override
			public QueryModelNode get() {
				return new EmptySet();
			}
		};

		replace(getMatchesPattern(), nodeFactory);
		replace(getQueryPattern(), nodeFactory);
		replace(getScorePattern(), nodeFactory);
		replace(getPropertyPattern(), nodeFactory);
		replace(getSnippetPattern(), nodeFactory);
		replace(getTypePattern(), nodeFactory);
	}

	/**
	 * Replace the given node with a new instance of the given replacement type.
	 * 
	 * @param pattern
	 *        the pattern to remove
	 * @param replacement
	 *        the replacement type
	 */
	private void replace(QueryModelNode node, Supplier<? extends QueryModelNode> replacement) {
		if (node != null) {
			node.replaceWith(replacement.get());
		}
	}

	public StatementPattern getMatchesPattern() {
		return matchesPattern;
	}

	/**
	 * return the name of the bound variable that should match the query
	 * 
	 * @return the name of the variable or null, if no name set
	 */
	public String getMatchesVariableName() {
		if (matchesPattern != null)
			return matchesPattern.getSubjectVar().getName();
		else
			return null;
	}

	public StatementPattern getQueryPattern() {
		return queryPattern;
	}

	public StatementPattern getPropertyPattern() {
		return propertyPattern;
	}

	public String getPropertyVariableName() {
		if (propertyPattern != null)
			return propertyPattern.getObjectVar().getName();
		else
			return null;
	}

	public StatementPattern getScorePattern() {
		return scorePattern;
	}

	/**
	 * The variable name associated with the query score
	 * 
	 * @return the name or null, if no score is queried in the pattern
	 */
	public String getScoreVariableName() {
		if (scorePattern != null)
			return scorePattern.getObjectVar().getName();
		else
			return null;
	}

	public StatementPattern getSnippetPattern() {
		return snippetPattern;
	}

	public String getSnippetVariableName() {
		if (snippetPattern != null)
			return snippetPattern.getObjectVar().getName();
		else
			return null;
	}

	public StatementPattern getTypePattern() {
		return typePattern;
	}

	/**
	 * the type of query, must equal {@link LuceneSailSchema#LUCENE_QUERY}. A
	 * null type is possible, but not valid.
	 * 
	 * @return the type of the Query or null, if no type assigned.
	 */
	public IRI getQueryType() {
		if (typePattern != null)
			return (IRI)typePattern.getObjectVar().getValue();
		else
			return null;
	}

	public Resource getSubject() {
		return subject;
	}

	/**
	 * return the literal expression of the query or null, if none set. (null
	 * values are possible, but not valid).
	 * 
	 * @return the query or null
	 */
	public String getQueryString() {
		// this should be the same as ((Literal)
		// queryPattern.getObjectVar().getValue()).getLabel();
		return queryString;
	}

	/**
	 * @return The URI of the property who's literal values should be searched,
	 *         or <code>null</code>
	 */
	public IRI getPropertyURI() {
		return propertyURI;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("QuerySpec\n");
		buffer.append("   queryString=\"" + queryString + "\"\n");
		buffer.append("   propertyURI=" + propertyURI + "\n");
		buffer.append("   subject=" + subject + "\n");
		append(matchesPattern, buffer);
		append(queryPattern, buffer);
		append(propertyPattern, buffer);
		append(scorePattern, buffer);
		append(snippetPattern, buffer);
		append(typePattern, buffer);
		return buffer.toString();
	}

	private void append(StatementPattern pattern, StringBuilder buffer) {
		if (pattern == null)
			return;

		buffer.append("   ");
		buffer.append("StatementPattern\n");
		append(pattern.getSubjectVar(), buffer);
		append(pattern.getPredicateVar(), buffer);
		append(pattern.getObjectVar(), buffer);
	}

	private void append(Var var, StringBuilder buffer) {
		buffer.append("      ");
		buffer.append(var.toString());
	}
}
