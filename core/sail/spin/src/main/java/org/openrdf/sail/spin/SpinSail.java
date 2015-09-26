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

import info.aduna.iteration.Iterations;

import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.algebra.evaluation.function.TupleFunctionRegistry;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencer;
import org.openrdf.spin.SpinParser;

public class SpinSail extends AbstractForwardChainingInferencer {

	public enum EvaluationMode {
		/**
		 * Uses the base SAIL along with an embedded SERVICE
		 * to perform query evaluation.
		 * The SERVICE is used to evaluate extended query algebra nodes
		 * such as {@link TupleFunction}s.
		 * (Default).
		 */
		SERVICE,
		/**
		 * Assumes the base SAIL supports an extended query algebra
		 * (e.g. {@link TupleFunction}s) and use it to perform
		 * all query evaluation.
		 */
		NATIVE,
		/**
		 * Treats the base SAIL as a simple triple source
		 * and all the query evaluation is performed by this SAIL.
		 */
		TRIPLE_SOURCE
	}

	private FunctionRegistry functionRegistry = FunctionRegistry.getInstance();
	private TupleFunctionRegistry tupleFunctionRegistry = TupleFunctionRegistry.getInstance();
	private FederatedServiceResolver serviceResolver = new FederatedServiceResolverImpl();
	private SpinParser parser = new SpinParser();
	private EvaluationMode evaluationMode = EvaluationMode.SERVICE;

	public SpinSail() {
		super.setFederatedServiceResolver(serviceResolver);
	}

	public SpinSail(NotifyingSail baseSail) {
		super(baseSail);
		super.setFederatedServiceResolver(serviceResolver);
	}

	public FunctionRegistry getFunctionRegistry() {
		return functionRegistry;
	}

	public void setFunctionRegistry(FunctionRegistry registry) {
		this.functionRegistry = registry;
	}

	public TupleFunctionRegistry getTupleFunctionRegistry() {
		return tupleFunctionRegistry;
	}

	public void setTupleFunctionRegistry(TupleFunctionRegistry registry) {
		this.tupleFunctionRegistry = registry;
	}

	public FederatedServiceResolver getFederatedServiceResolver() {
		return serviceResolver;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		serviceResolver = resolver;
		super.setFederatedServiceResolver(resolver);
	}

	public void setEvaluationMode(EvaluationMode mode) {
		this.evaluationMode = mode;
	}

	public EvaluationMode getEvaluationMode() {
		return evaluationMode;
	}

	public SpinParser getSpinParser() {
		return parser;
	}

	public void setSpinParser(SpinParser parser) {
		this.parser = parser;
	}

	@Override
	public SpinSailConnection getConnection()
		throws SailException
	{
		InferencerConnection con = (InferencerConnection)super.getConnection();
		return new SpinSailConnection(this, con);
	}

	@Override
	public void initialize()
		throws SailException
	{
		super.initialize();

		SpinSailConnection con = getConnection();
		try {
			con.begin();
			Set<Statement> stmts = Iterations.asSet(con.getStatements(getValueFactory().createURI(SP.NAMESPACE), RDF.TYPE, OWL.ONTOLOGY, true));
			if(stmts.isEmpty()) {
				con.addAxiomStatements();
			}
			con.commit();
		}
		finally {
			con.close();
		}
	}
}
