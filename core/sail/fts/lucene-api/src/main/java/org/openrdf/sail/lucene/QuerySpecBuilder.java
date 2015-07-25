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
package org.openrdf.sail.lucene;

import static org.openrdf.model.vocabulary.RDF.TYPE;
import static org.openrdf.sail.lucene.LuceneSailSchema.LUCENE_QUERY;
import static org.openrdf.sail.lucene.LuceneSailSchema.MATCHES;
import static org.openrdf.sail.lucene.LuceneSailSchema.PROPERTY;
import static org.openrdf.sail.lucene.LuceneSailSchema.QUERY;
import static org.openrdf.sail.lucene.LuceneSailSchema.SCORE;
import static org.openrdf.sail.lucene.LuceneSailSchema.SNIPPET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A QueryInterpreter creates a set of QuerySpecs based on Lucene-related
 * StatementPatterns that it finds in a TupleExpr.
 * <p>
 * QuerySpecs will only be created when the set of StatementPatterns is complete
 * (i.e. contains at least a matches and a query statement connected properly)
 * and correct (query pattern has a literal object, matches a resource subject,
 * etc.).
 */
public class QuerySpecBuilder implements SearchQueryInterpreter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final boolean incompleteQueryFails;

	/**
	 * Initialize a new QuerySpecBuilder
	 * 
	 * @param incompleteQueryFails
	 *        see {@link LuceneSail#isIncompleteQueryFails()}
	 */
	public QuerySpecBuilder(boolean incompleteQueryFails) {
		this.incompleteQueryFails = incompleteQueryFails;
	}

	/**
	 * Returns a set of QuerySpecs embodying all necessary information to perform
	 * the Lucene query embedded in a TupleExpr.
	 * To be removed, prefer {@link process(TupleExpr tupleExpr, BindingSet bindings, Collection<SearchQueryEvaluator> result)}.
	 */
	@Deprecated
	public Set<QuerySpec> process(TupleExpr tupleExpr, BindingSet bindings)
		throws SailException
	{
		HashSet<QuerySpec> result = new HashSet<QuerySpec>();
		process(tupleExpr, bindings, (Collection<SearchQueryEvaluator>) (Collection<?>) result);
		return result;
	}

	/**
	 * Appends a set of QuerySpecs embodying all necessary information to perform
	 * the Lucene query embedded in a TupleExpr.
	 */
	@Override
	public void process(TupleExpr tupleExpr, BindingSet bindings, Collection<SearchQueryEvaluator> result)
		throws SailException
	{
		// find Lucene-related StatementPatterns
		PatternFilter filter = new PatternFilter();
		tupleExpr.visit(filter);

		// loop over all matches statements
		for (StatementPattern matchesPattern : filter.matchesPatterns) {
			// the subject of the matches statements should be a variable or a
			// Resource
			Var subjectVar = matchesPattern.getSubjectVar();
			Value subjectValue = subjectVar.hasValue() ? subjectVar.getValue()
					: bindings.getValue(subjectVar.getName());

			if (subjectValue != null && !(subjectValue instanceof Resource)) {
				failOrWarn(MATCHES + " properties should have Resource subjects: " + subjectVar.getValue());
				continue;
			}

			Resource subject = (Resource)subjectValue;

			// the matches var should have no value
			Var matchesVar = matchesPattern.getObjectVar();
			if (matchesVar.hasValue()) {
				failOrWarn(MATCHES + " properties should have variable objects: " + matchesVar.getValue());
				continue;
			}

			// find the relevant outgoing patterns
			StatementPattern typePattern, queryPattern, propertyPattern, scorePattern, snippetPattern;

			try {
				typePattern = getPattern(matchesVar, filter.typePatterns);
				queryPattern = getPattern(matchesVar, filter.queryPatterns);
				propertyPattern = getPattern(matchesVar, filter.propertyPatterns);
				scorePattern = getPattern(matchesVar, filter.scorePatterns);
				snippetPattern = getPattern(matchesVar, filter.snippetPatterns);
			}
			catch (IllegalArgumentException e) {
				failOrWarn(e);
				continue;
			}

			// fetch the query String
			String queryString = null;

			if (queryPattern != null) {
				Var queryVar = queryPattern.getObjectVar();
				Value queryValue = queryVar.hasValue() ? queryVar.getValue()
						: bindings.getValue(queryVar.getName());

				if (queryValue instanceof Literal) {
					queryString = ((Literal)queryValue).getLabel();
				}
			}

			if (queryString == null) {
				failOrWarn("missing query string for Lucene query specification");
				continue;
			}

			// check property restriction or variable
			URI propertyURI = null;
			if (propertyPattern != null) {
				Var propertyVar = propertyPattern.getObjectVar();
				Value propertyValue = propertyVar.hasValue() ? propertyVar.getValue()
						: bindings.getValue(propertyVar.getName());

				// if property is a restriction, it should be an URI
				if (propertyValue instanceof URI) {
					propertyURI = (URI)propertyValue;
				}
				// otherwise, it should be a variable
				else if (propertyValue != null) {
					failOrWarn(PROPERTY + " should have a property URI or a variable as object: "
							+ propertyVar.getValue());
					continue;
				}
			}

			// check the score variable, if any
			Var scoreVar = scorePattern == null ? null : scorePattern.getObjectVar();
			if (scoreVar != null && scoreVar.hasValue()) {
				failOrWarn(SCORE + " should have a variable as object: " + scoreVar.getValue());
				continue;
			}

			// check the snippet variable, if any
			Var snippetVar = snippetPattern == null ? null : snippetPattern.getObjectVar();
			if (snippetVar != null && snippetVar.hasValue()) {
				failOrWarn(SNIPPET + " should have a variable as object: " + snippetVar.getValue());
				continue;
			}

			// check type pattern
			if (typePattern == null) {
				logger.debug("Query variable '{}' has not rdf:type, assuming {}", subject, LUCENE_QUERY);
			}

			// register a QuerySpec with these details
			result.add(new QuerySpec(matchesPattern, queryPattern, propertyPattern, scorePattern,
					snippetPattern, typePattern, subject, queryString, propertyURI));
		}

		// fail on superflous typePattern, query, score, or snippet patterns.
	}

	private void failOrWarn(Exception exception)
		throws SailException
	{
		if (incompleteQueryFails) {
			throw exception instanceof SailException ? (SailException)exception : new SailException(exception);
		}
		else {
			logger.warn(exception.getMessage(), exception);
		}
	}

	private void failOrWarn(String message)
		throws SailException
	{
		if (incompleteQueryFails) {
			throw new SailException("Invalid Text Query: " + message);
		}
		else {
			logger.warn(message);
		}
	}

	/**
	 * Returns the StatementPattern, if any, from the specified Collection that
	 * has the specified subject var. If multiple StatementPatterns exist with
	 * this subject var, an IllegalArgumentException is thrown. It also removes
	 * the patter from the arraylist, to be able to check if some patterns are
	 * added without a MATCHES property.
	 */
	private StatementPattern getPattern(Var subjectVar, ArrayList<StatementPattern> patterns)
		throws IllegalArgumentException
	{
		StatementPattern result = null;

		for (StatementPattern pattern : patterns) {
			if (pattern.getSubjectVar().equals(subjectVar)) {
				if (result == null) {
					result = pattern;
				}
				else {
					throw new IllegalArgumentException("multiple StatementPatterns with the same subject: "
							+ result + ", " + pattern);
				}
			}
		}
		// remove the result from the list, to filter out superflous patterns
		if (result != null)
			patterns.remove(result);
		return result;
	}

	private static class PatternFilter extends QueryModelVisitorBase<RuntimeException> {

		public ArrayList<StatementPattern> typePatterns = new ArrayList<StatementPattern>();

		public ArrayList<StatementPattern> matchesPatterns = new ArrayList<StatementPattern>();

		public ArrayList<StatementPattern> queryPatterns = new ArrayList<StatementPattern>();

		public ArrayList<StatementPattern> propertyPatterns = new ArrayList<StatementPattern>();

		public ArrayList<StatementPattern> scorePatterns = new ArrayList<StatementPattern>();

		public ArrayList<StatementPattern> snippetPatterns = new ArrayList<StatementPattern>();

		/**
		 * Method implementing the visitor pattern that gathers all statements
		 * using a predicate from the LuceneSail's namespace.
		 */
		@Override
		public void meet(StatementPattern node) {
			Value predicate = node.getPredicateVar().getValue();

			if (MATCHES.equals(predicate)) {
				matchesPatterns.add(node);
			}
			else if (QUERY.equals(predicate)) {
				queryPatterns.add(node);
			}
			else if (PROPERTY.equals(predicate)) {
				propertyPatterns.add(node);
			}
			else if (SCORE.equals(predicate)) {
				scorePatterns.add(node);
			}
			else if (SNIPPET.equals(predicate)) {
				snippetPatterns.add(node);
			}
			else if (TYPE.equals(predicate)) {
				Value object = node.getObjectVar().getValue();
				if (LUCENE_QUERY.equals(object)) {
					typePatterns.add(node);
				}
			}
		}
	}
}
