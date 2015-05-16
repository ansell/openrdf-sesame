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
package org.openrdf.sesame.spin;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;

public class SPINParser {
	public enum Input {
		TEXT_FIRST(true, true), TEXT_ONLY(true, false), RDF_FIRST(false, true), RDF_ONLY(false, false);

		final boolean textFirst;
		final boolean canFallback;

		Input(boolean textFirst, boolean canFallback) {
			this.textFirst = textFirst;
			this.canFallback = canFallback;
		}
	}

	private final Input input;

	public SPINParser()
	{
		this(Input.TEXT_FIRST);
	}

	public SPINParser(Input input)
	{
		this.input = input;
	}

	public ParsedQuery parseQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return parse(queryResource, null, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedGraphQuery) parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedTupleQuery) parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedBooleanQuery) parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedDescribeQuery) parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedQuery parse(Resource queryResource, URI queryType, TripleSource store) throws OpenRDFException {
		Statement actualTypeStmt = single(queryResource, RDF.TYPE, queryType, store);
		if(actualTypeStmt == null) {
			if(queryType != null) {
				throw new MalformedSPINException("No query of RDF type: "+queryType);
			}
			else {
				throw new MalformedSPINException("Query missing RDF type: "+queryResource);
			}
		}
		URI actualType = (URI) actualTypeStmt.getObject();

		ParsedQuery pq;
		if(input.textFirst) {
			pq = parseText(queryResource, actualType, store);
			if(pq == null && input.canFallback) {
				pq = parseRDF(queryResource, actualType, store);
			}
		}
		else {
			pq = parseRDF(queryResource, actualType, store);
			if(pq == null && input.canFallback) {
				pq = parseText(queryResource, actualType, store);
			}
		}
		if(pq == null) {
			throw new MalformedSPINException("Resource is not a query: "+queryResource);
		}
		return pq;
	}

	private ParsedQuery parseText(Resource queryResource, URI queryType, TripleSource store) throws OpenRDFException {
		Statement textStmt = single(queryResource, SP.TEXT_PROPERTY, null, store);
		if(textStmt != null) {
			return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, textStmt.getObject().stringValue(), null);
		}
		else {
			return null;
		}
	}

	private ParsedQuery parseRDF(Resource queryResource, URI queryType, TripleSource store) throws OpenRDFException {
		throw new UnsupportedOperationException("TO DO");
	}


	private static Statement single(Resource subj, URI pred, Value obj, TripleSource store) throws OpenRDFException {
		Statement stmt;
		CloseableIteration<? extends Statement,QueryEvaluationException> stmts = store.getStatements(subj, pred, obj);
		try {
			if(stmts.hasNext()) {
				stmt = stmts.next();
				if(stmts.hasNext()) {
					throw new MalformedSPINException("Multiple statements for pattern "+subj+" "+pred+" "+obj);
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

	private static Statement requireSingle(Resource subj, URI pred, Value obj, TripleSource store) throws OpenRDFException {
		Statement stmt = single(subj, pred, obj, store);
		if(stmt == null) {
			throw new MalformedSPINException("Missing statement for pattern "+subj+" "+pred+" "+obj);
		}
		return stmt;
	}
}
