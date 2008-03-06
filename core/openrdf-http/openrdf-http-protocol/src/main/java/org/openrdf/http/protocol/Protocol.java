/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import info.aduna.lang.VarargsNullValueException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ntriples.NTriplesUtil;

public abstract class Protocol {

	/**
	 * Protocol version.
	 */
	public static final String VERSION = "4";

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
	 * Get the location of the namespace with the specified prefix for a specific
	 * repository on the specified server.
	 * 
	 * @param serverLocation
	 *        the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *        the ID of the repository
	 * @param prefix
	 *        the namespace prefix
	 * @return the location of the the namespace with the specified prefix for a
	 *         specific repository on the specified server
	 */
	public static final String getNamespacePrefixLocation(String serverLocation, String repositoryID,
			String prefix)
	{
		return getNamespacesLocation(serverLocation, repositoryID) + "/" + prefix;
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

	/**
	 * Append a properly encoded parameter to the query string of the specified
	 * URL.
	 * 
	 * @param url
	 *        the URL to append to
	 * @param parameterName
	 *        the name of parameter to append
	 * @param parameterValue
	 *        the value to append
	 * @return the URL with the specified parameter appended to it
	 */
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
	 * Encode a parameter for use in a URL.
	 * 
	 * @param parameterName
	 *        the name of the parameter
	 * @param parameterValue
	 *        the value
	 * @return the encoded parameter
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
	 * Encode a parameter for use in a URL.
	 * 
	 * @param parameterName
	 *        the name of the parameter
	 * @param parameterValue
	 *        the value
	 * @return the encoded parameter
	 */
	public static String encodeParameter(String parameterName, Value parameterValue) {
		return encodeParameter(parameterName, encodeValue(parameterValue));
	}

	/**
	 * Encodes a value for use in a URL.
	 * 
	 * @param value
	 *        The value to encode, possibly <tt>null</tt>.
	 * @return The N-Triples representation of the supplied value, or
	 *         {@link #NULL_PARAM_VALUE} if the supplied value was <tt>null</tt>.
	 */
	public static String encodeValue(Value value) {
		return NTriplesUtil.toNTriplesString(value);
	}

	/**
	 * Decode a previously encoded value.
	 * 
	 * @param encodedValue
	 *        the encoded value
	 * @param valueFactory
	 *        the factory to use for constructing the Value
	 * @return the decoded Value
	 * @see encodeValue(org.openrdf.model.Value)
	 */
	public static Value decodeValue(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue != null) {
			return NTriplesUtil.parseValue(encodedValue, valueFactory);
		}

		return null;
	}

	/**
	 * Decode a previously encoded Resource.
	 * 
	 * @param encodedValue
	 *        the encoded value
	 * @param valueFactory
	 *        the factory to use for constructing the Resource
	 * @return the decoded Resource
	 * @see encodeValue(org.openrdf.model.Value)
	 */
	public static Resource decodeResource(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue != null) {
			return NTriplesUtil.parseResource(encodedValue, valueFactory);
		}

		return null;
	}

	/**
	 * Decode a previously encoded URI.
	 * 
	 * @param encodedValue
	 *        the encoded value
	 * @param valueFactory
	 *        the factory to use for constructing the URI
	 * @return the decoded URI
	 * @see encodeValue(org.openrdf.model.Value)
	 */
	public static URI decodeURI(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue != null) {
			return NTriplesUtil.parseURI(encodedValue, valueFactory);
		}

		return null;
	}

	/**
	 * Encodes a context resource for use in a URL.
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
			return encodeValue(context);
		}
	}

	/**
	 * Decode a previously encoded context Resource.
	 * 
	 * @param encodedValue
	 *        the encoded value
	 * @param valueFactory
	 *        the factory to use for constructing the Resource
	 * @return the decoded Resource, or null if the encoded values was null or
	 *         equal to {@link #NULL_PARAM_VALUE}
	 */
	public static Resource decodeContext(String encodedValue, ValueFactory valueFactory) {
		if (encodedValue == null) {
			return null;
		}
		else if (NULL_PARAM_VALUE.equals(encodedValue)) {
			return null;
		}
		else {
			return decodeResource(encodedValue, valueFactory);
		}
	}

	/**
	 * Encode context resources for use in a URL.
	 * 
	 * @param contexts
	 *        the contexts to encode, must not be <tt>null</tt>.
	 * @return the encoded contexts
	 * @throws IllegalArgumentException
	 *         If the <tt>contexts</tt> is <tt>null</tt>.
	 */
	public static String[] encodeContexts(Resource... contexts) {
		verifyContext(contexts);

		String[] result = new String[contexts.length];
		for (int index = 0; index < contexts.length; index++) {
			result[index] = encodeContext(contexts[index]);
		}

		return result;
	}

	/**
	 * Decode previously encoded contexts.
	 * 
	 * @param encodedValues
	 *        the encoded values
	 * @param valueFactory
	 *        the factory to use for constructing the Resources
	 * @return the decoded Resources, or an empty array if the supplied
	 *         <tt>encodedValues</tt> was <tt>null</tt>.
	 */
	public static Resource[] decodeContexts(String[] encodedValues, ValueFactory valueFactory) {
		Resource[] result;

		if (encodedValues == null) {
			result = new Resource[0];
		}
		else {
			result = new Resource[encodedValues.length];
			for (int index = 0; index < encodedValues.length; index++) {
				result[index] = decodeContext(encodedValues[index], valueFactory);
			}
		}

		return result;
	}

	private static void verifyContext(Resource... contexts) {
		if (contexts == null) {
			throw new VarargsNullValueException();
		}
	}
}
