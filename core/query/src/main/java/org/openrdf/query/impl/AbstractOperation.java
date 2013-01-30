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
package org.openrdf.query.impl;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Operation;

/**
 * Abstract super class of all operation types.
 * 
 * @author Jeen Broekstra
 */
public abstract class AbstractOperation implements Operation {

	/*------------*
	 * Attributes *
	 *------------*/

	protected final MapBindingSet bindings = new MapBindingSet();

	protected Dataset dataset = null;

	protected boolean includeInferred = true;


	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new operation object.
	 */
	protected AbstractOperation() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setBinding(String name, Value value) {
		bindings.addBinding(name, value);
	}

	public void removeBinding(String name) {
		bindings.removeBinding(name);
	}

	public void clearBindings() {
		bindings.clear();
	}

	public BindingSet getBindings() {
		return bindings;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	public boolean getIncludeInferred() {
		return includeInferred;
	}

}
