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
package org.openrdf.rio.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 * A RDFHandler that can be used to collect reported statements in collections.
 * 
 * @author Arjohn Kampman
 */
public class ContextStatementCollector extends RDFHandlerBase {

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
		this(statements, new LinkedHashMap<String, String>(), vf, contexts);
	}

	/**
	 * Creates a new StatementCollector that stores reported statements and
	 * namespaces in the supplied containers.
	 */
	public ContextStatementCollector(Collection<Statement> statements, Map<String, String> namespaces, ValueFactory vf,
			Resource... contexts)
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
				statements.add(vf.createStatement(st.getSubject(), st.getPredicate(),
						st.getObject(), nextContext));
			}
		}
	}
}
