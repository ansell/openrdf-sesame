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
