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
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A Model wrapper that prevents modification to the underlying model.
 */
class UnmodifiableModel extends AbstractModel {

	private static final long serialVersionUID = 6335569454318096059L;

	private final Model model;

	public UnmodifiableModel(Model delegate) {
		this.model = delegate;
	}

	@Override
	public Map<String, String> getNamespaces() {
		return Collections.unmodifiableMap(model.getNamespaces());
	}

	@Override
	public String getNamespace(String prefix) {
		return model.getNamespace(prefix);
	}

	@Override
	public String setNamespace(String prefix, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String removeNamespace(String prefix) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Resource subj, URI pred, Value obj, Resource... contexts) {
		return model.contains(subj, pred, obj, contexts);
	}

	@Override
	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Resource subj, URI pred, Value obj, Resource... contexts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Model filter(Resource subj, URI pred, Value obj, Resource... contexts) {
		return model.filter(subj, pred, obj, contexts).unmodifiable();
	}

	@Override
	public Iterator<Statement> iterator() {
		return Collections.unmodifiableSet(model).iterator();
	}

	@Override
	public int size() {
		return model.size();
	}

	@Override
	public void removeTermIteration(Iterator<Statement> iter, Resource subj, URI pred, Value obj,
			Resource... contexts)
	{
		throw new UnsupportedOperationException();
	}

}
