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
package org.openrdf.query.dawg;

import static org.openrdf.query.dawg.DAWGTestResultSetSchema.BOOLEAN;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTSET;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.Models;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestBooleanParser extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Model graph = new LinkedHashModel();

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
			Resource resultSetNode = Models.getUniqueSubject(graph, RDF.TYPE, RESULTSET);
			Literal booleanLit = Models.getUniqueObjectLiteral(graph, resultSetNode, BOOLEAN);

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
