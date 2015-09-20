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
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverBase;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencer;
import org.openrdf.spin.SpinParser;

public class SpinSail extends AbstractForwardChainingInferencer {

	private FunctionRegistry functionRegistry = FunctionRegistry.getInstance();
	private FederatedServiceResolverBase serviceRegistry = new FederatedServiceResolverImpl();
	private SpinParser parser = new SpinParser();

	public SpinSail() {
		super.setFederatedServiceResolver(serviceRegistry);
	}

	public SpinSail(NotifyingSail baseSail) {
		super(baseSail);
		super.setFederatedServiceResolver(serviceRegistry);
	}

	public FunctionRegistry getFunctionRegistry() {
		return functionRegistry;
	}

	public void setFunctionRegistry(FunctionRegistry registry) {
		this.functionRegistry = registry;
	}

	public FederatedServiceResolver getFederatedServiceResolver() {
		return serviceRegistry;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		if(!(resolver instanceof FederatedServiceResolverBase)) {
			throw new IllegalArgumentException("Required to be an instance of "+FederatedServiceResolverBase.class.getName());
		}
		serviceRegistry = (FederatedServiceResolverBase) resolver;
		super.setFederatedServiceResolver(resolver);
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
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new SpinSailConnection(this, con);
		}
		catch (ClassCastException e) {
			throw new SailException(e.getMessage(), e);
		}
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
