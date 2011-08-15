/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

	/**
	 * @param updateExpr
	 *        The updateExpr to add.
	 */
	public void addUpdateExpr(UpdateExpr updateExpr) {
		updateExprs.add(updateExpr);
	}

	/**
	 * @return Returns the list of update expressions.
	 */
	public List<UpdateExpr> getUpdateExprs() {
		return updateExprs;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (UpdateExpr updateExpr: updateExprs) {
			stringBuilder.append(updateExpr.toString());
			stringBuilder.append("; ");
		}
		return stringBuilder.toString();
	}
}
