/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf;

import org.openrdf.model.Resource;

/**
 * General utility methods for OpenRDF/Sesame modules.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class OpenRDFUtil {

	private static final Resource[] DEFAULT_CONTEXTS = new Resource[]{null};

	/**
	 * Verifies that the supplied contexts parameter is not <tt>null</tt>,
	 * returning the default context if it is.
	 * 
	 * @param contexts
	 *        The parameter to check.
	 * @returns a none-null array
	 */
	public static Resource[] notNull(Resource... contexts) {
		if (contexts == null)
			return DEFAULT_CONTEXTS;
		return contexts;
	}
}
