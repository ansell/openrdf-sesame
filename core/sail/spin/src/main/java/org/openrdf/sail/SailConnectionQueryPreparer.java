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
package org.openrdf.sail;

import org.openrdf.model.ValueFactory;
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
import org.openrdf.rio.ParserConfig;

/**
 * QueryPreparer for use with Sails.
 */
public class SailConnectionQueryPreparer implements QueryPreparer {

	private final SailConnection con;

	private final boolean includeInferred;

	private final ValueFactory vf;

	private final TripleSource source;

	private ParserConfig parserConfig = new ParserConfig();

	public SailConnectionQueryPreparer(SailConnection con, boolean includeInferred, ValueFactory vf) {
		this.con = con;
		this.includeInferred = includeInferred;
		this.vf = vf;
		this.source = new SailTripleSource(con, includeInferred, vf);
	}

	public void setParserConfig(ParserConfig parserConfig) {
		this.parserConfig = parserConfig;
	}

	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	@Override
	public BooleanQuery prepare(ParsedBooleanQuery askQuery) {
		BooleanQuery query = new SailConnectionBooleanQuery(askQuery, con);
		query.setIncludeInferred(includeInferred);
		return query;
	}

	@Override
	public TupleQuery prepare(ParsedTupleQuery tupleQuery) {
		TupleQuery query = new SailConnectionTupleQuery(tupleQuery, con);
		query.setIncludeInferred(includeInferred);
		return query;
	}

	@Override
	public GraphQuery prepare(ParsedGraphQuery graphQuery) {
		GraphQuery query = new SailConnectionGraphQuery(graphQuery, con, vf);
		query.setIncludeInferred(includeInferred);
		return query;
	}

	@Override
	public Update prepare(ParsedUpdate graphUpdate) {
		Update update = new SailConnectionUpdate(graphUpdate, con, vf, parserConfig);
		update.setIncludeInferred(includeInferred);
		return update;
	}

	@Override
	public TripleSource getTripleSource() {
		return source;
	}
}
