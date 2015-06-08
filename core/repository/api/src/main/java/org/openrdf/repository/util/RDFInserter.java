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
package org.openrdf.repository.util;

import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

/**
 * An RDFHandler that adds RDF data to a repository.
 * 
 * @author jeen
 */
public class RDFInserter extends AbstractRDFInserter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The connection to use for the add operations.
	 */
	protected final RepositoryConnection con;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFInserter object that preserves bnode IDs and that does
	 * not enforce any context upon statements that are reported to it.
	 * 
	 * @param con
	 *        The connection to use for the add operations.
	 */
	public RDFInserter(RepositoryConnection con) {
		super(con.getValueFactory());
		this.con = con;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void addNamespace(String prefix, String name) throws OpenRDFException
	{
		if (con.getNamespace(prefix) == null) {
			con.setNamespace(prefix, name);
		}
	}

	@Override
	protected void addStatement(Resource subj, IRI pred, Value obj, Resource ctxt) throws OpenRDFException
	{
		if (enforcesContext()) {
			con.add(subj, pred, obj, contexts);
		}
		else {
			con.add(subj, pred, obj, ctxt);
		}
	}
}
