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
package org.eclipse.rdf4j.query.dawg;

import static org.eclipse.rdf4j.query.dawg.DAWGTestResultSetSchema.BOOLEAN;
import static org.eclipse.rdf4j.query.dawg.DAWGTestResultSetSchema.RESULTSET;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.GraphImpl;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestBooleanParser extends AbstractRDFHandler {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Graph graph = new GraphImpl();

	private boolean value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DAWGTestBooleanParser() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean getValue() {
		return value;
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		graph.clear();
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		graph.add(st);
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		try {
			Resource resultSetNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RESULTSET);
			Literal booleanLit = GraphUtil.getUniqueObjectLiteral(graph, resultSetNode, BOOLEAN);

			if (booleanLit.equals(DAWGTestResultSetSchema.TRUE)) {
				value = true;
			}
			else if (booleanLit.equals(DAWGTestResultSetSchema.FALSE)) {
				value = false;
			}
			else {
				throw new RDFHandlerException("Invalid boolean value: " + booleanLit);
			}
		}
		catch (GraphUtilException e) {
			throw new RDFHandlerException(e.getMessage(), e);
		}
	}
}