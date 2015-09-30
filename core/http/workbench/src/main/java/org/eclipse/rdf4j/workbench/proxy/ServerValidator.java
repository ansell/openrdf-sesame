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
package org.eclipse.rdf4j.workbench.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a server
 */
class ServerValidator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerValidator.class);
	private static final String ACCEPTED_SERVER = "accepted-server-prefixes";
	private final String prefixes;

	protected ServerValidator(final ServletConfig config) {
		this.prefixes = config.getInitParameter(ACCEPTED_SERVER);
	}
	
	private boolean isDirectory(final String server) {
		boolean isDir = false;
		try {
			final URL url = new URL(server);
			isDir = asLocalFile(url).isDirectory();
		}
		catch (MalformedURLException e) {
			LOGGER.warn(e.toString(), e);
		}
		catch (IOException e) {
			LOGGER.warn(e.toString(), e);
		}
		return isDir;
	}

	/**
	 * Returns whether the given server can be connected to.
	 * 
	 * @param server
	 *        the server path
	 * @param password
	 *        the optional password
	 * @param user
	 *        the optional username
	 * @return true, if the given server can be connected to
	 */
	protected boolean isValidServer(final String server) {
		boolean isValid = checkServerPrefixes(server);
		if (isValid) {
			if (server.startsWith("http")) {
				isValid = canConnect(server);
			}
			else if (server.startsWith("file:")) {
				isValid = isDirectory(server);
			}
		}
		return isValid;
	}
	
	/**
	 * Returns whether the server prefix is in the list of acceptable prefixes,
	 * as given by the space-separated configuration parameter value for
	 * 'accepted-server-prefixes'.
	 * 
	 * @param server
	 *        the server for which to check the prefix
	 * @return whether the server prefix is in the list of acceptable prefixes
	 */
	private boolean checkServerPrefixes(final String server) {
		boolean accept = false;
		if (prefixes == null) {
			accept = true;
		}
		else {
			for (String prefix : prefixes.split(" ")) {
				if (server.startsWith(prefix)) {
					accept = true;
					break;
				}
			}
		}
		if (!accept) {
			LOGGER.warn("server URL {} does not have a prefix {}", server, prefixes);
		}
		return accept;
	}
	
	/**
	 * Assumption: server won't require credentials to access the
	 * protocol path.
	 */
	private boolean canConnect(final String server) {
		boolean success = false;
		try {
			final URL url = new URL(server + "/protocol");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			try {
				Integer.parseInt(reader.readLine());
				success = true;
			}
			finally {
				reader.close();
			}
		}
		catch (MalformedURLException e) {
			LOGGER.warn(e.toString(), e);
		}
		catch (IOException e) {
			LOGGER.warn(e.toString(), e);
		}
		return success;
	}
	
	private File asLocalFile(final URL rdf)
			throws UnsupportedEncodingException
		{
			return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
		}
}