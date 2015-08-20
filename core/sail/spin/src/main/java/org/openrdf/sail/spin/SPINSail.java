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

import org.openrdf.model.Resource;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencer;

public class SPINSail extends AbstractForwardChainingInferencer {

	private Resource axiomContext;

	public SPINSail() {
	}

	public SPINSail(NotifyingSail baseSail) {
		super(baseSail);
	}

	public void setAxiomContext(Resource context) {
		this.axiomContext = context;
	}

	public Resource getAxionContext() {
		return this.axiomContext;
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
			con.addAxiomStatements();
			con.commit();
		}
		finally {
			con.close();
		}
	}
}
