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

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsTripleRepository;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;

/**
 * Proxies request to a {@link RdbmsTripleRepository}.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsTripleSource implements TripleSource {

	private RdbmsTripleRepository triples;

	public RdbmsTripleSource(RdbmsTripleRepository triples) {
		super();
		this.triples = triples;
	}

	public RdbmsValueFactory getValueFactory() {
		return triples.getValueFactory();
	}

	public CloseableIteration getStatements(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException
	{
		try {
			RdbmsValueFactory vf = triples.getValueFactory();
			RdbmsResource s = vf.asRdbmsResource(subj);
			RdbmsURI p = vf.asRdbmsURI(pred);
			RdbmsValue o = vf.asRdbmsValue(obj);
			RdbmsResource[] c = vf.asRdbmsResource(contexts);
			return triples.find(s, p, o, c);
		}
		catch (SailException e) {
			throw new QueryEvaluationException(e);
		}
	}

}
