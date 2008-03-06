package org.openrdf.protocol.rest;

import static org.openrdf.protocol.rest.Protocol.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import junit.framework.TestCase;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.ntriples.NTriplesUtil;

public class ProtocolTest extends TestCase {

	private URL serverUrl;

	private String serverLocation;

	private String repositoryId;

	private URI context = new URIImpl("urn:x-local:graph1");

	public void setUp() {
		serverLocation = "http://localhost/openrdf";
		try {
			serverUrl = new URL(serverLocation);
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repositoryId = "mem-rdf";
	}

	public void testGetProtocolLocation() {
		String result = getProtocolLocation(serverLocation);
		assertEquals(result, serverLocation + PROTOCOL);
	}

	public void testGetProtocolUrl() throws MalformedURLException {
		URL result = getProtocolUrl(serverUrl);
		assertEquals(result, new URL(serverLocation + PROTOCOL));
	}

	public void testGetConfigLocation() {
		String result = getConfigLocation(serverLocation);
		assertEquals(result, serverLocation + CONFIG);
	}

	public void testGetConfigUrl() throws MalformedURLException {
		URL result = getConfigUrl(serverUrl);
		assertEquals(result, new URL(serverLocation + CONFIG));
	}

	public void testGetRepositoriesLocation() {
		String result = getRepositoriesLocation(serverLocation);
		assertEquals(result, serverLocation + REPOSITORIES);
	}

	public void testGetRepositoriesUrl() throws MalformedURLException {
		URL result = getRepositoriesUrl(serverUrl);
		assertEquals(result, new URL(serverLocation + REPOSITORIES));
	}

	public void testGetRepositoryLocation() {
		String result = getRepositoryLocation(serverLocation, repositoryId);
		assertEquals(result, serverLocation + REPOSITORIES + "/" + repositoryId);
	}

	public void testGetRepositoryUrl() throws MalformedURLException {
		URL result = getRepositoryUrl(serverUrl, repositoryId);
		assertEquals(result, new URL(serverLocation + REPOSITORIES + "/" + repositoryId));
	}

	public void testGetContextsLocation() {
		String result = getContextsLocation(serverLocation, repositoryId);
		assertEquals(result, serverLocation + REPOSITORIES + "/" + repositoryId + CONTEXTS);
	}

	public void testGetContextsUrl() throws MalformedURLException {
		URL result = getContextsUrl(serverUrl, repositoryId);
		assertEquals(result, new URL(serverLocation + REPOSITORIES + "/" + repositoryId + CONTEXTS));
	}

	public void testGetNamespacesLocation() {
		String result = getNamespacesLocation(serverLocation, repositoryId);
		assertEquals(result, serverLocation + REPOSITORIES + "/" + repositoryId + NAMESPACES);
	}

	public void testGetNamespacesUrl() throws MalformedURLException {
		URL result = getNamespacesUrl(serverUrl, repositoryId);
		assertEquals(result, new URL(serverLocation + REPOSITORIES + "/" + repositoryId + NAMESPACES));
	}

	public void testGetNullContextLocation() {
		String result = getContextLocation(serverLocation, repositoryId, null);
		assertEquals(result, serverLocation + REPOSITORIES + "/" + repositoryId + "?" + CONTEXT_PARAM_NAME + "="
				+ NULL_CONTEXT_PARAM_VALUE);

	}

	public void testGetNullContextUrl() throws MalformedURLException {
		URL result = getContextUrl(serverUrl, repositoryId, null);
		assertEquals(result, new URL(serverLocation + REPOSITORIES + "/" + repositoryId + "?" + CONTEXT_PARAM_NAME + "="
				+ NULL_CONTEXT_PARAM_VALUE));
	}

	public void testGetNamedContextLocation() throws UnsupportedEncodingException {
		String result = getContextLocation(serverLocation, repositoryId, context);
		assertEquals(result, serverLocation + REPOSITORIES + "/" + repositoryId + "?" + CONTEXT_PARAM_NAME + "="
				+ URLEncoder.encode(NTriplesUtil.toNTriplesString(context), "UTF-8"));
	}
	
	public void testGetNamedContextUrl() throws MalformedURLException, UnsupportedEncodingException {
		URL result = getContextUrl(serverUrl, repositoryId, context);
		assertEquals(result, new URL(serverLocation + REPOSITORIES + "/" + repositoryId + "?" + CONTEXT_PARAM_NAME + "="
				+ URLEncoder.encode(NTriplesUtil.toNTriplesString(context), "UTF-8")));
	}
	
	public void testConsistency() throws MalformedURLException {
		assertEquals(getProtocolLocation(serverLocation), getProtocolUrl(serverUrl).toExternalForm());
		assertEquals(getConfigLocation(serverLocation), getConfigUrl(serverUrl).toExternalForm());
		assertEquals(getRepositoriesLocation(serverLocation), getRepositoriesUrl(serverUrl).toExternalForm());
		assertEquals(getRepositoryLocation(serverLocation, repositoryId), getRepositoryUrl(serverUrl, repositoryId).toExternalForm());
		assertEquals(getContextsLocation(serverLocation, repositoryId), getContextsUrl(serverUrl, repositoryId).toExternalForm());
		assertEquals(getContextLocation(serverLocation, repositoryId, null), getContextUrl(serverUrl, repositoryId, null).toExternalForm());
		assertEquals(getContextLocation(serverLocation, repositoryId, context), getContextUrl(serverUrl, repositoryId, context).toExternalForm());
		assertEquals(getNamespacesLocation(serverLocation, repositoryId), getNamespacesUrl(serverUrl, repositoryId).toExternalForm());
	}
	
}
