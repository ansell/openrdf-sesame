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
package org.openrdf.sail;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Provided with add and remove operation to give them context within a
 * {@link UpdateExpr} operation.
 * 
 * @author James Leigh
 * @since 2.7.0
 */
public class UpdateContext {

	private final UpdateExpr updateExpr;

	private final Dataset dataset;

	private final BindingSet bindings;

	private final boolean includeInferred;

	public UpdateContext(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) {
		assert updateExpr != null;
		this.updateExpr = updateExpr;
		if (dataset == null) {
			this.dataset = new SimpleDataset();
		} else {
			this.dataset = dataset;
		}
		if (bindings == null) {
			this.bindings = EmptyBindingSet.getInstance();
		} else {
			this.bindings = bindings;
		}
		this.includeInferred = includeInferred;
	}

	@Override
	public String toString() {
		return updateExpr.toString();
	}

	/**
	 * @return Returns the updateExpr.
	 */
	public UpdateExpr getUpdateExpr() {
		return updateExpr;
	}

	/**
	 * @return Returns the dataset.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * @return Returns the bindings.
	 */
	public BindingSet getBindingSet() {
		return bindings;
	}

	/**
	 * @return Returns the includeInferred.
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}
}
