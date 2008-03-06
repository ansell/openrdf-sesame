/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RepositoryManagerConfig {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _adminPassword = "";

	private HashMap<String, UserConfig> _userMap = new LinkedHashMap<String, UserConfig>();

	private HashMap<String, RepositoryConfig> _repositoryMap = new LinkedHashMap<String, RepositoryConfig>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RepositoryManagerConfig() {
	}

	/*------------------------*
	 * Server-related methods *
	 *------------------------*/

	public void setAdminPassword(String password) {
		_adminPassword = password;
	}

	public String getAdminPassword() {
		return _adminPassword;
	}

	/*----------------------*
	 * User-related methods *
	 *----------------------*/

	public void addUserConfig(UserConfig ui) {
		_userMap.put(ui.getLogin(), ui);
	}

	/**
	 * Removes the user with the supplied login
	 * 
	 * @param login
	 *        User login
	 */
	public void removeUserConfig(String login) {
		_userMap.remove(login);
	}

	public UserConfig getUserConfig(String login) {
		return _userMap.get(login);
	}

	/**
	 * Gets all user configs.
	 */
	public Collection<UserConfig> getUserConfigs() {
		return Collections.unmodifiableCollection(_userMap.values());
	}

	/*----------------------------*
	 * Repository-related methods *
	 *----------------------------*/

	public void addRepositoryConfig(RepositoryConfig rc) {
		_repositoryMap.put(rc.getID(), rc);
	}

	/**
	 * Removes the repository with the supplied id
	 * 
	 * @param id
	 *        Repository id
	 */
	public void removeRepositoryConfig(String id) {
		_repositoryMap.remove(id);
	}

	public RepositoryConfig getRepositoryConfig(String id) {
		return _repositoryMap.get(id);
	}

	public Collection<RepositoryConfig> getRepositoryConfigs() {
		return Collections.unmodifiableCollection(_repositoryMap.values());
	}
}
