/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import org.openrdf.repository.config.RepositoryImplConfigBase;

/**
 * @author Arjohn Kampman
 */
public class SystemRepositoryConfig extends RepositoryImplConfigBase {

	public SystemRepositoryConfig() {
		super(SystemRepository.REPOSITORY_TYPE);
	}
}
