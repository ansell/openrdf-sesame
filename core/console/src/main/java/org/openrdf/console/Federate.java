/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
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
import org.openrdf.repository.http.config.HTTPRepositoryFactory;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sparql.config.SPARQLRepositoryFactory;

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
			String type = getOptionalParamValue(plist, "type", "http");
			if ("http".equals(type) || "sparql".equals(type)) {
				if (distinctValues(plist)) {
					String fedID = plist.pop();
					federate(distinct, readonly, type, fedID, plist);
				}
				else {
					cio.writeError("Duplicate repository id's specified.");
				}
			}
			else {
				cio.writeError("Invalid type specified for federation members.");
			}
		}
	}

	private boolean distinctValues(Deque<String> plist) {
		return plist.size() == new HashSet<String>(plist).size();
	}

	private void federate(boolean distinct, boolean readonly, String type, String fedID,
			Deque<String> memberIDs)
	{
		if (LOGGER.isDebugEnabled()) {
			logCallDetails(distinct, readonly, type, fedID, memberIDs);
		}
		if ((!readonly) && "sparql".equals(type)) {
			cio.writeError("Federations with SPARQLRepository members must be read-only.");
		}
		else {
			RepositoryManager manager = state.getManager();
			try {
				String memberType = "sparql".equals(type) ? SPARQLRepositoryFactory.REPOSITORY_TYPE
						: HTTPRepositoryFactory.REPOSITORY_TYPE;
				if (manager.hasRepositoryConfig(fedID)) {
					cio.writeError(fedID + " already exists.");
				}
				else if (validateMembers(manager, memberIDs, memberType)) {
					String description = cio.readln("Federation Description (optional):");
					RepositoryManagerFederator rmf = new RepositoryManagerFederator(manager);
					rmf.addFed(memberType, fedID, description, memberIDs);
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

	private boolean validateMembers(RepositoryManager manager, Deque<String> memberIDs, String fedMemberType)
		throws MalformedURLException
	{
		boolean result = true;
		try {
			for (String memberID : memberIDs) {
				if (!manager.hasRepositoryConfig(memberID)) {
					result = false;
					cio.writeError(memberID + " does not exist.");
				}
				else {
					String memberType = manager.getRepositoryConfig(memberID).getRepositoryImplConfig().getType();
					boolean isHTTP = HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberType);
					if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(fedMemberType) && isHTTP) {
						result = false;
						cio.writeError(memberID + " is " + memberType + ", and can't be federated as "
								+ fedMemberType);
					}
					boolean isSPARQL = SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberType);
					if (isSPARQL && HTTPRepositoryFactory.REPOSITORY_TYPE.equals(fedMemberType)) {
						result = false;
						cio.writeError(memberID + " is " + memberType + ", and can't be federated as "
								+ fedMemberType);
					}
					boolean local = !manager.getLocation().getProtocol().substring(0, 4).equalsIgnoreCase("http");
					if (local && !(isSPARQL || isHTTP)) {
						result = false;
						cio.writeError("Connection is local, and " + memberID + " isn't a "
								+ HTTPRepositoryFactory.REPOSITORY_TYPE + " or "
								+ SPARQLRepositoryFactory.REPOSITORY_TYPE);
					}
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

	private void logCallDetails(boolean distinct, boolean readonly, String type, String fedID,
			Deque<String> memberIDs)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Federate called with federation ID = " + fedID + ", and member ID's = ");
		for (String member : memberIDs) {
			builder.append("[").append(member).append("]");
		}
		builder.append(".\n  Distinct set to ").append(distinct).append(", readonly set to ").append(readonly).append(
				", and type is ").append(type).append(".\n");
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