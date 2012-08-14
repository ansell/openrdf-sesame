/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset.config;

import org.openrdf.repository.config.DelegatingRepositoryImplConfigBase;

/**
 * @author Arjohn Kampman
 */
public class DatasetRepositoryConfig extends DelegatingRepositoryImplConfigBase {

	public DatasetRepositoryConfig() {
		super(DatasetRepositoryFactory.REPOSITORY_TYPE);
	}
}
