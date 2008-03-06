package org.openrdf.protocol.rest;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.openrdf.model.Value;

public abstract class Protocol {

	/**
	 * Protocol version.
	 */
	public static final String VERSION = "2";
	
	/**
	 * Parameter name for the 'subject' parameter of a statement query.
	 */
	public static final String SUBJECT_PARAM_NAME = "sub";

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
	public static final String INCLUDE_INFERRED_PARAM_NAME = "includeInferred";

	/**
	 * Parameter name for the context parameter.
	 */
	public static final String CONTEXT_PARAM_NAME = "context";

	/**
	 * Parameter value for the NULL context.
	 */
	public static final String NULL_CONTEXT_PARAM_VALUE = "null";

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
	 * Parameter name for the namespace parameter.
	 */
	public static final String NAMESPACE_PARAM_NAME = "ns";

	/**
	 * Parameter name for the namespace prefix parameter.
	 */
	public static final String NAMESPACE_PREFIX_PARAM_NAME = "prefix";

	/**
	 * Parameter name for the Accept parameter (may also be used as the name of
	 * the Accept HTTP header).
	 */
	public static final String ACCEPT_PARAM_NAME = "Accept";

	/**
	 * Relative location of the protocol resource.
	 */
	public static final String PROTOCOL = "/protocol";

	/**
	 * Relative location of the config resource.
	 */
	public static final String CONFIG = "/config";

	/**
	 * Relative location of the repository list resource.
	 */
	public static final String REPOSITORIES = "/repositories";

	/**
	 * Relative location of the context list resource of a repository.
	 */
	public static final String CONTEXTS = "/contexts";

	/**
	 * Relative location of the namespaces list resource of a repository.
	 */
	public static final String NAMESPACES = "/namespaces";

	/**
	 * Get the location of the protocol resource on the specified server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @return the location of the protocol resource on the specified server
	 */
	public static final String getProtocolLocation(String serverLocation) {
		StringBuilder result = new StringBuilder(serverLocation);

		removeSlashIfNeeded(result);
		result.append(PROTOCOL);

		return result.toString();
	}

	/**
	 * Get the URL of the protocol resource on the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @return the URL of the protocol resource on the specified server
	 */
	public static final URL getProtocolUrl(URL serverUrl) throws MalformedURLException {
		return new URL(getProtocolLocation(serverUrl.toExternalForm()));
	}

	/**
	 * Get the location of the server configuration resource on the specified
	 * server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @return the location of the server configuration resource on the
	 *         specified server
	 */
	public static final String getConfigLocation(String serverLocation) {
		StringBuilder result = new StringBuilder(serverLocation);

		removeSlashIfNeeded(result);
		result.append(CONFIG);

		return result.toString();
	}

	/**
	 * Get the URL of the server configuration resource on the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @return the URL of the protocol resource on the specified server
	 */
	public static final URL getConfigUrl(URL serverUrl) throws MalformedURLException {
		return new URL(getConfigLocation(serverUrl.toExternalForm()));
	}

	/**
	 * Get the location of the repository list resource on the specified server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @return the location of the repository list resource on the specified
	 *         server
	 */
	public static final String getRepositoriesLocation(String serverUrl) {
		StringBuilder result = new StringBuilder(serverUrl);

		removeSlashIfNeeded(result);
		result.append(REPOSITORIES);

		return result.toString();
	}

	/**
	 * Get the URL of the repository list resource on the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @return the URL of the repository list resource on the specified server
	 */
	public static final URL getRepositoriesUrl(URL serverUrl) throws MalformedURLException {
		return new URL(getRepositoriesLocation(serverUrl.toExternalForm()));
	}

	/**
	 * Get the location of a specific repository resource on the specified
	 * server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the location of a specific repository resource on the specified
	 *         server
	 */
	public static final String getRepositoryLocation(String serverLocation, String repositoryID) {
		StringBuilder result = new StringBuilder(getRepositoriesLocation(serverLocation));

		if (!repositoryID.startsWith("/")) {
			result.append("/");
		}
		result.append(repositoryID);

		return result.toString();
	}

	/**
	 * Get the URL of a specific repository resource on the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the URL of a specific repository resource on the specified server
	 */
	public static final URL getRepositoryUrl(URL serverUrl, String repositoryID) throws MalformedURLException {
		return new URL(getRepositoryLocation(serverUrl.toExternalForm(), repositoryID));
	}

	/**
	 * Get the location of the contexts lists resource for a specific repository
	 * on the specified server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the location of the contexts lists resource for a specific
	 *         repository on the specified server
	 */
	public static final String getContextsLocation(String serverLocation, String repositoryID) {
		StringBuilder result = new StringBuilder(getRepositoryLocation(serverLocation, repositoryID));

		removeSlashIfNeeded(result);
		result.append(CONTEXTS);

		return result.toString();
	}

	/**
	 * Get the URL of the contexts lists resource for a specific repository on
	 * the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the URL of the contexts lists resource for a specific repository
	 *         on the specified server
	 */
	public static final URL getContextsUrl(URL serverUrl, String repositoryID) throws MalformedURLException {
		return new URL(getContextsLocation(serverUrl.toExternalForm(), repositoryID));
	}

	/**
	 * Get the location of a specific context resource for a specific repository
	 * on the specified server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the location of a specific context resource for a specific
	 *         repository on the specified server
	 */
	public static final String getContextLocation(String serverLocation, String repositoryID, Value context) {
		StringBuilder result = new StringBuilder(getRepositoryLocation(serverLocation, repositoryID));

		result.append(buildContextQuery(context));

		return result.toString();
	}

	/**
	 * Get the URL of a specific context resource for a specific repository on
	 * the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the URL of a specific context resource for a specific repository
	 *         on the specified server
	 */
	public static final URL getContextUrl(URL serverUrl, String repositoryID, Value context) throws MalformedURLException {
		return new URL(getContextLocation(serverUrl.toExternalForm(), repositoryID, context));
	}

	/**
	 * Get the location of the namespaces lists resource for a specific
	 * repository on the specified server.
	 * 
	 * @param serverLocation
	 *            the base location of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the location of the namespaces lists resource for a specific
	 *         repository on the specified server
	 */
	public static final String getNamespacesLocation(String serverLocation, String repositoryID) {
		StringBuilder result = new StringBuilder(getRepositoryLocation(serverLocation, repositoryID));

		removeSlashIfNeeded(result);
		result.append(NAMESPACES);

		return result.toString();
	}

	/**
	 * Get the URL of the namespaces lists resource for a specific repository on
	 * the specified server.
	 * 
	 * @param serverUrl
	 *            the base URL of a server implementing this REST protocol.
	 * @param repositoryID
	 *            the ID of the repository
	 * @return the URL of the namespaces lists resource for a specific
	 *         repository on the specified server
	 */
	public static final URL getNamespacesUrl(URL serverUrl, String repositoryID) throws MalformedURLException {
		return new URL(getNamespacesLocation(serverUrl.toExternalForm(), repositoryID));
	}

	/**
	 * If there is a slash at the end of the input, remove it.
	 * 
	 * @param input
	 *            the input
	 */
	private static void removeSlashIfNeeded(StringBuilder input) {
		if (input.length() > 0 && input.charAt(input.length() - 1) == '/') {
			input.setLength(input.length() - 1);
		}
	}

	/**
	 * Build a properly encoded query string indicating the specified context.
	 * 
	 * @param context
	 *            the context. If null, the query string will indicate the NULL
	 *            context
	 * @return a query string indicating the specified context
	 */
	private static final String buildContextQuery(Value context) {
		String result = null;
		if (context == null) {
			result = buildContextQueryFromEncodedContext(Protocol.NULL_CONTEXT_PARAM_VALUE);
		}
		else {
			result = buildContextQueryFromEncodedContext(ProtocolUtil.encodeParameterValue(context));
		}
		return result;
	}

	/**
	 * Build a URL-encoded query string indicating the specified context,
	 * assuming the input is already encoded according to protocol.
	 * 
	 * @param encodedContext
	 *            the encoded context
	 * @return a URL-encoded query string indicating the specified context
	 */
	private static String buildContextQueryFromEncodedContext(String encodedContext) {
		StringBuilder result = new StringBuilder("?");
		result.append(CONTEXT_PARAM_NAME);
		result.append("=");
		try {
			result.append(URLEncoder.encode(encodedContext, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			// this should never happen, UTF-8 support is required by the
			// Java spec
			e.printStackTrace();
		}
		return result.toString();
	}
}
