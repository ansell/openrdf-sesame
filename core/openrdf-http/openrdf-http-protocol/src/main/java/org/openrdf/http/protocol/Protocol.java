/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ntriples.NTriplesUtil;

public abstract class Protocol {

	/**
	 * Protocol version.
	 */
	public static final String VERSION = "3";

	/**
	 * Parameter name for the 'subject' parameter of a statement query.
	 */
	public static final String SUBJECT_PARAM_NAME = "subj";

	/**
	 * Parameter name for the 'predicate' parameter of a statement query.
	 */
	public static final String PREDICATE_PARAM_NAME = "pred";

	/**
	 * Parameter name for the 'object' parameter of statement query.
	 */
	public static final String OBJECT_PARAM_NAME = "obj";

	/**
	 * Parameter name for the 'includeReferred' parameter of a statement query.
	 */
	public static final String INCLUDE_INFERRED_PARAM_NAME = "infer";

	/**
	 * Parameter name for the context parameter.
	 */
	public static final String CONTEXT_PARAM_NAME = "context";

	/**
	 * Parameter value for the NULL context.
	 */
	public static final String NULL_PARAM_VALUE = "null";

	/**
	 * Parameter name for the base-URI parameter.
	 */
	public static final String BASEURI_PARAM_NAME = "baseURI";

	/**
	 * Parameter name for the query parameter.
	 */
	public static final String QUERY_PARAM_NAME = "query";

	/**
	 * Parameter name for the query language parameter.
	 */
	public static final String QUERY_LANGUAGE_PARAM_NAME = "queryLn";

	/**
	 * Parameter name for the Accept parameter (may also be used as the name of
	 * the Accept HTTP header).
	 */
	public static final String ACCEPT_PARAM_NAME = "Accept";

	/**
	 * Relative location of the protocol resource.
	 */
	public static final String PROTOCOL = "protocol";

	/**
	 * Relative location of the config resource.
	 */
	public static final String CONFIG = "config";

	/**
	 * Relative location of the repository list resource.
	 */
	public static final String REPOSITORIES = "repositories";

	/**
	 * Relative location of the statement list resource of a repository.
	 */
	public static final String STATEMENTS = "statements";

	/**
	 * Relative location of the context list resource of a repository.
	 */
	public static final String CONTEXTS = "contexts";

	/**
	 * Relative location of the namespaces list resource of a repository.
	 */
	public static final String NAMESPACES = "namespaces";

	/**
	 * Relative location of the 'size' resource of a repository.
	 */
	public static final String SIZE = "size";

	/**
	 * MIME type for transactions: <tt>application/x-rdftransaction</tt>.
	 */
	public static final String TXN_MIME_TYPE = "application/x-rdftransaction";

	/**
	 * MIME type for www forms: <tt>application/x-www-form-urlencoded</tt>.
	 */
	public static final String FORM_MIME_TYPE = "application/x-www-form-urlencoded";

	private static String getServerDir(String serverLocation) {
		if (serverLocation.endsWith("/")) {
			return serverLocation;
		}
		else {
			return serverLocation + "/";
		}
	}

	/**
	 * Get the location of the protocol resource on the specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @return the location of the protocol resource on the specified server
	 */
	public static final String getProtocolLocation(String serverLocation) {
		return getServerDir(serverLocation) + PROTOCOL;
	}

	/**
	 * Get the location of the server configuration resource on the specified
	 * server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @return the location of the server configuration resource on the specified
	 *         server
	 */
	public static final String getConfigLocation(String serverLocation) {
		return getServerDir(serverLocation) + CONFIG;
	}

	/**
	 * Get the location of the repository list resource on the specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @return the location of the repository list resource on the specified
	 *         server
	 */
	public static final String getRepositoriesLocation(String serverLocation) {
		return getServerDir(serverLocation) + REPOSITORIES;
	}

	/**
	 * Get the location of a specific repository resource on the specified
	 * server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *        the ID of the repository
	 * @return the location of a specific repository resource on the specified
	 *         server
	 */
	public static final String getRepositoryLocation(String serverLocation, String repositoryID) {
		return getRepositoriesLocation(serverLocation) + "/" + repositoryID;
	}

	/**
	 * Get the location of the statements resource for a specific repository on
	 * the specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *        the ID of the repository
	 * @return the location of the statements resource for a specific repository
	 *         on the specified server
	 */
	public static final String getStatementsLocation(String serverLocation, String repositoryID) {
		return getRepositoryLocation(serverLocation, repositoryID) + "/" + STATEMENTS;
	}

	/**
	 * Get the location of the contexts lists resource for a specific repository
	 * on the specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *        the ID of the repository
	 * @return the location of the contexts lists resource for a specific
	 *         repository on the specified server
	 */
	public static final String getContextsLocation(String serverLocation, String repositoryID) {
		return getRepositoryLocation(serverLocation, repositoryID) + "/" + CONTEXTS;
	}

	/**
	 * Get the location of the namespaces lists resource for a specific
	 * repository on the specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *        the ID of the repository
	 * @return the location of the namespaces lists resource for a specific
	 *         repository on the specified server
	 */
	public static final String getNamespacesLocation(String serverLocation, String repositoryID) {
		return getRepositoryLocation(serverLocation, repositoryID) + "/" + NAMESPACES;
	}

	/**
	 * Get the location of the 'size' resource for a specific repository on the
	 * specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *        the ID of the repository
	 * @return the location of the 'size' resource for a specific repository on
	 *         the specified server
	 */
	public static final String getSizeLocation(String serverLocation, String repositoryID) {
		return getRepositoryLocation(serverLocation, repositoryID) + "/" + SIZE;
	}

	public static final String getNamespacePrefixLocation(String serverLocation, String repositoryID,
			String prefix)
	{
		return getNamespacesLocation(serverLocation, repositoryID) + "/" + prefix;
	}

	public static final String appendParameter(String url, String parameterName, String parameterValue) {
		StringBuilder result = new StringBuilder(url);

		if (url.indexOf('?') >= 0) {
			result.append('&');
		}
		else {
			result.append('?');
		}

		result.append(encodeParameter(parameterName, parameterValue));

		return result.toString();
	}

	/**
	 * @param parameterName
	 * @param parameterValue
	 * @return
	 */
	public static String encodeParameter(String parameterName, String parameterValue) {
		StringBuilder result = new StringBuilder();
		try {
			result.append(URLEncoder.encode(parameterName, "UTF-8"));
			result.append('=');
			result.append(URLEncoder.encode(parameterValue, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			// nop, UTF-8 is supported
		}

		return result.toString();
	}

	/**
	 * @param parameterName
	 * @param parameterValue
	 * @return
	 */
	public static String encodeParameter(String parameterName, Value parameterValue) {
		return encodeParameter(parameterName, encodeValue(parameterValue));
	}

	/**
	 * Encodes a value for sending it as a query parameter in an HTTP request.
	 * 
	 * @param value
	 *        The value to encode, possibly <tt>null</tt>.
	 * @return The N-Triples representation of the supplied value, or
	 *         {@link #NULL_PARAM_VALUE} if the supplied value was <tt>null</tt>.
	 */
	public static String encodeValue(Value value) {
		return NTriplesUtil.toNTriplesString(value);
	}

	public static Value decodeValue(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue != null) {
			return NTriplesUtil.parseValue(encodedValue, valueFactory);
		}

		return null;
	}

	public static Resource decodeResource(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue != null) {
			return NTriplesUtil.parseResource(encodedValue, valueFactory);
		}

		return null;
	}

	public static URI decodeURI(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue != null) {
			return NTriplesUtil.parseURI(encodedValue, valueFactory);
		}

		return null;
	}

	/**
	 * Encodes a context resource for sending it as a query parameter in an HTTP
	 * request.
	 * 
	 * @param value
	 *        The resource to encode, possibly <tt>null</tt>.
	 * @return The N-Triples representation of the supplied resource, or
	 *         {@link #NULL_PARAM_VALUE} if the supplied value was <tt>null</tt>.
	 */
	public static String encodeContext(Resource context) {
		if (context == null) {
			return Protocol.NULL_PARAM_VALUE;
		}
		else {
			return NTriplesUtil.toNTriplesString(context);
		}
	}

	public static Resource decodeContext(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue == null) {
			return null;
		}
		else if (NULL_PARAM_VALUE.equals(encodedValue)) {
			return null;
		}
		else {
			return NTriplesUtil.parseResource(encodedValue, valueFactory);
		}
	}
}
