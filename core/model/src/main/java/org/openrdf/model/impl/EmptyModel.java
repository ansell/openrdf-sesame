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
package org.openrdf.model.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Blocks access to the statements of the model, allowing only changes to the
 * model's namespaces.
 * 
 * @author James Leigh
 * @since 2.7.0
 */
public class EmptyModel extends AbstractModel {

	private final Model model;

	public EmptyModel(Model model) {
		this.model = model;
	}

	private static final long serialVersionUID = 3123007631452759092L;

	private Set<Statement> emptySet = Collections.emptySet();

	@Override
	public Namespace getNamespace(String prefix) {
		return this.model.getNamespace(prefix);
	}

	@Override
	public Set<Namespace> getNamespaces() {
		return this.model.getNamespaces();
	}

	@Override
	public Namespace setNamespace(String prefix, String name) {
		return this.model.setNamespace(prefix, name);
	}

	@Override
	public void setNamespace(Namespace namespace) {
		this.model.setNamespace(namespace);
	}

	@Override
	public Namespace removeNamespace(String prefix) {
		return this.model.removeNamespace(prefix);
	}

	@Override
	public Iterator<Statement> iterator() {
		return emptySet.iterator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		throw new UnsupportedOperationException("All statements are filtered out of view");
	}

	@Override
	public boolean contains(Resource subj, URI pred, Value obj, Resource... contexts) {
		return false;
	}

	@Override
	public Model filter(Resource subj, URI pred, Value obj, Resource... contexts) {
		return this;
	}

	@Override
	public boolean remove(Resource subj, URI pred, Value obj, Resource... contexts) {
		return false;
	}

	@Override
	public void removeTermIteration(Iterator<Statement> iter, Resource subj, URI pred, Value obj,
			Resource... contexts)
	{
		// remove nothing
	}

}