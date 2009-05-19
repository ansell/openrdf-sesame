/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import org.openrdf.http.protocol.Protocol;

/**
 * Defines resource path templates for the controllers.
 * 
 * @author Arjohn Kampman
 */
public class Paths {

	public static final String SCHEMAS = "/" + Protocol.SCHEMAS;;

	public static final String PROTOCOL = "/" + Protocol.PROTOCOL;;

	/*
	 * Configurations
	 */

	public static final String CONFIGURATIONS = "/" + Protocol.CONFIGURATIONS;;

	public static final String CONFIGURATION_ID = CONFIGURATIONS + "/*";

	/*
	 * Templates
	 */

	public static final String TEMPLATES = "/" + Protocol.TEMPLATES;;

	public static final String TEMPLATE_ID = TEMPLATES + "/*";

	/*
	 * Repositories
	 */

	public static final String REPOSITORIES = "/" + Protocol.REPOSITORIES;;

	public static final String REPOSITORY_ID = REPOSITORIES + "/*";

	public static final String REPOSITORY_STATEMENTS = REPOSITORY_ID + "/" + Protocol.STATEMENTS;

	public static final String REPOSITORY_CONTEXTS = REPOSITORY_ID + "/" + Protocol.CONTEXTS;

	public static final String REPOSITORY_SIZE = REPOSITORY_ID + "/" + Protocol.SIZE;

	public static final String REPOSITORY_METADATA = REPOSITORY_ID + "/" + Protocol.METADATA;

	/*
	 * Connections
	 */

	public static final String CONNECTIONS = REPOSITORY_ID + "/" + Protocol.CONNECTIONS;

	public static final String CONNECTION_ID = CONNECTIONS + "/*";

	public static final String CONNECTION_STATEMENTS = CONNECTION_ID + "/" + Protocol.STATEMENTS;

	public static final String CONNECTION_CONTEXTS = CONNECTION_ID + "/" + Protocol.CONTEXTS;

	public static final String CONNECTION_BNODES = CONNECTION_ID + "/" + Protocol.BNODES;

	public static final String CONNECTION_SIZE = CONNECTION_ID + "/" + Protocol.SIZE;

	public static final String CONNECTION_METADATA = CONNECTION_ID + "/" + Protocol.METADATA;

	/*
	 * Transactions
	 */

	public static final String CONNECTION_BEGIN = CONNECTION_ID + "/" + Protocol.BEGIN;

	public static final String CONNECTION_COMMIT = CONNECTION_ID + "/" + Protocol.COMMIT;

	public static final String CONNECTION_ROLLBACK = CONNECTION_ID + "/" + Protocol.ROLLBACK;

	public static final String CONNECTION_PING = CONNECTION_ID + "/" + Protocol.PING;

	/*
	 * Queries
	 */

	public static final String CONNECTION_QUERIES = CONNECTION_ID + "/" + Protocol.QUERIES;

	public static final String CONNECTION_QUERY_ID = CONNECTION_QUERIES + "/*";

	/*
	 * Namespaces
	 */

	public static final String REPOSITORY_NAMESPACES = REPOSITORY_ID + "/" + Protocol.NAMESPACES;

	public static final String CONNECTION_NAMESPACES = CONNECTION_ID + "/" + Protocol.NAMESPACES;

	public static final String REPOSITORY_NAMESPACE_PREFIX = REPOSITORY_NAMESPACES + "/*";

	public static final String CONNECTION_NAMESPACE_PREFIX = CONNECTION_NAMESPACES + "/*";

}
