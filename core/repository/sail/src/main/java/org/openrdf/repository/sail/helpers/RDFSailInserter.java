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
package org.openrdf.repository.sail.helpers;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.util.AbstractRDFInserter;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UpdateContext;

/**
 * An RDFHandler that adds RDF data to a sail.
 * 
 * @author jeen
 */
public class RDFSailInserter extends AbstractRDFInserter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The connection to use for the add operations.
	 */
	private final SailConnection con;

	private final UpdateContext uc;

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
	public RDFSailInserter(SailConnection con, ValueFactory vf, UpdateContext uc) {
		super(vf);
		this.con = con;
		this.uc = uc;
	}

	public RDFSailInserter(SailConnection con, ValueFactory vf) {
		this(con, vf, null);
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
	protected void addStatement(Resource subj, URI pred, Value obj, Resource ctxt) throws OpenRDFException
	{
		if (enforcesContext()) {
			addStatement(uc, subj, pred, obj, contexts);
		}
		else {
			if (uc != null && ctxt == null) {
				final URI insertGraph = uc.getDataset().getDefaultInsertGraph();
				if (insertGraph != null) {
					addStatement(uc, subj, pred, obj, insertGraph);
				}
				else {
					addStatement(uc, subj, pred, obj);
				}
			}
			else {
				addStatement(uc, subj, pred, obj, ctxt);
			}
		}
	}

	private void addStatement(UpdateContext uc, Resource subj, URI pred, Value obj, Resource... ctxts) throws SailException
	{
		if(uc != null) {
			con.addStatement(uc, subj, pred, obj, ctxts);
		}
		else {
			con.addStatement(subj, pred, obj, ctxts);
		}
	}
}
