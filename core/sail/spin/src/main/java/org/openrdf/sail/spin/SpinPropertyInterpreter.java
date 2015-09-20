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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.model.vocabulary.SPL;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.helpers.BGPCollector;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.spin.SpinParser;


public class SpinPropertyInterpreter implements QueryOptimizer {

	private final TripleSource tripleSource;
	private final SpinParser parser;
	private final URI spinService;

	public SpinPropertyInterpreter(SpinParser parser, TripleSource tripleSource) {
		this.parser = parser;
		this.tripleSource = tripleSource;
		this.spinService = tripleSource.getValueFactory().createURI(SpinFederatedServiceResolver.SPIN_SERVICE);
	}

	@Override
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new PropertyScanner());
	}



	class PropertyScanner extends QueryModelVisitorBase<RuntimeException> {
		Map<Resource,StatementPattern> joins;

		private void processGraphPattern(TupleExpr node, List<StatementPattern> sps) {
			TupleExpr magicPropNode = null;

			List<StatementPattern> magicProperties = new ArrayList<StatementPattern>();
			Map<String,Map<URI,List<StatementPattern>>> spIndex = new HashMap<String,Map<URI,List<StatementPattern>>>();

			for(StatementPattern sp : sps) {
				URI pred = (URI) sp.getPredicateVar().getValue();
				if(pred != null) {
					if(SPIN.CONSTRUCT_PROPERTY.equals(pred)) {
						magicProperties.add(sp);
					}
					else if(SPIN.SELECT_PROPERTY.equals(pred)) {
						magicProperties.add(sp);
					}
					else {
						// TODO check for defined magic properties
						// else below
						String subj = sp.getSubjectVar().getName();
						Map<URI,List<StatementPattern>> predMap = spIndex.get(subj);
						if(predMap == null) {
							predMap = new HashMap<URI,List<StatementPattern>>(8);
							spIndex.put(subj, predMap);
						}
						List<StatementPattern> v = predMap.get(pred);
						if(v == null) {
							v = new ArrayList<StatementPattern>(1);
							predMap.put(pred, v);
						}
						v.add(sp);
					}
				}
			}

			for(StatementPattern sp : magicProperties) {
				magicPropNode = join(magicPropNode, sp);

				TupleExpr subjList = list(sp.getSubjectVar().getName(), spIndex);
				if(subjList != null) {
					magicPropNode = new Join(magicPropNode, subjList);
				}

				TupleExpr objList = list(sp.getObjectVar().getName(), spIndex);
				if(objList != null) {
					magicPropNode = new Join(magicPropNode, objList);
				}
			}

			if(magicPropNode != null) {
				Var serviceRef = new Var("_const_spin_service_uri");
				serviceRef.setAnonymous(true);
				serviceRef.setConstant(true);
				serviceRef.setValue(spinService);
				Map<String,String> prefixDecls = new HashMap<String,String>(8);
				prefixDecls.put(SP.PREFIX, SP.NAMESPACE);
				prefixDecls.put(SPIN.PREFIX, SPIN.NAMESPACE);
				prefixDecls.put(SPL.PREFIX, SPL.NAMESPACE);
				Service service = new Service(serviceRef, magicPropNode, "", prefixDecls, null, false);
				Join join = new Join();
				node.replaceWith(join);
				join.setLeftArg(node);
				join.setRightArg(service);
			}
		}

		private TupleExpr join(TupleExpr node, TupleExpr toMove) {
			toMove.replaceWith(new SingletonSet());
			if(node != null) {
				node = new Join(node, toMove);
			}
			else {
				node = toMove;
			}
			return node;
		}

		private TupleExpr list(String subj, Map<String,Map<URI,List<StatementPattern>>> spIndex) {
			TupleExpr node = null;
			do
			{
				Map<URI,List<StatementPattern>> predMap = spIndex.get(subj);
				subj = null;
				if(predMap != null) {
					List<StatementPattern> firstStmts = predMap.get(RDF.FIRST);
					List<StatementPattern> restStmts = predMap.get(RDF.REST);
					if(firstStmts != null && restStmts != null) {
						for(StatementPattern sp : firstStmts) {
							node = join(node, sp);
						}
						for(StatementPattern sp : restStmts) {
							subj = sp.getObjectVar().getName();
							node = join(node, sp);
						}
						List<StatementPattern> typeStmts = predMap.get(RDF.TYPE);
						if(typeStmts != null) {
							for(StatementPattern sp : firstStmts) {
								Value type = sp.getObjectVar().getValue();
								if(RDFS.RESOURCE.equals(type) || RDF.LIST.equals(type)) {
									sp.replaceWith(new SingletonSet());
								}
							}
						}
					}
				}
			}
			while(subj != null);
			return node;
		}

		@Override
		public void meet(Join node)
		{
			BGPCollector<RuntimeException> collector = new BGPCollector<RuntimeException>(this);
			node.visit(collector);
			processGraphPattern(node, collector.getStatementPatterns());
		}

		@Override
		public void meet(StatementPattern node)
		{
			processGraphPattern(node, Collections.singletonList(node));
		}
	}
}
