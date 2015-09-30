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
package org.openrdf.rio.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.Namespaces;
import org.openrdf.rio.RDFHandlerException;

/**
 * A RDFHandler that can be used to collect reported statements in collections.
 * 
 * @author Arjohn Kampman
 */
public class ContextStatementCollector extends AbstractRDFHandler {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Collection<Statement> statements;

	private Map<String, String> namespaces;

	private Resource[] contexts;

	private ValueFactory vf;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new StatementCollector that uses a new ArrayList to store the
	 * reported statements and a new LinkedHashMap to store the reported
	 * namespaces.
	 */
	public ContextStatementCollector(ValueFactory vf, Resource... contexts) {
		this(new ArrayList<Statement>(), vf, contexts);
	}

	/**
	 * Creates a new StatementCollector that stores reported statements in the
	 * supplied collection and that uses a new LinkedHashMap to store the
	 * reported namespaces.
	 */
	public ContextStatementCollector(Collection<Statement> statements, ValueFactory vf, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);
		if (statements instanceof Model) {
			this.namespaces = Namespaces.wrap(((Model)statements).getNamespaces());
		}
		else {
			this.namespaces = new LinkedHashMap<String, String>();
		}
		this.statements = statements;
		this.vf = vf;
		this.contexts = contexts;
	}

	/**
	 * Creates a new StatementCollector that stores reported statements and
	 * namespaces in the supplied containers.
	 */
	public ContextStatementCollector(Collection<Statement> statements, Map<String, String> namespaces,
			ValueFactory vf, Resource... contexts)
	{
		OpenRDFUtil.verifyContextNotNull(contexts);
		this.statements = statements;
		this.namespaces = namespaces;
		this.vf = vf;
		this.contexts = contexts;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Clear the set of collected statements.
	 */
	public void clear() {
		statements.clear();
	}

	/**
	 * Gets the collection that contains the collected statements.
	 */
	public Collection<Statement> getStatements() {
		return statements;
	}

	/**
	 * Gets the map that contains the collected namespaces.
	 */
	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		if (!namespaces.containsKey(prefix)) {
			namespaces.put(prefix, uri);
		}
	}

	@Override
	public void handleStatement(Statement st) {
		if (contexts.length == 0) {
			statements.add(st);
		}
		else {
			for (Resource nextContext : contexts) {
				statements.add(vf.createStatement(st.getSubject(), st.getPredicate(), st.getObject(), nextContext));
			}
		}
	}
}
