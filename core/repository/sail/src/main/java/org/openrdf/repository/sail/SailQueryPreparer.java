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
package org.openrdf.repository.sail;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.Update;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.evaluation.RepositoryTripleSource;

/**
 * QueryPreparer for use with SailRepository.
 */
public class SailQueryPreparer implements QueryPreparer {

	private final SailRepositoryConnection con;

	private final boolean includeInferred;

	private final TripleSource source;

	public SailQueryPreparer(SailRepositoryConnection con, boolean includeInferred) {
		this.con = con;
		this.includeInferred = includeInferred;
		this.source = new RepositoryTripleSource(con, includeInferred);
	}

	@Override
	public BooleanQuery prepare(ParsedBooleanQuery askQuery) {
		BooleanQuery query = new SailBooleanQuery(askQuery, con);
		query.setIncludeInferred(includeInferred);
		return query;
	}

	@Override
	public TupleQuery prepare(ParsedTupleQuery tupleQuery) {
		TupleQuery query = new SailTupleQuery(tupleQuery, con);
		query.setIncludeInferred(includeInferred);
		return query;
	}

	@Override
	public GraphQuery prepare(ParsedGraphQuery graphQuery) {
		GraphQuery query = new SailGraphQuery(graphQuery, con);
		query.setIncludeInferred(includeInferred);
		return query;
	}

	@Override
	public Update prepare(ParsedUpdate graphUpdate) {
		Update update = new SailUpdate(graphUpdate, con);
		update.setIncludeInferred(includeInferred);
		return update;
	}

	@Override
	public TripleSource getTripleSource() {
		return source;
	}
}
