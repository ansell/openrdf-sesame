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
package org.openrdf.query.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.UpdateExpr;

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

	public ParsedUpdate(String sourceString) {
		super(sourceString);
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
