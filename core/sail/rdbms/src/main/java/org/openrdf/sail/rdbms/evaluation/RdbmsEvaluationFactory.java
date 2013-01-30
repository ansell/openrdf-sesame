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
package org.openrdf.sail.rdbms.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.rdbms.RdbmsTripleRepository;
import org.openrdf.sail.rdbms.schema.IdSequence;

/**
 * Creates an {@link RdbmsEvaluation}.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsEvaluationFactory {

	private QueryBuilderFactory factory;

	private RdbmsTripleRepository triples;

	private IdSequence ids;

	public void setQueryBuilderFactory(QueryBuilderFactory factory) {
		this.factory = factory;
	}

	public void setRdbmsTripleRepository(RdbmsTripleRepository triples) {
		this.triples = triples;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public RdbmsEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new RdbmsEvaluation(factory, triples, dataset, ids);
	}
}
