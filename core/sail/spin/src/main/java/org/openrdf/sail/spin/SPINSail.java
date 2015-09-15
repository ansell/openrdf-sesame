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
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencer;
import org.openrdf.spin.AskFunction;
import org.openrdf.spin.SPINParser;

public class SPINSail extends AbstractForwardChainingInferencer {

	static {
		FunctionRegistry.getInstance().add(new org.openrdf.sail.spin.function.Concat());
		SPINParser parser = new SPINParser();
		FunctionRegistry.getInstance().add(new AskFunction(parser));
	}

	private SPINParser parser = new SPINParser();

	public SPINSail() {
	}

	public SPINSail(NotifyingSail baseSail) {
		super(baseSail);
	}

	public SPINParser getSPINParser() {
		return parser;
	}

	public void setSPINParser(SPINParser parser) {
		this.parser = parser;
	}

	@Override
	public SPINSailConnection getConnection()
		throws SailException
	{
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new SPINSailConnection(this, con);
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

		SPINSailConnection con = getConnection();
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
