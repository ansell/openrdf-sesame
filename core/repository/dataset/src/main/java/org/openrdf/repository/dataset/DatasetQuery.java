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
