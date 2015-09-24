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
import org.openrdf.query.algebra.evaluation.federation.FederatedService;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverBase;
import org.openrdf.query.algebra.evaluation.federation.TupleFunctionFederatedService;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.algebra.evaluation.function.TupleFunctionRegistry;
import org.openrdf.query.algebra.helpers.BGPCollector;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.TupleExprs;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.queryrender.sparql.SPARQLQueryRenderer;
import org.openrdf.spin.SpinParser;
import org.openrdf.spin.function.ConstructTupleFunction;
import org.openrdf.spin.function.SelectTupleFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpinMagicPropertyInterpreter implements QueryOptimizer {
	private static final Logger logger = LoggerFactory.getLogger(SpinMagicPropertyInterpreter.class);

	private static final String SPIN_SERVICE = "spin:/";

	private final TripleSource tripleSource;
	private final SpinParser parser;
	private final TupleFunctionRegistry tupleFunctionRegistry;
	private final FederatedServiceResolverBase serviceResolver;

	public SpinMagicPropertyInterpreter(SpinParser parser, TripleSource tripleSource, TupleFunctionRegistry tupleFunctionRegistry, FederatedServiceResolverBase serviceResolver) {
		this.parser = parser;
		this.tripleSource = tripleSource;
		this.tupleFunctionRegistry = tupleFunctionRegistry;
		this.serviceResolver = serviceResolver;
		if(!tupleFunctionRegistry.has(SPIN.CONSTRUCT_PROPERTY.stringValue())) {
			tupleFunctionRegistry.add(new ConstructTupleFunction(parser));
		}
		if(!tupleFunctionRegistry.has(SPIN.SELECT_PROPERTY.stringValue())) {
			tupleFunctionRegistry.add(new SelectTupleFunction(parser));
		}
	}

	@Override
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new PropertyScanner());
	}



	class PropertyScanner extends QueryModelVisitorBase<RuntimeException> {
		Map<Resource,StatementPattern> joins;

		private void processGraphPattern(List<StatementPattern> sps) {
			List<StatementPattern> magicProperties = new ArrayList<StatementPattern>();
			Map<String,Map<URI,List<StatementPattern>>> spIndex = new HashMap<String,Map<URI,List<StatementPattern>>>();

			for(StatementPattern sp : sps) {
				URI pred = (URI) sp.getPredicateVar().getValue();
				if(pred != null) {
					if(tupleFunctionRegistry.has(pred.stringValue())) {
						magicProperties.add(sp);
					}
					else {
						// TODO check for defined magic properties and add to registry
						// else below
						// normal statement
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
				URI magicPropUri = (URI) sp.getPredicateVar().getValue();
				String magicProp = magicPropUri.stringValue();
				if(!serviceResolver.hasService(magicProp)) {
					TupleFunction func = tupleFunctionRegistry.get(magicProp);
					FederatedService fs = new TupleFunctionFederatedService(func, tripleSource.getValueFactory());
					serviceResolver.registerService(magicProp, fs);
				}

				SingletonSet stub = new SingletonSet();
				sp.replaceWith(stub);
				TupleExpr magicPropNode = sp;

				TupleExpr subjList = list(sp.getSubjectVar().getName(), spIndex);
				if(subjList != null) {
					magicPropNode = new Join(magicPropNode, subjList);
				}

				TupleExpr objList = list(sp.getObjectVar().getName(), spIndex);
				if(objList != null) {
					magicPropNode = new Join(magicPropNode, objList);
				}

				Var serviceRef = TupleExprs.createConstVar(magicPropUri);
				String exprString;
				try {
					exprString = new SPARQLQueryRenderer().render(new ParsedTupleQuery(magicPropNode));
					exprString = exprString.substring(exprString.indexOf('{')+1, exprString.lastIndexOf('}'));
				}
				catch(Exception e) {
					throw new RuntimeException(e);
				}
				Map<String,String> prefixDecls = new HashMap<String,String>(8);
				prefixDecls.put(SP.PREFIX, SP.NAMESPACE);
				prefixDecls.put(SPIN.PREFIX, SPIN.NAMESPACE);
				prefixDecls.put(SPL.PREFIX, SPL.NAMESPACE);
				Service service = new Service(serviceRef, magicPropNode, exprString, prefixDecls, null, false);
				stub.replaceWith(service);
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
			processGraphPattern(collector.getStatementPatterns());
		}

		@Override
		public void meet(StatementPattern node)
		{
			processGraphPattern(Collections.singletonList(node));
		}
	}
}
