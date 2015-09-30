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
package org.eclipse.rdf4j.query.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;

/**
 * A parsed update sequence formulated in the OpenRDF query algebra.
 * 
 * @author Jeen Broekstra
 */
public class ParsedUpdate extends ParsedOperation {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> namespaces;

	private List<UpdateExpr> updateExprs = new ArrayList<UpdateExpr>();

	private Map<UpdateExpr, Dataset> datasetMapping = new HashMap<UpdateExpr, Dataset>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new update sequence. To complete this update sequence, one or
	 * more update expressions need to be supplied to it using
	 * {@link #addUpdateExpr(UpdateExpr)}.
	 */
	public ParsedUpdate() {
		super();
	}

	/**
	 * Creates a new update sequence. To complete this update sequence, one or
	 * update expressions need to be supplied to it using
	 * {@link #addUpdateExpr(UpdateExpr)}.
	 * 
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the update.
	 */
	public ParsedUpdate(Map<String, String> namespaces) {
		super();
		this.namespaces = namespaces;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getNamespaces() {
		if (namespaces != null) {
			return namespaces;
		}
		else {
			return Collections.emptyMap();
		}
	}

	public void addUpdateExpr(UpdateExpr updateExpr) {
		updateExprs.add(updateExpr);
	}

	public List<UpdateExpr> getUpdateExprs() {
		return updateExprs;
	}

	/**
	 * @param updateExpr
	 *        The updateExpr to map to a dataset.
	 * @param dataset
	 *        the dataset that applies to the updateExpr. May be null.
	 */
	public void map(UpdateExpr updateExpr, Dataset dataset) {
		datasetMapping.put(updateExpr, dataset);
	}

	/**
	 * @return Returns the map of update expressions and associated datasets.
	 */
	public Map<UpdateExpr, Dataset> getDatasetMapping() {
		return datasetMapping;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		// TODO visualize dataset in toString()?
		for (UpdateExpr updateExpr : updateExprs) {
			stringBuilder.append(updateExpr.toString());
			stringBuilder.append("; ");
		}
		return stringBuilder.toString();
	}
}
