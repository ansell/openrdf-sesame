/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

@Deprecated
public class UserConfig {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _login;

	private String _fullName;

	private String _password;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public UserConfig() {
	}

	public UserConfig(String login) {
		setLogin(login);
	}

	/**
	 * Creates a new UserConfig with the supplied login, full name and password.
	 * 
	 * @param login
	 *        User's login name.
	 * @param fullName
	 *        User's full name.
	 * @param password
	 *        User's password.
	 */
	public UserConfig(String login, String fullName, String password) {
		setLogin(login);
		setFullName(fullName);
		setPassword(password);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getLogin() {
		return _login;
	}

	public void setLogin(String login) {
		_login = login;
	}

	public String getFullName() {
		return _fullName;
	}

	public void setFullName(String fullName) {
		_fullName = fullName;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}

	/**
	 * Two UserConfig objects are equal when their login names are equal.
	 */
	public boolean equals(Object other) {
		if (other instanceof UserConfig) {
			UserConfig o = (UserConfig)other;

			if (_login == null) {
				return o.getLogin() == null;
			}
			else {
				return _login.equals(o.getLogin());
			}
		}

		return false;
	}

	public int hashCode() {
		return _login == null ? 0 : _login.hashCode();
	}
}
