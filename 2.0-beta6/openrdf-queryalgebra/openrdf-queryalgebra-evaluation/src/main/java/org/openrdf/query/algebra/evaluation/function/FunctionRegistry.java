/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function;

import info.aduna.lang.service.ServiceRegistry;

/**
 * @author Arjohn Kampman
 */
public class FunctionRegistry extends ServiceRegistry<String, Function> {

	private static FunctionRegistry defaultRegistry;

	/**
	 * Gets the default QueryParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static FunctionRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new FunctionRegistry();
		}

		return defaultRegistry;
	}

	public FunctionRegistry() {
		super(Function.class);
	}

	@Override
	protected String getKey(Function function)
	{
		return function.getURI();
	}
}
