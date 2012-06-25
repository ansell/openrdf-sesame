/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
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
 * A parsed update formulated in the OpenRDF query algebra.
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
	 * Creates a new update. To complete this update, one or more update expressions need
	 * to be supplied to it using {@link #addUpdateExpr(UpdateExpr)}.
	 */
	public ParsedUpdate() {
		super();
	}

	/**
	 * Creates a new update. To complete this update, one or update expressions need
	 * to be supplied to it using {@link #addUpdateExpr(UpdateExpr)}.
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
	
	public List<UpdateExpr> getupdateExprs() {
		return updateExprs;
	}
	
	/**
	 * @param updateExpr
	 *        The updateExpr to add.
	 * @param dataset
	 * 		 the dataset that applies to the updateExpr. May be null.
	 */
	public void addDatasetMapping(UpdateExpr updateExpr, Dataset dataset) {
		datasetMapping.put(updateExpr, dataset);
	}

	/**
	 * @return Returns the map of update expressions and associated datasets.
	 */
	public Map<UpdateExpr, Dataset> getDatasetMappings() {
		return datasetMapping;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		// TODO visualize dataset in toString()?
		for (UpdateExpr updateExpr: datasetMapping.keySet()) {
			stringBuilder.append(updateExpr.toString());
			stringBuilder.append("; ");
		}
		return stringBuilder.toString();
	}
}
