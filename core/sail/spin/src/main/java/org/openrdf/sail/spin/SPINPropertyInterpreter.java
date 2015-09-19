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
package org.openrdf.sail.spin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.helpers.BGPCollector;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.spin.SPINParser;


public class SPINPropertyInterpreter implements QueryOptimizer {

	private final TripleSource tripleSource;
	private final SPINParser parser;
	private final URI spinService;

	public SPINPropertyInterpreter(SPINParser parser, TripleSource tripleSource) {
		this.parser = parser;
		this.tripleSource = tripleSource;
		this.spinService = tripleSource.getValueFactory().createURI("spin:/");
	}

	@Override
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new PropertyScanner());
	}



	class PropertyScanner extends QueryModelVisitorBase<RuntimeException> {
		Map<Resource,StatementPattern> joins;

		private void processGraphPattern(List<StatementPattern> sps) {
			List<StatementPattern> magicProperties = new ArrayList<StatementPattern>();
			Map<SPKey,List<StatementPattern>> spIndex = new HashMap<SPKey,List<StatementPattern>>();
			for(StatementPattern sp : sps) {
				URI pred = (URI) sp.getPredicateVar().getValue();
				if(SPIN.CONSTRUCT_PROPERTY.equals(pred)) {
					magicProperties.add(sp);
				}
				else {
					SPKey k = new SPKey(sp.getSubjectVar().getName(), sp.getPredicateVar().getName());
					List<StatementPattern> v = spIndex.get(k);
					if(v == null) {
						v = new ArrayList<StatementPattern>(2);
						spIndex.put(k, v);
					}
					v.add(sp);
				}
			}

			for(StatementPattern sp : sps) {
				URI pred = (URI) sp.getPredicateVar().getValue();
				if(SPIN.CONSTRUCT_PROPERTY.equals(pred)) {
					EmptySet stub = new EmptySet();
					sp.replaceWith(stub);
					Var serviceRef = new Var("_const-spin-service-uri");
					serviceRef.setAnonymous(true);
					serviceRef.setConstant(true);
					serviceRef.setValue(spinService);
					Map<String,String> prefixDecls = Collections.emptyMap();
					Service service = new Service(serviceRef, sp, "", prefixDecls, null, false);
					stub.replaceWith(service);
				}
			}
		}

		@Override
		public void meet(Join node)
		{
			BGPCollector<RuntimeException> collector = new BGPCollector<RuntimeException>(this);
			node.visit(collector);
			processGraphPattern(collector.getStatementPatterns());
		}

		@Override
		public void meet(StatementPattern node)
		{
			processGraphPattern(Collections.singletonList(node));
		}
	}


	static class SPKey {
		final String subj;
		final String pred;

		SPKey(String subj, String pred) {
			this.subj = subj;
			this.pred = pred;
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof SPKey)) {
				return false;
			}
			SPKey k = (SPKey) o;
			return subj.equals(k.subj) && pred.equals(k.pred);
		}

		@Override
		public int hashCode() {
			return Objects.hash(subj, pred);
		}
	}
}
