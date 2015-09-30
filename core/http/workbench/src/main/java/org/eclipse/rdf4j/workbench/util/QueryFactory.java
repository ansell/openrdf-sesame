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
package org.eclipse.rdf4j.workbench.util;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Utility class for generating query objects.
 */
public class QueryFactory {

	public static Query prepareQuery(final RepositoryConnection con, final QueryLanguage queryLn, final String query)
		throws OpenRDFException
	{
		Query rval = null;
		try {
			rval = con.prepareQuery(queryLn, query);
		}
		catch (UnsupportedOperationException exc) {
			// TODO must be an HTTP repository
			try {
				con.prepareTupleQuery(queryLn, query).evaluate().close();
				rval = con.prepareTupleQuery(queryLn, query);
			}
			catch (Exception e1) {
				// guess its not a tuple query
				try {
					con.prepareGraphQuery(queryLn, query).evaluate().close();
					rval = con.prepareGraphQuery(queryLn, query);
				}
				catch (Exception e2) {
					// guess its not a graph query
					try {
						con.prepareBooleanQuery(queryLn, query).evaluate();
						rval = con.prepareBooleanQuery(queryLn, query);
					}
					catch (Exception e3) {
						// guess its not a boolean query
						// let's assume it is an malformed tuple query
						rval = con.prepareTupleQuery(queryLn, query);
					}
				}
			}
		}
		return rval;
	}
}
