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
package org.openrdf.console;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.runtime.RepositoryManagerFederator;

/**
 * Implements the 'federate' command for the Sesame Console.
 * 
 * @author Dale Visser
 */
public class Federate implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Federate.class);

	private final ConsoleIO cio;

	private final ConsoleState state;

	protected Federate(ConsoleIO cio, ConsoleState state) {
		this.cio = cio;
		this.state = state;
	}

	/**
	 * Executes a 'federate' command for the Sesame Console.
	 * 
	 * @param parameters
	 *        the expectations for the tokens in this array are fully documented
	 *        in {@link PrintHelp#FEDERATE}.
	 */
	@Override
	public void execute(String... parameters)
		throws IOException
	{
		if (parameters.length < 4) {
			cio.writeln(PrintHelp.FEDERATE);
		}
		else {
			LinkedList<String> plist = new LinkedList<String>(Arrays.asList(parameters));
			plist.remove(); // "federate"
			boolean distinct = getOptionalParamValue(plist, "distinct", false);
			boolean readonly = getOptionalParamValue(plist, "readonly", true);
			if (distinctValues(plist)) {
				String fedID = plist.pop();
				federate(distinct, readonly, fedID, plist);
			}
			else {
				cio.writeError("Duplicate repository id's specified.");
			}
		}
	}

	private boolean distinctValues(Deque<String> plist) {
		return plist.size() == new HashSet<String>(plist).size();
	}

	private void federate(boolean distinct, boolean readonly, String fedID, Deque<String> memberIDs) {
		if (LOGGER.isDebugEnabled()) {
			logCallDetails(distinct, readonly, fedID, memberIDs);
		}
		else {
			RepositoryManager manager = state.getManager();
			try {
				if (manager.hasRepositoryConfig(fedID)) {
					cio.writeError(fedID + " already exists.");
				}
				else if (validateMembers(manager, readonly, memberIDs)) {
					String description = cio.readln("Federation Description (optional):");
					RepositoryManagerFederator rmf = new RepositoryManagerFederator(manager);
					rmf.addFed(fedID, description, memberIDs, readonly, distinct);
					cio.writeln("Federation created.");
				}
			}
			catch (RepositoryConfigException rce) {
				cio.writeError(rce.getMessage());
			}
			catch (RepositoryException re) {
				cio.writeError(re.getMessage());
			}
			catch (MalformedURLException mue) {
				cio.writeError(mue.getMessage());
			}
			catch (OpenRDFException ore) {
				cio.writeError(ore.getMessage());
			}
			catch (IOException ioe) {
				cio.writeError(ioe.getMessage());
			}
		}
	}

	private boolean validateMembers(RepositoryManager manager, boolean readonly, Deque<String> memberIDs) {
		boolean result = true;
		try {
			for (String memberID : memberIDs) {
				if (manager.hasRepositoryConfig(memberID)) {
					if (!readonly) {
						if (!manager.getRepository(memberID).isWritable()) {
							result = false;
							cio.writeError(memberID + " is read-only.");
						}
					}
				}
				else {
					result = false;
					cio.writeError(memberID + " does not exist.");
				}
			}
		}
		catch (RepositoryException re) {
			cio.writeError(re.getMessage());
		}
		catch (RepositoryConfigException rce) {
			cio.writeError(rce.getMessage());
		}
		return result;
	}

	private void logCallDetails(boolean distinct, boolean readonly, String fedID, Deque<String> memberIDs) {
		StringBuilder builder = new StringBuilder();
		builder.append("Federate called with federation ID = " + fedID + ", and member ID's = ");
		for (String member : memberIDs) {
			builder.append("[").append(member).append("]");
		}
		builder.append(".\n  Distinct set to ").append(distinct).append(", and readonly set to ").append(
				readonly).append(".\n");
		LOGGER.debug(builder.toString());
	}

	private boolean getOptionalParamValue(Deque<String> parameters, String name, boolean defaultValue) {
		return Boolean.parseBoolean(getOptionalParamValue(parameters, name, Boolean.toString(defaultValue)));
	}

	private String getOptionalParamValue(Deque<String> parameters, String name, String defaultValue) {
		String result = defaultValue;
		for (String parameter : parameters) {
			if (parameter.length() >= name.length()
					&& parameter.substring(0, name.length()).equalsIgnoreCase(name))
			{
				String[] parsed = parameter.split("=");
				if (parsed.length == 2 && parsed[0].equalsIgnoreCase(name)) {
					result = parsed[1].toLowerCase();
					parameters.remove(parameter);
					break;
				}
			}
		}
		return result;
	}
}
