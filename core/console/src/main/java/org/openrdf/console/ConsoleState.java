/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * @author Dale Visser
 */
public interface ConsoleState {

	String getApplicationName();

	File getDataDirectory();

	String getManagerID();

	String getRepositoryID();

	RepositoryManager getManager();

	void setManager(RepositoryManager manager);

	void setManagerID(String managerID);

	Repository getRepository();

	void setRepositoryID(String repositoryID);

	void setRepository(Repository repository);
}
