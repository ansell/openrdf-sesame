/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction;

/**
 * Interface defining tags and attribute names for the XML serialization of
 * transactions.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
interface TransactionXMLConstants {

	public static final String TRANSACTION_TAG = "transaction";

	public static final String ADD_STATEMENT_TAG = "add";

	public static final String REMOVE_STATEMENTS_TAG = "remove";

	public static final String REMOVE_NAMED_CONTEXT_STATEMENTS_TAG = "removeFromNamedContext";

	public static final String CLEAR_TAG = "clear";

	public static final String NULL_TAG = "null";

	public static final String URI_TAG = "uri";

	public static final String BNODE_TAG = "bnode";

	public static final String LITERAL_TAG = "literal";

	public static final String LANG_ATT = "xml:lang";

	public static final String DATATYPE_ATT = "datatype";

	public static final String SET_NAMESPACE_TAG = "setNamespace";

	public static final String REMOVE_NAMESPACE_TAG = "removeNamespace";

	public static final String PREFIX_ATT = "prefix";

	public static final String NAME_ATT = "name";

	public static final String CLEAR_NAMESPACES_TAG = "clearNamespaces";

	public static final String CONTEXTS_TAG = "contexts";
}
