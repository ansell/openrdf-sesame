/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;
import info.aduna.xml.SimpleSAXParser;

public class RepositoryManagerConfigReader {

	public static RepositoryManagerConfig read(File configFile)
		throws SAXException, IOException
	{
		InputStream in = new FileInputStream(configFile);
		try {
			return read(in);
		}
		finally {
			in.close();
		}
	}

	public static RepositoryManagerConfig read(InputStream inputStream)
		throws SAXException, IOException
	{
		SimpleSAXParser simpleSAXParser = new SimpleSAXParser();

		ServerConfigParser parser = new ServerConfigParser();
		simpleSAXParser.setListener(parser);
		simpleSAXParser.parse(inputStream);
		return parser.getServerConfig();
	}

	public static RepositoryManagerConfig read(Reader reader)
		throws SAXException, IOException
	{
		SimpleSAXParser simpleSAXParser = new SimpleSAXParser();

		ServerConfigParser parser = new ServerConfigParser();
		simpleSAXParser.setListener(parser);
		simpleSAXParser.parse(reader);
		return parser.getServerConfig();
	}

	/*--------------------------------*
	 * Inner class ServerConfigParser *
	 *--------------------------------*/

	private static class ServerConfigParser extends SimpleSAXAdapter {

		final Logger logger = LoggerFactory.getLogger(this.getClass());

		/*-----------*
		 * Variables *
		 *-----------*/

		private RepositoryManagerConfig _config;

		private UserConfig _currentUser;

		private RepositoryConfig _currentRepository;

		private SailConfig _currentSail;

		private boolean _inAcl;

		/*---------*
		 * Methods *
		 *---------*/

		public RepositoryManagerConfig getServerConfig() {
			return _config;
		}

		public void startDocument() {
			// Initialize variables
			_currentUser = null;
			_currentRepository = null;
			_currentSail = null;
			_inAcl = false;
			_config = new RepositoryManagerConfig();
		}

		public void endDocument()
			throws SAXException
		{
			// Check if admin password was set
			if (_config.getAdminPassword() == null) {
				logger.warn("admin password was not set!!!");
			}
		}

		public void startTag(String tagName, Map<String, String> atts, String text) {
			if (tagName.equals("admin")) {
				String password = atts.get("password");
				if (password == null) {
					logger.error("'password' attribute missing in 'admin' element");
					return;
				}
				_config.setAdminPassword(password);
			}
			else if (tagName.equals("user") && !_inAcl) {
				_currentUser = null;
				_currentRepository = null;

				String login = atts.get("login");
				if (login == null) {
					logger.error("'login' attribute missing in 'user' element");
					return;
				}

				_currentUser = new UserConfig(login.trim());
			}
			else if (tagName.equals("fullname")) {
				_currentUser.setFullName(text.trim());
			}
			else if (tagName.equals("password")) {
				_currentUser.setPassword(text.trim());
			}
			else if (tagName.equals("repository")) {
				String id = atts.get("id");
				if (id == null) {
					logger.error("'id' attribute missing in 'repository' element");
					return;
				}
				_currentRepository = new RepositoryConfig(id);
				_currentUser = null;
			}
			else if (tagName.equals("title")) {
				_currentRepository.setTitle(text.trim());
			}
			else if (tagName.equals("sail")) {
				if (_currentRepository != null) {
					String className = atts.get("class");
					if (className == null) {
						logger.error("'class' attribute missing in 'sail' element");
						return;
					}

					_currentSail = new SailConfig(className);
					_currentRepository.addSailConfig(_currentSail);
				}
			}
			else if (tagName.equals("param")) {
				if (_currentSail != null) {
					String paramName = atts.get("name");
					String paramValue = atts.get("value");
					if (paramName == null) {
						logger.error("'name' attribute missing in 'param' element");
						return;
					}
					if (paramValue == null) {
						logger.error("'value' attribute missing in 'param' element");
						return;
					}

					_currentSail.addParameter(paramName, paramValue);
				}
			}
			else if (tagName.equals("acl")) {
				if (_currentRepository != null) {
					_inAcl = true;

					if (atts.containsKey("worldReadable")) {
						_currentRepository.setWorldReadable("true".equals(atts.get("worldReadable")));
					}

					if (atts.containsKey("worldWritable")) {
						_currentRepository.setWorldWritable("true".equals(atts.get("worldWritable")));
					}
					else if (atts.containsKey("worldWriteable")) {
						// Allow alternative spelling of "writ(e)able" too
						_currentRepository.setWorldWritable("true".equals(atts.get("worldWriteable")));
					}
				}
			}
			else if (tagName.equals("user") && _inAcl) {
				String login = atts.get("login");
				if (login == null) {
					logger.error("'login' attribute missing in 'user' element for acl");
					return;
				}

				if ("true".equals(atts.get("readAccess"))) {
					_currentRepository.grantReadAccess(login);
				}

				if ("true".equals(atts.get("writeAccess"))) {
					_currentRepository.grantWriteAccess(login);
				}
			}
		}

		public void endTag(String tagName) {
			if (tagName.equals("user") && !_inAcl) {
				if (_currentUser != null) {
					_addCurrentUser();
					_currentUser = null;
				}
			}
			else if (tagName.equals("repository")) {
				if (_currentRepository != null) {
					_addCurrentRepository();
					_currentRepository = null;
					_inAcl = false;
				}
			}
			else if (tagName.equals("acl")) {
				_inAcl = false;
			}
		}

		private void _addCurrentUser() {
			boolean correctConfig = true;

			String login = _currentUser.getLogin();

			// Check if all parameters are set.
			if (_currentUser.getFullName() == null) {
				logger.error("fullname missing for user " + login);
				correctConfig = false;
			}
			if (_currentUser.getPassword() == null) {
				logger.error("password missing for user " + login);
				correctConfig = false;
			}

			if (correctConfig) {
				logger.debug("user " + login + " was parsed correctly");
				_config.addUserConfig(_currentUser);
			}
			else {
				logger.warn("ignoring user " + login);
			}
		}

		private void _addCurrentRepository() {
			boolean correctConfig = true;

			String id = _currentRepository.getID();

			// Check if all parameters are set.
			if (_currentRepository.getTitle() == null) {
				logger.error("title missing for repository " + id);
				correctConfig = false;
			}
			if (_currentRepository.getSailConfigStack().isEmpty()) {
				logger.error("no sail defined for repository " + id);
				correctConfig = false;
			}

			if (correctConfig) {
				logger.debug("repository " + id + " was parsed correctly");
				_config.addRepositoryConfig(_currentRepository);
			}
			else {
				logger.warn("ignoring repository " + id);
			}
		}
	}
}
