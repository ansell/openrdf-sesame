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
package org.eclipse.rdf4j.queryrender.builder;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

/**
 * <p>
 * Factory class for obtaining instances of {@link QueryBuilder} objects for the
 * various types of queries.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class QueryBuilderFactory {

	/**
	 * Create a QueryBuilder for creating a select query
	 * 
	 * @return a select QueryBuilder
	 */
	public static QueryBuilder<ParsedBooleanQuery> ask() {
		return new AbstractQueryBuilder<ParsedBooleanQuery>(new ParsedBooleanQuery());
	}

	/**
	 * Create a QueryBuilder for creating a select query
	 * 
	 * @return a select QueryBuilder
	 */
	public static QueryBuilder<ParsedTupleQuery> select() {
		return new AbstractQueryBuilder<ParsedTupleQuery>(new ParsedTupleQuery());
	}

	/**
	 * Create a QueryBuilder for creating a select query
	 * 
	 * @param theProjectionVars
	 *        the list of elements in the projection of the query
	 * @return a select query builder
	 */
	public static QueryBuilder<ParsedTupleQuery> select(String... theProjectionVars) {
		QueryBuilder<ParsedTupleQuery> aBuilder = new AbstractQueryBuilder<ParsedTupleQuery>(
				new ParsedTupleQuery());
		aBuilder.addProjectionVar(theProjectionVars);

		return aBuilder;
	}

	/**
	 * Create a QueryBuilder for building a construct query
	 * 
	 * @return a construct QueryBuilder
	 */
	public static QueryBuilder<ParsedGraphQuery> construct() {
		return new AbstractQueryBuilder<ParsedGraphQuery>(new ParsedGraphQuery());
	}

	/**
	 * Create a QueryBuilder for creating a describe query
	 * 
	 * @param theValues
	 *        the specific bound URI values to be described
	 * @return a describe query builder
	 */
	public static QueryBuilder<ParsedGraphQuery> describe(Resource... theValues) {
		return describe(null, theValues);
	}

	/**
	 * Create a QueryBuilder for creating a describe query
	 * 
	 * @param theVars
	 *        the variables to be described
	 * @param theValues
	 *        the specific bound URI values to be described
	 * @return a describe query builder
	 */
	public static QueryBuilder<ParsedGraphQuery> describe(String[] theVars, Resource... theValues) {
		QueryBuilder<ParsedGraphQuery> aBuilder = new AbstractQueryBuilder<ParsedGraphQuery>(
				new ParsedGraphQuery());

		aBuilder.reduced();
		aBuilder.addProjectionVar("descr_subj", "descr_pred", "descr_obj");
		GroupBuilder<?, ?> aGroup = aBuilder.group();

		if (theVars != null) {
			for (String aVar : theVars) {
				Var aVarObj = new Var(aVar);
				aVarObj.setAnonymous(true);

				aGroup.filter().or(new SameTerm(aVarObj, new Var("descr_subj")),
						new SameTerm(aVarObj, new Var("descr_obj")));
			}
		}

		if (theValues != null) {
			for (Resource aVar : theValues) {
				Var aSubjVar = new Var("descr_subj");
				aSubjVar.setAnonymous(true);

				Var aObjVar = new Var("descr_obj");
				aObjVar.setAnonymous(true);

				aGroup.filter().or(new SameTerm(new ValueConstant(aVar), aSubjVar),
						new SameTerm(new ValueConstant(aVar), aObjVar));
			}
		}

		aGroup.atom("descr_subj", "descr_pred", "descr_obj");

		return aBuilder;
	}
}
