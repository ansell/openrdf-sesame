/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
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

import org.xml.sax.SAXException;

import org.openrdf.util.log.ThreadLog;
import org.openrdf.util.xml.SimpleSAXAdapter;
import org.openrdf.util.xml.SimpleSAXParser;

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
				ThreadLog.warning("admin password was not set!!!");
			}
		}
	
		public void startTag(String tagName, Map<String, String> atts, String text) {
			if (tagName.equals("admin")) {
				String password = atts.get("password");
				if (password == null) {
					ThreadLog.error("'password' attribute missing in 'admin' element");
					return;
				}
				_config.setAdminPassword(password);
			}
			else if (tagName.equals("log")) {
				String dir = atts.get("dir");
				if (dir == null) {
					ThreadLog.error("'dir' attribute missing in 'log' element");
					return;
				}
				_config.setLogDir(dir);
	
				int logLevel = 2;
				String logLevelStr = atts.get("level");
				if (logLevelStr != null) {
					try {
						logLevel = Integer.parseInt(logLevelStr);
						if (logLevel >= 0 && logLevel  <= 5) {
							_config.setLogLevel(logLevel);
						}
						else {
							ThreadLog.error("value of 'level' attribute in 'log' element should be a number between 0 and 5");
						}
					}
					catch (NumberFormatException e) {
						ThreadLog.error("value of 'level' attribute in 'log' element should be a number between 0 and 5");
					}
				}
			}
			else if (tagName.equals("tmp")) {
				String dir = atts.get("dir");
				if (dir == null) {
					ThreadLog.error("'dir' attribute missing in 'tmp' element");
					return;
				}
				_config.setTmpDir(dir);
			}
			else if (tagName.equals("user") && !_inAcl) {
				_currentUser = null;
				_currentRepository = null;
	
				String login = atts.get("login");
				if (login == null) {
					ThreadLog.error("'login' attribute missing in 'user' element");
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
					ThreadLog.error("'id' attribute missing in 'repository' element");
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
						ThreadLog.error("'class' attribute missing in 'sail' element");
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
						ThreadLog.error("'name' attribute missing in 'param' element");
						return;
					}
					if (paramValue == null) {
						ThreadLog.error("'value' attribute missing in 'param' element");
						return;
					}
	
					_currentSail.addParameter(paramName, paramValue);
				}
			}
			else if (tagName.equals("acl")) {
				if (_currentRepository != null) {
					_inAcl = true;
	
					_currentRepository.setWorldReadable(
							"true".equals( atts.get("worldReadable") ));
	
					_currentRepository.setWorldWriteable(
							"true".equals( atts.get("worldWriteable") ));
	
				}
			}
			else if (tagName.equals("user") && _inAcl) {
				String login = atts.get("login");
				if (login == null) {
					ThreadLog.error("'login' attribute missing in 'user' element for acl");
					return;
				}
	
				if ("true".equals( atts.get("readAccess") )) {
					_currentRepository.grantReadAccess(login);
				}
	
				if ("true".equals( atts.get("writeAccess") )) {
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
				ThreadLog.error("fullname missing for user " + login);
				correctConfig = false;
			}
			if (_currentUser.getPassword() == null) {
				ThreadLog.error("password missing for user " + login);
				correctConfig = false;
			}
	
			if (correctConfig) {
				ThreadLog.trace("user " + login + " was parsed correctly");
				_config.addUserConfig(_currentUser);
			}
			else {
				ThreadLog.warning("ignoring user " + login);
			}
		}
	
		private void _addCurrentRepository() {
			boolean correctConfig = true;
	
			String id = _currentRepository.getID();
	
			// Check if all parameters are set.
			if (_currentRepository.getTitle() == null) {
				ThreadLog.error("title missing for repository " + id);
				correctConfig = false;
			}
			if (_currentRepository.getSailConfigStack().isEmpty()) {
				ThreadLog.error("no sail defined for repository " + id);
				correctConfig = false;
			}
	
			if (correctConfig) {
				ThreadLog.trace("repository " + id + " was parsed correctly");
				_config.addRepositoryConfig(_currentRepository);
			}
			else {
				ThreadLog.warning("ignoring repository " + id);
			}
		}
	}
}
