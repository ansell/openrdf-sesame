/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.console;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.rdf4j.http.client.SesameClient;
import org.eclipse.rdf4j.http.client.SesameClientImpl;
import org.eclipse.rdf4j.http.client.SesameSession;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dale
 */
public class Connect implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Connect.class);

	private final ConsoleState appInfo;

	private final ConsoleIO consoleIO;

	private final Disconnect disconnect;

	Connect(ConsoleIO consoleIO, ConsoleState info, Disconnect disconnect) {
		super();
		this.consoleIO = consoleIO;
		this.appInfo = info;
		this.disconnect = disconnect;
	}

	public void execute(String... tokens) {
		if (tokens.length < 2) {
			consoleIO.writeln(PrintHelp.CONNECT);
			return;
		}
		final String target = tokens[1];
		if ("default".equalsIgnoreCase(target)) {
			connectDefault();
		}
		else {
			try {
				new URL(target);
				// target is a valid URL
				final String username = (tokens.length > 2) ? tokens[2] : null; // NOPMD
				final String password = (tokens.length > 3) ? tokens[3] : null; // NOPMD
				connectRemote(target, username, password);
			}
			catch (MalformedURLException e) {
				// assume target is a directory path
				connectLocal(target);
			}
		}
	}

	protected boolean connectDefault() {
		return installNewManager(new LocalRepositoryManager(this.appInfo.getDataDirectory()),
				"default data directory");
	}

	private boolean connectRemote(final String url, final String user, final String passwd) {
		final String pass = (passwd == null) ? "" : passwd;
		boolean result = false;
		try {
			// Ping server
			final SesameClient client = new SesameClientImpl();
			try {
				SesameSession httpClient = client.createSesameSession(url);

				if (user != null) {
					httpClient.setUsernameAndPassword(user, pass);
				}

				// Ping the server
				httpClient.getServerProtocol();
			}
			finally {
				client.shutDown();
			}
			final RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
			manager.setUsernameAndPassword(user, pass);
			result = installNewManager(manager, url);
		}
		catch (UnauthorizedException e) {
			if (user != null && pass.length() > 0) {
				consoleIO.writeError("Authentication for user '" + user + "' failed");
				LOGGER.warn("Authentication for user '" + user + "' failed", e);
			}
			else {
				// Ask user for credentials
				try {
					consoleIO.writeln("Authentication required");
					final String username = consoleIO.readln("Username:");
					final String password = consoleIO.readPassword("Password:");
					connectRemote(url, username, password);
				}
				catch (IOException ioe) {
					consoleIO.writeError("Failed to read user credentials");
					LOGGER.warn("Failed to read user credentials", ioe);
				}
			}
		}
		catch (IOException e) {
			consoleIO.writeError("Failed to access the server: " + e.getMessage());
			LOGGER.warn("Failed to access the server", e);
		}
		catch (RepositoryException e) {
			consoleIO.writeError("Failed to access the server: " + e.getMessage());
			LOGGER.warn("Failed to access the server", e);
		}

		return result;
	}

	protected boolean connectLocal(final String path) {
		final File dir = new File(path);
		boolean result = false;
		if (dir.exists() && dir.isDirectory()) {
			result = installNewManager(new LocalRepositoryManager(dir), dir.toString());
		}
		else {
			consoleIO.writeError("Specified path is not an (existing) directory: " + path);
		}
		return result;
	}

	private boolean installNewManager(final RepositoryManager newManager, final String newManagerID) {
		boolean installed = false;
		final String managerID = this.appInfo.getManagerID();
		if (newManagerID.equals(managerID)) {
			consoleIO.writeln("Already connected to " + managerID);
			installed = true;
		}
		else {
			try {
				newManager.initialize();
				disconnect.execute(false);
				this.appInfo.setManager(newManager);
				this.appInfo.setManagerID(newManagerID);
				consoleIO.writeln("Connected to " + newManagerID);
				installed = true;
			}
			catch (RepositoryException e) {
				consoleIO.writeError(e.getMessage());
				LOGGER.error("Failed to install new manager", e);
			}
		}
		return installed;
	}

	protected boolean connectRemote(final String url) {
		return connectRemote(url, null, null);
	}
}
