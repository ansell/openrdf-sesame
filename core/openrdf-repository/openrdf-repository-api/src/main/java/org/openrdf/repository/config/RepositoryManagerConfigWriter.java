/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

import info.aduna.xml.XMLWriter;

@Deprecated
public class RepositoryManagerConfigWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManagerConfig _config;
	private XMLWriter _xmlWriter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RepositoryManagerConfigWriter() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	// FIXME: make the methods in this class static?
	
	public synchronized void write(RepositoryManagerConfig systemConfig, OutputStream outputStream)
		throws IOException
	{
		_write(systemConfig, new XMLWriter(outputStream));
	}

	public synchronized void write(RepositoryManagerConfig systemConfig, Writer writer)
		throws IOException
	{
		_write(systemConfig, new XMLWriter(writer));
	}

	private void _write(RepositoryManagerConfig systemConfig, XMLWriter xmlWriter)
		throws IOException
	{
		_config = systemConfig;
		_xmlWriter = xmlWriter;
		_xmlWriter.setPrettyPrint(true);

		_xmlWriter.startDocument();

		_writeHeader();

		_xmlWriter.emptyLine();
		_xmlWriter.comment("admin");
		_writeAdmin();

		_xmlWriter.emptyLine();
		_xmlWriter.comment("users");
		_writeUsers();

		_xmlWriter.emptyLine();
		_xmlWriter.comment("repositories");
		_writeRepositories();

		_writeFooter();

		_xmlWriter.endDocument();
	}

	private void _writeHeader()
		throws IOException
	{
		_xmlWriter.startTag("openrdf-sesame");
	}

	private void _writeFooter()
		throws IOException
	{
		_xmlWriter.endTag("openrdf-sesame");
	}

	private void _writeAdmin()
		throws IOException
	{
		_xmlWriter.setAttribute("password", _config.getAdminPassword());
		_xmlWriter.emptyElement("admin");
	}

	private void _writeUsers()
		throws IOException
	{
		_xmlWriter.startTag("userlist");

		boolean firstElement = true;
		for (UserConfig userInfo : _config.getUserConfigs()) {
			if (!firstElement) {
				_xmlWriter.emptyLine();
			}
			else {
				firstElement = false;
			}

			_xmlWriter.setAttribute("login", userInfo.getLogin());
			_xmlWriter.startTag("user");

			_xmlWriter.textElement("fullname", userInfo.getFullName());
			_xmlWriter.textElement("password", userInfo.getPassword());

			_xmlWriter.endTag("user");
		}

		_xmlWriter.endTag("userlist");
	}

	private void _writeRepositories()
		throws IOException
	{
		_xmlWriter.startTag("repositorylist");

		boolean firstElement = true;
		for (RepositoryConfig repConfig : _config.getRepositoryConfigs()) {
			if (!firstElement) {
				_xmlWriter.emptyLine();
			}
			else {
				firstElement = false;
			}

			_xmlWriter.setAttribute("id", repConfig.getID());
			if (repConfig.getClassName() != null) {
				_xmlWriter.setAttribute("class", repConfig.getClassName());
			}
			_xmlWriter.startTag("repository");

			_xmlWriter.textElement("title", repConfig.getTitle());

			_writeSailStack(repConfig);
			_writeAcl(repConfig);

			_xmlWriter.endTag("repository");
		}

		_xmlWriter.endTag("repositorylist");
	}

	private void _writeSailStack(RepositoryConfig repConfig)
		throws IOException
	{
		_xmlWriter.startTag("sailstack");

		for (SailConfig sailConfig : repConfig.getSailConfigStack()) {
			_writeSail(sailConfig);
		}

		_xmlWriter.endTag("sailstack");
	}

	private void _writeSail(SailConfig sailConfig)
		throws IOException
	{
		_xmlWriter.setAttribute("class", sailConfig.getClassName());

		List<SailParameter> params = sailConfig.getParameters();

		if (params.isEmpty()) {
			// Sail without initialization parameters
			_xmlWriter.emptyElement("sail");
		}
		else {
			// Sail with initialization parameters
			_xmlWriter.startTag("sail");

			for (SailParameter param : params) {
				_xmlWriter.setAttribute("name", param.getName());
				_xmlWriter.setAttribute("value", param.getValue());
				_xmlWriter.emptyElement("param");
			}

			_xmlWriter.endTag("sail");
		}
	}

	private void _writeAcl(RepositoryConfig repConfig)
		throws IOException
	{
		_xmlWriter.setAttribute("worldReadable", repConfig.isWorldReadable());
		_xmlWriter.setAttribute("worldWritable", repConfig.isWorldWritable());

		HashSet<String> users = new HashSet<String>();
		users.addAll(repConfig.getReadACL());
		users.addAll(repConfig.getWriteACL());
		
		if (users.isEmpty()) {
			_xmlWriter.emptyElement("acl");
		}
		else {
			_xmlWriter.startTag("acl");

			for (String login : users) {				
				_xmlWriter.setAttribute("login", login);
				_xmlWriter.setAttribute("readAccess", repConfig.hasReadAccess(login));
				_xmlWriter.setAttribute("writeAccess", repConfig.hasWriteAccess(login));
				_xmlWriter.emptyElement("user");
			}

			_xmlWriter.endTag("acl");
		}
	}
}
