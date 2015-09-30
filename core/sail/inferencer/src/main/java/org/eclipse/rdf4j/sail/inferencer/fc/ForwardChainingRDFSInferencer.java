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
package org.eclipse.rdf4j.sail.inferencer.fc;

import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.inferencer.InferencerConnection;

/**
 * Forward-chaining RDF Schema inferencer, using the rules from the <a
 * href="http://www.w3.org/TR/2004/REC-rdf-mt-20040210/">RDF Semantics
 * Recommendation (10 February 2004)</a>. This inferencer can be used to add
 * RDF Schema semantics to any Sail that returns {@link InferencerConnection}s
 * from their {@link Sail#getConnection()} method.
 */
public class ForwardChainingRDFSInferencer extends AbstractForwardChainingInferencer {
	/*--------------*
	 * Constructors *
	 *--------------*/

	public ForwardChainingRDFSInferencer() {
		super();
	}

	public ForwardChainingRDFSInferencer(NotifyingSail baseSail) {
		super(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public ForwardChainingRDFSInferencerConnection getConnection()
		throws SailException
	{
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new ForwardChainingRDFSInferencerConnection(this, con);
		}
		catch (ClassCastException e) {
			throw new SailException(e.getMessage(), e);
		}
	}

	/**
	 * Adds axiom statements to the underlying Sail.
	 */
	@Override
	public void initialize()
		throws SailException
	{
		super.initialize();

		ForwardChainingRDFSInferencerConnection con = getConnection();
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
