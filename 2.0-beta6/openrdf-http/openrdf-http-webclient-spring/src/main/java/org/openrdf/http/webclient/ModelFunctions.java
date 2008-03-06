/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 * @author Herko ter Horst
 */
public class ModelFunctions {

	public static final boolean isResource(Value value) {
		return value instanceof Resource;
	}

}
