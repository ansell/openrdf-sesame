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

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UpdateContext;

/**
 * An RDFHandler that removes RDF data from a repository.
 * 
 * @author jeen
 */
public class RDFSailRemover extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The connection to use for the remove operations.
	 */
	private final SailConnection con;

	private final ValueFactory vf;

	private final UpdateContext uc;

	/**
	 * The contexts to remove the statements from. If this variable is a
	 * non-empty array, statements will be removed from the corresponding
	 * contexts.
	 */
	private Resource[] contexts = new Resource[0];

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFSailRemover object.
	 * 
	 * @param con
	 *        The connection to use for the remove operations.
	 */
	public RDFSailRemover(SailConnection con, ValueFactory vf, UpdateContext uc) {
		this.con = con;
		this.vf = vf;
		this.uc = uc;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Enforces the supplied contexts upon all statements that are reported to
	 * this RDFSailRemover.
	 * 
	 * @param contexts
	 *        the contexts to use. Use an empty array (not null!) to indicate no
	 *        context(s) should be enforced.
	 */
	public void enforceContext(Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);
		this.contexts = contexts;
	}

	/**
	 * Checks whether this RDFRemover enforces its contexts upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its contexts, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return contexts.length != 0;
	}

	/**
	 * Gets the contexts that this RDFRemover enforces upon all statements that
	 * are reported to it (in case <tt>enforcesContext()</tt> returns
	 * <tt>true</tt>).
	 * 
	 * @return A Resource[] identifying the contexts, or <tt>null</tt> if no
	 *         contexts is enforced.
	 */
	public Resource[] getContexts() {
		return contexts;
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource ctxt = st.getContext();

		try {
			if (enforcesContext()) {
				con.removeStatement(uc, subj, pred, obj, contexts);
			}
			else {
				if (ctxt != null) {
					con.removeStatement(uc, subj, pred, obj, ctxt);
				}
				else {
					con.removeStatement(uc, subj, pred, obj);
				}
			}
		}
		catch (SailException e) {
			throw new RDFHandlerException(e);
		}
	}
}
