/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import java.util.Collections;
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

	private UpdateExpr updateExpr;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new update. To complete this update, an update expression needs
	 * to be supplied to it using {@link #setUpdateExpr(UpdateExpr)}.
	 */
	public ParsedUpdate() {
		super();
	}

	/**
	 * Creates a new update. To complete this update, an update expression needs
	 * to be supplied to it using {@link #setUpdateExpr(UpdateExpr)}.
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
	 *        The updateExpr to set.
	 */
	public void setUpdateExpr(UpdateExpr updateExpr) {
		this.updateExpr = updateExpr;
	}

	/**
	 * @return Returns the updateExpr.
	 */
	public UpdateExpr getUpdateExpr() {
		return updateExpr;
	}
	
	@Override
	public String toString() {
		return updateExpr.toString();
	}
}
