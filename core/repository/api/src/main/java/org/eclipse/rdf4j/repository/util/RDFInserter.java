/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.util;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

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
