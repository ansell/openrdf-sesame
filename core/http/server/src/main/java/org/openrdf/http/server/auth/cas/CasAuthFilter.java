/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.auth.cas;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.openrdf.http.protocol.cas.AuthFailure;
import org.openrdf.http.protocol.cas.AuthSuccess;
import org.openrdf.http.protocol.cas.CasParseException;
import org.openrdf.http.protocol.cas.ProxyGrantingTicketRegistry;
import org.openrdf.http.protocol.cas.ServiceResponse;
import org.openrdf.http.protocol.cas.ServiceResponseParser;
import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;

/**
 * @author Arjohn Kampman
 */
// FIXME: make this an Authenticator subclass?
public final class CasAuthFilter extends Filter {

	private final Logger logger = LoggerFactory.getLogger(CasAuthFilter.class);

	/**
	 * The URL to the CAS Server login, e.g.http://localhost:8080/cas/
	 */
	private final String casServerURL;

	private final Map<String, String> pgtIouMap = new HashMap<String, String>();

	public CasAuthFilter(String casServerURL, Context context, Restlet next) {
		super(context, next);
		this.casServerURL = casServerURL;
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		Form queryParams = new Form(request.getResourceRef().getQuery());

		String serviceTicket = queryParams.getFirstValue("ticket");
		if (serviceTicket != null) {
			// Try to authenticate against CAS
			boolean success = validateServiceTicket(serviceTicket, request, response);
			return success ? Filter.CONTINUE : Filter.SKIP;
		}

		String pgtId = queryParams.getFirstValue("pgtId");
		String pgtIou = queryParams.getFirstValue("pgtIou");
		if (pgtId != null && pgtIou != null) {
			// FIXME: clean up the hash map once in a while
			pgtIouMap.put(pgtIou, pgtId);
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(new StringRepresentation("bla"));
			return Filter.SKIP;
		}

		Session session = SessionManager.get();
		if (session != null && session.getUsername() != null) {
			// Already authenticated
			return CONTINUE;
		}

		redirectToCAS(request, response);
		return Filter.SKIP;
	}

	private boolean validateServiceTicket(String serviceTicket, Request request, Response response)
		throws ResourceException
	{
		Reference validationURL = new Reference(casServerURL + "proxyValidate");
		validationURL.addQueryParameter("service", getServiceURL(request));
		validationURL.addQueryParameter("ticket", serviceTicket);
		validationURL.addQueryParameter("pgtUrl", getServiceURL(request));

		ClientResource serviceValidate = new ClientResource(validationURL);
		try {
			Representation validationResult = serviceValidate.get();
			ServiceResponse serviceResponse = ServiceResponseParser.parse(validationResult.getStream());

			if (serviceResponse instanceof AuthSuccess) {
				AuthSuccess authSuccess = (AuthSuccess)serviceResponse;
				request.getClientInfo().setUser(new User(authSuccess.getUser()));
				logger.debug("Validated CAS-ticket for user '{}'", authSuccess.getUser());

				if (authSuccess.getProxyGrantingTicket() != null) {
					String pgtId = pgtIouMap.remove(authSuccess.getProxyGrantingTicket());
					if (pgtId != null) {
						ProxyGrantingTicketRegistry.storeProxyGrantingTicket(pgtId);
					}
					else {
						logger.warn("pgtIou mapping missing for validated user '{}'", authSuccess.getUser());
					}
				}

				return true;
			}
			else if (serviceResponse instanceof AuthFailure) {
				AuthFailure authFailure = (AuthFailure)serviceResponse;
				response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, authFailure.getMessage());
			}
			else {
				logger.warn("Unexpected response from CAS server ({}): {}", serviceResponse.getClass(),
						serviceResponse);
				response.setStatus(Status.SERVER_ERROR_INTERNAL, "CAS server communication problem");
			}
		}
		catch (ResourceException e) {
			logger.warn("Failed to validate CAS-ticket " + serviceTicket, e);
			response.setStatus(Status.SERVER_ERROR_INTERNAL, "Failed to validate CAS-ticket");
		}
		catch (IOException e) {
			logger.warn("Unable to parse response from CAS server: {}", e.getMessage());
			response.setStatus(Status.SERVER_ERROR_INTERNAL, "Unable to parse response from CAS server");
		}
		catch (CasParseException e) {
			logger.warn("Failed to parse response from CAS server: {}", e.getMessage());
			response.setStatus(Status.SERVER_ERROR_INTERNAL, "Failed to parse response from CAS server");
		}
		catch (SAXException e) {
			logger.warn("Failed to parse response from CAS server: {}", e.getMessage());
			response.setStatus(Status.SERVER_ERROR_INTERNAL, "Failed to parse response from CAS server");
		}
		catch (ParserConfigurationException e) {
			logger.warn("Failed to parse response from CAS server: {}", e.getMessage());
			response.setStatus(Status.SERVER_ERROR_INTERNAL, "Failed to parse response from CAS server");
		}

		return false;
	}

	private void redirectToCAS(Request request, Response response) {
		Reference loginURL = new Reference(casServerURL + "login");
		loginURL.addQueryParameter("service", getServiceURL(request));
		response.redirectTemporary(loginURL);
	}

	private String getServiceURL(Request request) {
		return request.getResourceRef().toString(false, false);
	}
}
