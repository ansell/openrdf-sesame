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
package org.openrdf.repository.dataset;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;
import org.openrdf.repository.sail.SailQuery;

/**
 * @author Arjohn Kampman
 */
abstract class DatasetQuery implements Query {

	protected final DatasetRepositoryConnection con;

	protected final SailQuery sailQuery;

	protected DatasetQuery(DatasetRepositoryConnection con, SailQuery sailQuery) {
		this.con = con;
		this.sailQuery = sailQuery;
	}

	public final void setBinding(String name, Value value) {
		sailQuery.setBinding(name, value);
	}

	public final void removeBinding(String name) {
		sailQuery.removeBinding(name);
	}

	public final void clearBindings() {
		sailQuery.clearBindings();
	}

	public final BindingSet getBindings() {
		return sailQuery.getBindings();
	}

	public final void setDataset(Dataset dataset) {
		sailQuery.setDataset(dataset);
	}

	public final Dataset getDataset() {
		return sailQuery.getDataset();
	}

	public final void setIncludeInferred(boolean includeInferred) {
		sailQuery.setIncludeInferred(includeInferred);
	}

	public final boolean getIncludeInferred() {
		return sailQuery.getIncludeInferred();
	}

	@Override
	public void setMaxExecutionTime(int maxExecTime) {
		sailQuery.setMaxExecutionTime(maxExecTime);
	}
	
	@Override
	public int getMaxExecutionTime() {
		return sailQuery.getMaxExecutionTime();
	}
	
	@Deprecated
	public void setMaxQueryTime(int maxQueryTime) {
		setMaxExecutionTime(maxQueryTime);
	}

	@Deprecated
	public int getMaxQueryTime() {
		return getMaxExecutionTime();
	}

	@Override
	public String toString() {
		return sailQuery.toString();
	}
}
