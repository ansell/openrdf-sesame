/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.lubm;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

/**
 * 
 */
public class NativeRepositoryFactory extends RepositoryFactory {

	// implements RepositoryFactory.create()
	@Override
	public Repository create() {
		return new NativeRepository();
	}

}
